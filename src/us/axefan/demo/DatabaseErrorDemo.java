package us.axefan.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.PersistenceException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;

public class DatabaseErrorDemo extends JavaPlugin{

	private String name;
    private final HashMap<Player, PlayerSettings> playerSettings = new HashMap<Player, PlayerSettings>();
	
	@Override
	public void onEnable(){
        PluginDescriptionFile desc = this.getDescription();
        this.name = desc.getName();
        this.sendMessage(Messages.PluginStartingMessage);
		this.getCommand("ded").setExecutor(new CommandExecutor(this));
		this.installDatabase();
		this.sendMessage(Messages.PluginEnabledMessage.replace("{$version}", desc.getVersion()));
	}
	
	@Override
	public void onDisable(){}

    /*
     * Ensures that the database for this plugin is installed.
     */
    private void installDatabase() {
		try {
			File ebeans = new File("ebean.properties");
			if (!ebeans.exists()) {
				try {
					ebeans.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			EbeanServer db = this.getDatabase();
			db.find(DatabaseEntry.class).findRowCount();
		} catch (PersistenceException ex) {
			this.sendMessage(Messages.InstallingDatabaseMessage);
			this.installDDL();
		}
	}
	
    /*
     * List the classes to store in the database.
     */
	public List<Class<?>> getDatabaseClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(DatabaseEntry.class);
		return list;
	}
	
	/*
	 * Sends a message.
	 * @param sender - The command sender.
	 * @param message - The message.
	 */
	protected void sendMessage(CommandSender sender, String message) {
		if (message.length() == 0) return;
		if (sender == null){
			this.sendMessage(message);
		}else{
			sender.sendMessage(message);
		}
	}
	
	/*
	 * Sends a message to the server console.
	 * @param message - The message.
	 */
	protected void sendMessage(String message) {
		System.out.print(this.name + ": " + ChatColor.stripColor(message));
	}

	/*
	 * Tests the ability to save records in batch.
	 * @param size - The size of the test;
	 */
	protected void test(CommandSender sender, int size) {
		// Get settings.
		Boolean batch = this.getBatchSetting(sender);
		Boolean journal = this.getJournalSetting(sender);
		Boolean cleanup = this.getCleanupSetting(sender);
		this.sendMessage(sender, "batch: " + batch);
		this.sendMessage(sender, "journal: " + journal);
		this.sendMessage(sender, "cleanup: " + cleanup);
		// Create some records.
		String playerName = (sender instanceof Player) ? ((Player)sender).getName() : "console";
		EbeanServer db = this.getDatabase();
		if (journal) db.beginTransaction();
		int result = 0;
		List<DatabaseEntry> createEntries = new ArrayList<DatabaseEntry>();
		for (int i=0; i<size; i++){
			// Create new entry
			DatabaseEntry entry = new DatabaseEntry();
			entry.setPlayerName(playerName);
			if (batch){
				createEntries.add(entry);
			}else{
				try{
					db.save(entry);
					result++;
				}catch(Exception ex){
					if (journal) db.rollbackTransaction();
					ex.printStackTrace();
					this.sendMessage(sender, Messages.CreateEntryError);
					if (sender instanceof Player) this.sendMessage(sender, Messages.CheckServerLogs);			
					return;
				}
			}
		}
		// Save objects - batch mode.
		if (batch){
			try{
				result = db.save(createEntries);
			}catch(Exception ex){
				if (journal) db.rollbackTransaction();
				ex.printStackTrace();
				this.sendMessage(sender, Messages.CreateEntriesError);
				if (sender instanceof Player) this.sendMessage(sender, Messages.CheckServerLogs);			
				return;
			}
		}
		if (journal) db.commitTransaction();
		// Check results.
		this.sendMessage(sender, ChatColor.DARK_GREEN + "expected: " + size);
		if (batch){
			if (createEntries.size() > 0){
				this.sendMessage(sender, ChatColor.DARK_GREEN + "entries: " + createEntries.size());			
			}else{
				this.sendMessage(sender, ChatColor.DARK_GREEN + "entries: 0");			
			}
		}
		if (size == result){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "result: " + result);
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "result: " + result);
		}
		List<DatabaseEntry> checkEntries = db.find(DatabaseEntry.class).findList();
		if (size == checkEntries.size()){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "check: " + checkEntries.size());
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "check: " + checkEntries.size());
		}
		// Delete the records - always in batch with journaling
		if (checkEntries.size() == 0) return;
		if (!cleanup) return;
		try{
			db.beginTransaction();
			result = db.delete(checkEntries);
		}catch(Exception ex){
			db.rollbackTransaction();
			ex.printStackTrace();
			this.sendMessage(sender, Messages.DeleteEntriesError);
			if (sender instanceof Player) this.sendMessage(sender, Messages.CheckServerLogs);			
			return;
		}
		db.commitTransaction();
		// Check delete results
		if (checkEntries.size() == result){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "deleted: " + result);
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "deleted: " + result);
		}
		// Should always be zero.
		int check = db.find(DatabaseEntry.class).findRowCount();
		if (check == 0){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "clear: " + check);
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "clear: " + check);
		}
	}

	/*
	 * Indicates whether batch mode is enabled for a player.
	 */
	private Boolean getBatchSetting(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) player = (Player)sender;
		if (!playerSettings.containsKey(player)) return true;
		return playerSettings.get(player).batch;
	}

	/*
	 * Indicates whether batch mode is enabled for a player.
	 */
	private Boolean getJournalSetting(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) player = (Player)sender;
		if (!playerSettings.containsKey(player)) return true;
		return playerSettings.get(player).journal;
	}

	/*
	 * Indicates whether records will automatically removed.
	 */
	private Boolean getCleanupSetting(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) player = (Player)sender;
		if (!playerSettings.containsKey(player)) return true;
		return playerSettings.get(player).cleanup;
	}
	
	/*
	 * Toggles the batch setting.
	 */
	protected void toggleBatch(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) player = (Player)sender;
		PlayerSettings settings = null;
		if (!playerSettings.containsKey(player)){
			settings = new PlayerSettings();
		}else{
			settings = playerSettings.get(player);
		}
		settings.batch = !settings.batch;
		playerSettings.put(player, settings);
		this.sendMessage(sender, "batch: " + settings.batch);
	}
	
	/*
	 * Toggles the journal setting.
	 */
	protected void toggleJournal(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) player = (Player)sender;
		PlayerSettings settings = null;
		if (!playerSettings.containsKey(player)){
			settings = new PlayerSettings();
		}else{
			settings = playerSettings.get(player);
		}
		settings.journal = !settings.journal;
		playerSettings.put(player, settings);
		this.sendMessage(sender, "journal: " + settings.journal);
	}
	
	/*
	 * Toggles the cleanup setting.
	 */
	protected void toggleCleanup(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) player = (Player)sender;
		PlayerSettings settings = null;
		if (!playerSettings.containsKey(player)){
			settings = new PlayerSettings();
		}else{
			settings = playerSettings.get(player);
		}
		settings.cleanup = !settings.cleanup;
		playerSettings.put(player, settings);
		this.sendMessage(sender, "cleanup: " + settings.cleanup);
	}
	
}
