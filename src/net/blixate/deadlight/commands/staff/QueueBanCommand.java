package net.blixate.deadlight.commands.staff;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLData;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.time.TimeParser;
import net.md_5.bungee.api.ChatColor;

public class QueueBanCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Check permission
		if(!sender.hasPermission("deadlight.queueban")) {
			sender.sendMessage(Deadlight.msg("no_permission"));
			return false;
		}
		
		if(args.length >= 2) {
			@SuppressWarnings("deprecation")
			OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
			if(player == null || !player.hasPlayedBefore()) {
				sender.sendMessage(ChatColor.RED + "Unknown player " + ChatColor.YELLOW + player.getName());
				return false;
			}
			long time = TimeParser.parseTime(args[1]);
			DLData user = PlayerManager.getOfflineUser(player.getUniqueId());
			if(player.isOnline()) {
				DLUser onlineUser = PlayerManager.getUser(player.getUniqueId());
				if(onlineUser.isQueued()) {
					Deadlight.queue.removeDLUser(onlineUser);
				}
			}
			user.queueBan = System.currentTimeMillis() + time;
			user.savePlayerData();
			sender.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has been queue banned for " + ChatColor.YELLOW + TimeParser.toFancyTime(time));
			return true;
		}else {
			sender.sendMessage(ChatColor.RED + "Usage: /qban <player> <duration>");
		}
		return false;
	}

}
