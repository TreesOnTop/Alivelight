package net.blixate.deadlight.discord.commands;

import net.blixate.discord.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class WtfCommand implements DiscordCommand {

	@Override
	public void execute(String command, String[] args, Message message, User user, MessageChannelUnion channel) {
		message.addReaction(Emoji.fromFormatted(":face_with_raised_eyebrow:")).queue();
	}

}
