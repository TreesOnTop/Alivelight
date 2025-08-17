package net.blixate.deadlight.commands.staff;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.blixate.deadlight.Deadlight;
import net.md_5.bungee.api.ChatColor;

public class StaffCommand implements CommandExecutor {

	public static void staffChat(CommandSender sender, String message) {
		Deadlight.staffMsg(Deadlight.msg("staff_message", new String[] {sender.getName(), message }));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender.hasPermission("deadlight.staffchat")) {
			if(args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Usage: /sc <message>");
			}else {
				staffChat(sender, String.join(" ", args));
			}
			return true;
		}
		return false;
	}
}
