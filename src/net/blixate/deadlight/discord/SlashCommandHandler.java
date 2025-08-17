package net.blixate.deadlight.discord;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.discord.commands.LeaderboardCommand;
import net.blixate.deadlight.discord.commands.LookupCommand;
import net.blixate.deadlight.discord.commands.ServerStatsCommand;
import net.blixate.deadlight.player.DLData;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.time.TimeParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandHandler extends ListenerAdapter {
	
	@SuppressWarnings("deprecation")
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getName().equals("list")) {
			String[] playerList = new String[Bukkit.getOnlinePlayers().size()];
			if(Bukkit.getOnlinePlayers().size() == 0) {
				event.reply("There are no players online.").queue();
			}else {
				int i = 0;
				for(Player player : Bukkit.getOnlinePlayers()) {
					playerList[i] = "`" + player.getName() + "`";
					i++;
				}
				event.reply("**Online Players** (" + playerList.length + "/" + Bukkit.getMaxPlayers() + "): " +String.join(", ", playerList)).queue();
			}
		}else if(event.getName().equals("lookup")) {
			DLData data;
			OfflinePlayer player = null;
			if(event.getOption("username") == null) {
				if(DiscordManager.isVerified(event.getUser().getIdLong())) {
					data = DiscordManager.getUserById(event.getUser().getIdLong());
					player = Bukkit.getOfflinePlayer(FormatUtil.getUUIDFromString(data.getUUIDString()));
					if(!LookupCommand.loadData(data)) {
						event.reply(":x: An error occurred! You don't exist?").queue();
						return;
					}
				} else {
					event.reply(":x: You are not verified. Please put a player name to look up or go through the verification process.").queue();
					return;
				}
			} else {
				player = Bukkit.getOfflinePlayer(event.getOption("username").getAsString());
			}
			if(!player.isOnline()) {
				data = PlayerManager.getOfflineUser(player.getUniqueId());
				if(!LookupCommand.loadData(data)) {
					event.reply(":x: That player doesn't exist.").setEphemeral(true).queue();
					return;
				}
			}else {
				data = PlayerManager.getUser(player.getUniqueId());
			}
			event.replyEmbeds(LookupCommand.getPlayerData(data, player).build()).queue();
		}else if(event.getName().equals("leaderboard")) {
			Deadlight.getLeaderboard().checkCache();
			EmbedBuilder embed = LeaderboardCommand.getLeaderboardEmbed();
			event.replyEmbeds(embed.build()).queue();
		}else if(event.getName().equals("serverinfo")) {
			EmbedBuilder embed = ServerStatsCommand.getServerStatsEmbed();
			event.replyEmbeds(embed.build()).queue();
		}else if(event.getName().equals("lookingforgroup")) {
			long channelId = DiscordManager.getSettings().getLong("looking for group channel");
			long cooldown = DiscordManager.getSettings().getLong("looking for group cooldown");
			long roleId = DiscordManager.getSettings().getLong("looking for group role id");
			if(event.getChannel().getIdLong() != channelId) {
				event.reply("Incorrect channel. Please use <#"+channelId+">").setEphemeral(true).queue();
			}
			if(!DiscordManager.lfgCooldown.isDone()) {
				event.reply("You must wait **" + TimeParser.toLongFancyTime(DiscordManager.lfgCooldown.timeLeft()) + "** Looking for Group can be pinged again!").setEphemeral(true).queue();
			}else {
				event.reply("<@&"+roleId+"> There are " + Bukkit.getOnlinePlayers().size() + " players looking to play!").queue();
				DiscordManager.lfgCooldown.start(cooldown);
			}
			
		}
	}
	
}
