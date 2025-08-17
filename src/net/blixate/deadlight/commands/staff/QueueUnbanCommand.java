package net.blixate.deadlight.commands.staff;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLData;
import net.blixate.deadlight.player.PlayerManager;
import net.md_5.bungee.api.ChatColor;

public class QueueUnbanCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Check permission
		if(!sender.hasPermission("deadlight.queueunban")) {
			sender.sendMessage(Deadlight.msg("no_permission"));
			return false;
		}
		
		if(args.length >= 1) {
			@SuppressWarnings("deprecation")
			OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
			if(player == null || !player.hasPlayedBefore()) {
				sender.sendMessage(ChatColor.RED + "Unknown player " + ChatColor.YELLOW + player.getName());
				return false;
			}
			DLData user = PlayerManager.getOfflineUser(player.getUniqueId());
			user.queueUnban();
			user.savePlayerData();
			return true;
		}else {
			sender.sendMessage(ChatColor.RED + "Usage: /qunban <player>");
		}
		return false;
	}

}
