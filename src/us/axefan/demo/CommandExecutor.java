package us.axefan.demo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.axefan.demo.Messages;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {

	private static DatabaseErrorDemo plugin;
	
	public CommandExecutor(DatabaseErrorDemo instance) {
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		label = label.toLowerCase();
		if(!label.equals("ded")) return false;
		if (args.length < 1)  return false;
		String action = args[0].toLowerCase();
		if (action.equals("tx")) {
			plugin.toggleTransactions(sender);
			return true;
		}else{
			// Get the test size
			int size;
			String value = args[0].trim();
			try{
				size = Integer.parseInt(value);
			}catch(Exception ex){
				plugin.sendMessage(sender, Messages.IntegerError.replace("{$value}", value).replace("{$setting}", "size"));
				return true;
			}
			if (size < 1){
				plugin.sendMessage(sender, Messages.PositiveIntegerError.replace("{$value}", value).replace("{$setting}", "size"));
				return true;			
			}
			plugin.test(sender, size);
			return true;
		}
	}

}
