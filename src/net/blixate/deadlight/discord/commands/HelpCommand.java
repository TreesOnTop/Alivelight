package net.blixate.deadlight.discord.commands;

import java.awt.Color;

import net.blixate.discord.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class HelpCommand implements DiscordCommand {

	@Override
	public void execute(String command, String[] args, Message message, User user, MessageChannelUnion channel) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.addField(new Field("Commands", String.join("\n", getNormalCommands()), false));
		builder.setColor(Color.orange);
		channel.sendMessageEmbeds(builder.build()).queue();;
	}
	
	public static String[] getNormalCommands() {
		return new String[] {
			"!leaderboard - View the leaderboard",
			"!list - List online players",
			"!lookup [player] - Lookup a player's information!",
			"!offerings - View active offerings",
			"!verify [code] - Link your Discord account to your Minecraft account",
			"!help - Display this help message",
			"!mhlookup [player] - Lookup a Minehut player's rank",
			"!serverstats - See the server's current statistics",
		};
	}

}
