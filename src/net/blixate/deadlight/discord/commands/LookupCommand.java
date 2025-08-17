package net.blixate.deadlight.discord.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.discord.DiscordManager;
import net.blixate.deadlight.kit.type.KillerTypes;
import net.blixate.deadlight.player.DLData;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.discord.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class LookupCommand implements DiscordCommand {

	@SuppressWarnings("deprecation")
	@Override
	public void execute(String command, String[] args, Message message, User user, MessageChannelUnion channel) {
		DLData data = null;
		OfflinePlayer player = null;
		if(args.length < 1) {
			if(DiscordManager.isVerified(user.getIdLong())) {
				data = DiscordManager.getUserById(user.getIdLong());
				player = Bukkit.getOfflinePlayer(FormatUtil.getUUIDFromString(data.getUUIDString()));
				if(!loadData(data)) {
					channel.sendMessage(":x: An error occurred! You don't exist?").queue();
					return;
				}
			}else {
				channel.sendMessage(":x: You are not verified. Please put a player name to look up or go through the verification process.").queue();
				return;
			}
		}else {
			player = Bukkit.getOfflinePlayer(args[0]);
			if(!player.isOnline()) {
				data = PlayerManager.getOfflineUser(player.getUniqueId());
				if(!loadData(data)) {
					channel.sendMessage(":x: That player doesn't exist.").queue();
					return;
				}
			}else {
				data = PlayerManager.getUser(player.getUniqueId());
			}
		}
		if(player == null || data == null) {
			channel.sendMessage(":x: Something went wrong.").queue();;
			return;
		}
		
		EmbedBuilder builder = getPlayerData(data, player);
		channel.sendMessageEmbeds(builder.build()).queue();
	}
	
	public static EmbedBuilder getPlayerData(DLData data, OfflinePlayer player) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(player.getName());
		builder.addField(new Field("Progression", "Prestige " + data.getPrestige() + "\nLevel " + data.getLevel(), true));
		builder.addField(new Field("Statistics", "Kills: " + data.kills + "\nDeaths: " + data.deaths + "\nEscapes: " + data.escapes, true));
		builder.addField(new Field("Currency", "Blood: " + FormatUtil.formatCurrency(data.blood) + "\nSouls: " + FormatUtil.formatCurrency(data.souls), true));
		if(data.nickname != null) {
			builder.addField(new Field("Nickname", data.nickname, false));
		}
		
		if(!data.getSurvivorPerks().isEmpty()) {
			String[] perks = new String[data.getSurvivorPerks().size()];
			int i = 0;
			for(String perk : data.getSurvivorPerks()) {
				perks[i] = Deadlight.getPerkManager().getSurvivorPerk(perk).name+" "+ data.getPerkTier(perk);
				i++;
			}
			builder.addField(new Field("Survivor Perks", String.join("\n", perks), true));
		}
		if(!data.getKillerPerks().isEmpty()) {
			String[] perks = new String[data.getKillerPerks().size()];
			int i = 0;
			for(String perk : data.getKillerPerks()) {
				perks[i] = Deadlight.getPerkManager().getKillerPerk(perk).name +" "+ data.getPerkTier(perk);
				i++;
			}
			builder.addField(new Field("Killer Perks", String.join("\n", perks), true));
		}
		// killer types
		String messages = "";
		for(KillerTypes type : KillerTypes.values()) {
			if(type == data.killerType) {
				messages += "**" + type.getName() + "**";
			}else {
				messages += type.getName();
			}
			messages += " Lv. " + data.killerData.get(type.name()).level + "\n";
		}
		builder.addField(new Field("Killer Types", messages, true));
		return builder;
	}

	public static boolean loadData(DLData data) {
		try {
			data.loadPlayerData();
			return true;
		}catch(Throwable t) {
			t.printStackTrace();
			return false;
		}
	}
	
}
