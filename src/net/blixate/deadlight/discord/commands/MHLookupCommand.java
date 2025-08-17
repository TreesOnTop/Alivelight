package net.blixate.deadlight.discord.commands;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

import net.blixate.deadlight.util.WebUtils;
import net.blixate.discord.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class MHLookupCommand implements DiscordCommand {

	@SuppressWarnings("deprecation")
	@Override
	public void execute(String command, String[] args, Message message, User user, MessageChannelUnion channel) {
		String uuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString();
		JSONObject json;
		try {
			json = WebUtils.getJSON(WebUtils.MINEHUT_COSMETICS_URL.replace("%uuid", uuid));
			channel.sendMessage(args[0] + "'s Minehut Rank: " + (String)json.get("rank")).queue();
		}catch(Throwable t) {
			channel.sendMessage("Cannot get player's rank.").queue();
		}
	}

}
