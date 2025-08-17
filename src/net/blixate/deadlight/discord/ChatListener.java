package net.blixate.deadlight.discord;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.discord.MessageListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class ChatListener implements MessageListener {
	
	// Listens to Discord messages, which we can ignore for now
	@Override
	public boolean onMessage(Guild guild, User user, Message message, MessageChannelUnion channel) {
		if(channel.getIdLong() != Deadlight.cfg().getLong("bot.minecraft chat channel") || user.isBot()) {
			return true;
		}
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(FormatUtil.color("&9&lDiscord &7" + user.getName() + ": &f" + message.getContentDisplay()));
		}
		/*if(DiscordManager.isVerified(user.getIdLong())) {
			
		}else {
			channel.sendMessage(":x: You need to be verified to talk in Minecraft chat!");
		}*/
		return true;
	}
	
}
