package us.axefan.demo;

import org.bukkit.ChatColor;

public class Messages {

	// General Messages
	public static final String PluginStartingMessage = ChatColor.DARK_GRAY + "Starting...";
	public static final String InstallingDatabaseMessage = ChatColor.DARK_GRAY + "Installing database on first use...";
	public static final String PluginEnabledMessage = ChatColor.DARK_GRAY + "Version {$version} enabled.";
	
	// Error messages
	public static final String IntegerError = ChatColor.RED + "Invalid value: {$value}! '{$setting}' must be an integer";
	public static final String PositiveIntegerError = ChatColor.RED + "Invalid value: {$value}! '{$setting}' must be a positive integer";
	public static final String CreateEntryError = ChatColor.RED + "Error! Unable to create entry.";
	public static final String CreateEntriesError = ChatColor.RED + "Error! Unable to create entries.";
	public static final String DeleteEntriesError = ChatColor.RED + "Error! Unable to delete entries.";
	public static final String CheckServerLogs = ChatColor.RED + "Check the server log.";

}
