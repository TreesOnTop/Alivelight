package net.blixate.deadlight.discord;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.discord.commands.HelpCommand;
import net.blixate.deadlight.discord.commands.LeaderboardCommand;
import net.blixate.deadlight.discord.commands.ListCommand;
import net.blixate.deadlight.discord.commands.LookupCommand;
import net.blixate.deadlight.discord.commands.MHLookupCommand;
import net.blixate.deadlight.discord.commands.OfferingsCommand;
import net.blixate.deadlight.discord.commands.ServerStatsCommand;
import net.blixate.deadlight.discord.commands.VerifyCommand;
import net.blixate.deadlight.discord.commands.WtfCommand;
import net.blixate.deadlight.player.DLData;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.time.Cooldown;
import net.blixate.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class DiscordManager {
	
	private static File file;
	private static FileConfiguration data;
	
	public static DiscordBot bot;
	public static boolean enabled;
	
	public static ArrayList<VerificationCode> codes = new ArrayList<>();
	
	public static Cooldown lfgCooldown = new Cooldown();
	
	public static void createBot() {
		// check if this bot is enabled
		enabled = getSettings().getBoolean("enabled");
		if(enabled) {
			bot = new DiscordBot(getSettings());
			bot.addDiscordCommand("list", new ListCommand());
			bot.addDiscordCommand("lookup", new LookupCommand());
			bot.addDiscordCommand("verify", new VerifyCommand());
			bot.addDiscordCommand("mhlookup", new MHLookupCommand());
			bot.addDiscordCommand("offerings", new OfferingsCommand());
			bot.addDiscordCommand("serverinfo", new ServerStatsCommand());
			bot.addDiscordCommand("help", new HelpCommand());
			bot.addDiscordCommand("sex", new WtfCommand());
			bot.addDiscordCommand("gaysex", new WtfCommand());
			bot.addDiscordCommand("saygex", new WtfCommand());
			LeaderboardCommand command = new LeaderboardCommand();
			bot.addDiscordCommand("leaderboard", command);
			bot.addDiscordCommand("lb", command);
			bot.addDiscordCommand("top", command);
			bot.setMessageListener(new ChatListener());
			try {
				bot.load();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			bot.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
			bot.getJDA().getPresence().setActivity(Activity.playing("Deadlight.minehut.gg"));
			bot.getJDA().updateCommands().addCommands(
					Commands.slash("list", "List all online players."),
					Commands.slash("lookup", "Lookup a player's data")
						.addOption(OptionType.STRING, "username", "The player's username."),
					Commands.slash("verify", "Link your Minecraft account to Discord.")
						.addOption(OptionType.STRING, "code", "The code provided in-game."),
					Commands.slash("serverinfo", "View public server details."),
					//Commands.slash("help", "List of all commands."),
					Commands.slash("lookingforgroup", "Ping the LFG role"),
					Commands.slash("leaderboard", "View the server leaderboard.")).queue();
			bot.getJDA().addEventListener(new SlashCommandHandler());
		}
	}
	
	public static void shutdownBot() {
		bot.unload();
	}
	
	public static void load() {
		file = new File(Deadlight.dataFolder, "discord.yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		data = YamlConfiguration.loadConfiguration(file);
	}
	
	public static void sendAnnouncement(String title, String content, String author) {
		sendAnnouncement(title, content, author, null);
	}
	
	public static void sendAnnouncement(String title, String content, String author, String message) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.setDescription(content);
		builder.setColor(Color.orange);
		builder.setFooter("Announcement | Created by " + author);
		TextChannel channel = bot.getJDA().getChannelById(TextChannel.class, Deadlight.cfg().getLong("bot.announcement channel"));
		MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
		if(message != null) {
			messageBuilder.setContent(message);
		}
		
		messageBuilder.setEmbeds(builder.build());
		MessageCreateAction action = channel.sendMessage(messageBuilder.build());
		action.queue(msg -> {
			msg.addReaction(Emoji.fromUnicode("🎉")).queue();
		});
	}
	
	public static void sendStaffAlert(String title, String content) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(title);
		builder.setDescription(content);
		builder.setColor(Color.red);
		TextChannel channel = bot.getJDA().getChannelById(TextChannel.class, Deadlight.cfg().getLong("bot.staff message channel"));
		channel.sendMessageEmbeds(builder.build()).queue();
	}
	
	public static void sendChatMessage(DLUser sender, String message) {
		long channelId = Deadlight.cfg().getLong("bot.minecraft chat channel");
		if(channelId == -1) {
			return;
		}
		TextChannel channel = bot.getJDA().getChannelById(TextChannel.class, channelId);
		channel.sendMessage("`"+sender.getName()+": "+message.replace('`', '\'')+"`").queue();
	}
	
	public static void save() {
		try {
			data.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static DLData getUserById(long id) {
		UUID uuid = UUID.fromString(data.getString("users." + id));
		return PlayerManager.getOfflineUser(uuid);
	}
	
	public static void setUserId(UUID uuid, long id) {
		data.set("users." + id, uuid.toString());
	}

	public static boolean isVerified(long id) {
		return data.contains("users." + id);
	}
	
	public static boolean isVerified(DLUser user) {
		if(user.getDiscordUserID() == 0) {
			return false;
		}
		return data.contains("users." + user.getDiscordUserID());
	}
	
	public static boolean isLoaded() {
		return file != null;
	}
	
	public static DiscordBot getBot() {
		return bot;
	}

	public static ConfigurationSection getSettings() {
		return Deadlight.inst.getConfig().getConfigurationSection("bot");
	}
	
	public static boolean hasAdmin(Guild guild, User user) {
		Member member = guild.getMemberById(user.getId());
		Role role = guild.getRoleById(DiscordManager.getSettings().getLong("admin role id"));
		return member.getRoles().contains(role) && member.hasPermission(Permission.ADMINISTRATOR);
	}
}
