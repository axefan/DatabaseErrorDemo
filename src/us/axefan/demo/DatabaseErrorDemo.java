package us.axefan.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	public void onDisable(){
		
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
	 * Tests the ability to save records in batch.
	 * @param size - The size of the test;
	 */
	protected void test(CommandSender sender, int size) {
		EbeanServer db = this.getDatabase();
		// create some records.
		int result;
		List<DatabaseEntry> createEntries = new ArrayList<DatabaseEntry>();
		for (int i=0; i<size; i++){
			createEntries.add(new DatabaseEntry());
		}
		try{
			db.beginTransaction();
			result = db.save(createEntries);
		}catch(Exception ex){
			db.rollbackTransaction();
			ex.printStackTrace();
			this.sendMessage(sender, Messages.CreateEntriesError);
			if (sender instanceof Player) this.sendMessage(sender, Messages.CheckServerLogs);			
			return;
		}
		db.commitTransaction();
		// check results.
		this.sendMessage(sender, ChatColor.DARK_GREEN + "expected: " + size);
		if (createEntries.size() > 0){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "entries: " + createEntries.size());			
		}else{
			this.sendMessage(sender, ChatColor.DARK_GREEN + "entries: 0");			
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
		if (createEntries.size() == 0) return;
		// delete the records
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
		// check results
		if (checkEntries.size() == result){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "deleted: " + result);
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "deleted: " + result);
		}
		int check = db.find(DatabaseEntry.class).findRowCount();
		if (check == 0){
			this.sendMessage(sender, ChatColor.DARK_GREEN + "clear: " + check);
		}else{
			this.sendMessage(sender, ChatColor.DARK_RED + "clear: " + check);
		}
	}
	
}
