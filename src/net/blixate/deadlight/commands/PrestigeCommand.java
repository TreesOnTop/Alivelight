package net.blixate.deadlight.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.blixate.deadlight.discord.DiscordManager;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;

public class PrestigeCommand implements CommandExecutor  {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("You can't prestige a non-player account!");
			return true;
		}
		Player p = (Player)sender;
		DLUser user = PlayerManager.getUser(p.getUniqueId());
		if(user.getPrestige() >= DLUser.MAX_PRESTIGE) {
			user.send("max_prestige");
			return true;
		}
		if(user.getLevel() >= DLUser.MAX_LEVEL) {
			user.prestige();
			DiscordManager.sendAnnouncement(user.getName() + " has prestiged!", user.getName() + " is now prestige " + user.getPrestige(), "Deadlight");
		}else{
			user.send("cant_prestige");
		}
		return true;
	}

}
