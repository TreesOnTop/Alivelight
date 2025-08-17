package net.blixate.deadlight.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;

public class QueueCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		
		DLUser user = PlayerManager.getUser(sender);
		user.queue();
		return true;
	}
}
