package net.blixate.deadlight.commands.admin;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import net.blixate.deadlight.discord.DiscordManager;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.file.SongManager;

public class RankPurchaseCommand implements CommandExecutor {
	
	// Arg 1 = Username
	// Arg 2 = Purchased Item
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		ArrayList<String> list = Lists.newArrayList(args);
		list.remove(0);
		String purchased = String.join(" ", list.toArray(new String[0]));
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("");
			player.sendMessage(FormatUtil.color("&9&lANNOUNCEMENT &e" + args[0] + " &dhas purchased &e" + purchased));
			player.sendMessage("");
		}
		SongManager.playSong(SongManager.getSong("cheers"), Bukkit.getOnlinePlayers());
		DiscordManager.sendAnnouncement(args[0] + " has purchased " + purchased, label, "Tebex");
		return true;
	}
}
