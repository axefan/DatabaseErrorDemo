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
	 * Tests the ability to save records based on user setting.
	 * @param sender - The command sender.
	 * @param size - The size of the test;
	 */
	protected void test(CommandSender sender, int size) {
		if (this.getTransactionsSetting(sender)){
			this.testTx(sender, size);
		}else{
			this.testList(sender, size);
		}
	}
	
	/*
	 * Tests the ability to save records using transactions.
	 * @param sender - The command sender.
	 * @param size - The size of the test;
	 */
	protected void testTx(CommandSender sender, int size) {
		// Create some records.
		String playerName = (sender instanceof Player) ? ((Player)sender).getName() : "console";
		EbeanServer db = this.getDatabase();
		db.beginTransaction();
		try{
			for (int i=0; i<size; i++){
				DatabaseEntry entry = new DatabaseEntry();
				entry.setPlayerName(playerName);
				db.save(entry);
			}
			db.commitTransaction();
		}catch(Exception ex){
			db.rollbackTransaction();
			ex.printStackTrace();
			this.sendMessage(sender, Messages.CreateEntriesError);
			if (sender instanceof Player) this.sendMessage(sender, Messages.CheckServerLogs);
			return;
		}finally{
			db.endTransaction();
		}
		// Check create results.
		List<DatabaseEntry> entries = db.find(DatabaseEntry.class).where().eq("playerName", playerName).findList();
		this.sendMessage(sender, ChatColor.DARK_GREEN + "expected: " + size);
		if (size == entries.size()){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "found: " + entries.size());
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "found: " + entries.size());
		}
		// Delete test records.
		db.beginTransaction();
		try{
			db.delete(entries);
			db.commitTransaction();
		}catch(Exception ex){
			db.rollbackTransaction();
			ex.printStackTrace();
			this.sendMessage(sender, Messages.DeleteEntriesError);
			if (sender instanceof Player) this.sendMessage(sender, Messages.CheckServerLogs);
			return;
		}finally{
			db.endTransaction();
		}
		// Check delete results.
		entries = db.find(DatabaseEntry.class).where().eq("playerName", playerName).findList();
		if (entries.size() == 0){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "count: " + entries.size());
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "count: " + entries.size());
		}
	}
	
	/*
	 * Tests the ability to save records using a list.
	 * @param sender - The command sender.
	 * @param size - The size of the test.
	 */
	protected void testList(CommandSender sender, int size) {
		// Create some records.
		String playerName = (sender instanceof Player) ? ((Player)sender).getName() : "console";
		EbeanServer db = this.getDatabase();
		List<DatabaseEntry> entries = new ArrayList<DatabaseEntry>();
		try{
			for (int i=0; i<size; i++){
				DatabaseEntry entry = new DatabaseEntry();
				entry.setPlayerName(playerName);
				entries.add(entry);
			}
			db.save(entries);
		}catch(Exception ex){
			ex.printStackTrace();
			this.sendMessage(sender, Messages.CreateEntriesError);
			if (sender instanceof Player) this.sendMessage(sender, Messages.CheckServerLogs);	
			return;
		}
		// Check create results.
		entries = db.find(DatabaseEntry.class).where().eq("playerName", playerName).findList();
		this.sendMessage(sender, ChatColor.DARK_GREEN + "expected: " + size);
		if (size == entries.size()){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "found: " + entries.size());
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "found: " + entries.size());
		}
		// Delete test records.
		try{
			db.delete(entries);
		}catch(Exception ex){
			ex.printStackTrace();
			this.sendMessage(sender, Messages.DeleteEntriesError);
			if (sender instanceof Player) this.sendMessage(sender, Messages.CheckServerLogs);	
			return;
		}
		// Check delete results.
		entries = db.find(DatabaseEntry.class).where().eq("playerName", playerName).findList();
		if (entries.size() == 0){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "count: " + entries.size());
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "count: " + entries.size());
		}
	}

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
	 * Toggles the player's transactions setting.
	 * @param sender - The command sender.
	 */
	public void toggleTransactions(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) player = (Player)sender;
		PlayerSettings settings = null;
		if (!playerSettings.containsKey(player)){
			settings = new PlayerSettings();
		}else{
			settings = playerSettings.get(player);
		}
		settings.transactions = !settings.transactions;
		playerSettings.put(player, settings);
		this.sendMessage(sender, "transactions: " + settings.transactions);
	}
	
	/*
	 * Indicates whether transactions are enabled for a player.
	 * @param sender - The command sender.
	 */
	private Boolean getTransactionsSetting(CommandSender sender) {
		Player player = null;
		if (sender instanceof Player) player = (Player)sender;
		if (!playerSettings.containsKey(player)) return true;
		return playerSettings.get(player).transactions;
	}

	
}
