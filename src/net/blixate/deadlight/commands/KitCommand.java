package net.blixate.deadlight.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.blixate.deadlight.kit.KitGui;
import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.type.addons.AddonManager;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.KillerData;
import net.blixate.deadlight.player.PlayerManager;
import net.md_5.bungee.api.ChatColor;

public class KitCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		DLUser user = PlayerManager.getUser(sender);
		if(user.getLobby() != null) {
			Player p = user.getPlayer();
			if(!user.perks.isEmpty()) {
				p.sendMessage(ChatColor.YELLOW + "Your Perks:");
				for(Perk perk : user.perks) {
					p.sendMessage(ChatColor.YELLOW + "- " + perk.getRegistry().name + ChatColor.GOLD + ChatColor.BOLD + " " + "I".repeat(perk.getTier()));
				}
			}else {
				p.sendMessage(ChatColor.RED + "You don't have any perks!");
			}
			if(user.isKiller()) {
				KillerData data = user.getKillerData();
				if(!data.selectedAugments.isEmpty()) {
					p.sendMessage(ChatColor.YELLOW + "Your Augments:");
					for(String addon : data.selectedAugments) {
						String addonName = AddonManager.getNameById(user.killerType, addon);
						p.sendMessage(ChatColor.YELLOW + "- " + addonName);
					}
				}else {
					p.sendMessage(ChatColor.RED + "You don't have any augments!");
				}
			}
		}
		else if(user != null) {
			user.openInventory(new KitGui(user));
		}
		return true;
	}
	
}
