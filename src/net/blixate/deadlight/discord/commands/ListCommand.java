package net.blixate.deadlight.discord.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.blixate.discord.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class ListCommand implements DiscordCommand {

	@Override
	public void execute(String command, String[] args, Message message, User user, MessageChannelUnion channel) {
		String[] playerList = new String[Bukkit.getOnlinePlayers().size()];
		if(Bukkit.getOnlinePlayers().size() == 0) {
			channel.sendMessage("There are no players online.").queue();
		}else {
			int i = 0;
			for(Player player : Bukkit.getOnlinePlayers()) {
				playerList[i] = "`" + player.getName() + "`";
				i++;
			}
			channel.sendMessage("**Online Players** (" + playerList.length + "/" + Bukkit.getMaxPlayers() + "): " +String.join(", ", playerList)).queue();
		}
	}

}
