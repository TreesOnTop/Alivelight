package net.blixate.deadlight.discord.commands;

import java.awt.Color;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.leaderboard.Leaderboard;
import net.blixate.discord.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class LeaderboardCommand implements DiscordCommand {

	@Override
	public void execute(String command, String[] args, Message message, User user, MessageChannelUnion channel) {
		Deadlight.getLeaderboard().checkCache();
		EmbedBuilder embed = getLeaderboardEmbed();
		channel.sendMessageEmbeds(embed.build()).queue();
	}
	
	public static EmbedBuilder getLeaderboardEmbed() {
		EmbedBuilder embed = new EmbedBuilder();
		String list = "";
		for(int i = 0; i < Math.min(Deadlight.getLeaderboard().cache.size(), Leaderboard.TOP_RESULTS); i++) {
			if(i >= Deadlight.getLeaderboard().cache.size()) {
				break;
			}
			Leaderboard.LeaderboardMember data = (Leaderboard.LeaderboardMember) Deadlight.getLeaderboard().cache.get(i);
			list += "" + (i+1) + ". **" + data.getName() + "** (Level "+data.level+", Prestige "+data.prestige + ")\n";
		}
		embed.setTitle("Leaderboard");
		embed.setDescription(list);
		embed.setColor(Color.green);
		return embed;
	}

}
