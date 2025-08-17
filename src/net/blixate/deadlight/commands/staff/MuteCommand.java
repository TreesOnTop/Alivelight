package net.blixate.deadlight.commands.staff;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLData;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.md_5.bungee.api.ChatColor;

public class MuteCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Check permission
		if(!sender.hasPermission("deadlight.mute")) {
			sender.sendMessage(Deadlight.msg("no_permission"));
			return true;
		}
		
		if(args.length >= 1) {
			// Convert arguments to variables
			@SuppressWarnings("deprecation")
			OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
			if(player == null || !player.hasPlayedBefore()) {
				sender.sendMessage(ChatColor.RED + "Unknown player.");
				return true;
			}
			String punisher = sender.getName();
			String reason = null;
			if(args.length > 1) {
				String[] reasonArr = Arrays.copyOfRange(args, 1, args.length);
				reason = String.join(" ", reasonArr);
			}
			// Perform the mute operation
			DLData user = PlayerManager.getOfflineUser(player.getUniqueId());
			if(user == null) {
				sender.sendMessage(ChatColor.RED + "No player data available.");
				return false;
			}
			user.muteDuration = Long.MAX_VALUE;
			user.muteReason = reason;
			user.savePlayerData();
			// Make it public
			if(reason != null) {
				for(DLUser u : PlayerManager.getPlayers()) {
					u.send("mute_public", player.getName(), punisher, reason);
				}
			}else {
				for(DLUser u : PlayerManager.getPlayers()) {
					u.send("mute_no_reason", player.getName(), punisher);
				}
			}
			return true;
		}else {
			sender.sendMessage(ChatColor.RED + "Usage: /mute <player> <reason>");
		}
		return true;
	}
	
}
