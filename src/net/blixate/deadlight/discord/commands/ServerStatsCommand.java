package net.blixate.deadlight.discord.commands;

import java.awt.Color;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.bukkit.Bukkit;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.nms.NMSHandler;
import net.blixate.discord.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class ServerStatsCommand implements DiscordCommand {

	@Override
	public void execute(String cmd, String[] args, Message message, User user, MessageChannelUnion channel) {
		EmbedBuilder embed = getServerStatsEmbed();
		channel.sendMessageEmbeds(embed.build()).queue();
	}
	
	public static EmbedBuilder getServerStatsEmbed() {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("Server Info");
		embed.addField(new Field("Players", getPlayerData(), true));
		embed.addField(new Field("System", getServerData(), true));
		embed.addField(new Field("Version", getVersionData(), true));
		embed.setColor(Color.gray);
		return embed;
	}
	
	public static String getServerData() {
		String msg = "";
		msg += add("Time", "<t:" + (System.currentTimeMillis()/1000) + ">");
		msg += add("Specs", System.getProperty("os.name") + " " + System.getProperty("os.version") + "\nCores: " + Runtime.getRuntime().availableProcessors());
		return msg;
	}
	
	public static String getVersionData() {
		String msg = "";
		msg += add("Bukkit/Spigot Version", Bukkit.getBukkitVersion() + " / " + Bukkit.getVersion());
		msg += add("NMS Version", NMSHandler.getNMSVersion());
		msg += add("Java Version", System.getProperty("java.version"));
		msg += add("Deadlight Version", Deadlight.getVersion());
		return msg;
	}
	
	public static String getPlayerData() {
		String msg = "";
		msg += add("Total Joins", "" + Bukkit.getOfflinePlayers().length);
		msg += add("Total Player File Size", humanReadableByteCountSI(Deadlight.sizeOfDirectory(PlayerManager.getPlayerFolder())));
		return msg;
	}
	
	public static String add(String title, String data) {
		String message = "";
		message += "**" + title + "**\n";
		message += data + "\n";
		return message;
	}
	
	public static String humanReadableByteCountSI(long bytes) {
	    if (-1000 < bytes && bytes < 1000) {
	        return bytes + " B";
	    }
	    CharacterIterator ci = new StringCharacterIterator("kMGTPE");
	    while (bytes <= -999_950 || bytes >= 999_950) {
	        bytes /= 1000;
	        ci.next();
	    }
	    return String.format("%.1f %cB", bytes / 1000.0, ci.current());
	}
}
