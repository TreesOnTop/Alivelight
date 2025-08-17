package net.blixate.deadlight.discord.commands;

import java.util.UUID;

import net.blixate.deadlight.discord.DiscordManager;
import net.blixate.deadlight.discord.VerificationCode;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.discord.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public class VerifyCommand implements DiscordCommand {
	
	@Override
	public void execute(String command, String[] args, Message message, User user, MessageChannelUnion channel) {
		if(args.length > 0) {
			String enterCode = args[0];
			for(VerificationCode code : DiscordManager.codes) {
				if(code.code.equals(enterCode)) {
					DiscordManager.codes.remove(code);
					// check if this code is valid
					if(code.isExpired()) {
						channel.sendMessage(":x: Code is expired! Generate a new one and try again.").queue();
						return;
					}
					// success
					UUID gen = code.getGenerator();
					DLUser userGen = PlayerManager.getUser(gen);
					if(userGen != null) {
						userGen.send("discord_verified", user.getAsTag());
						userGen.setDiscordUserID(user.getIdLong());
					}
					DiscordManager.setUserId(gen, user.getIdLong());
					channel.sendMessage(":white_check_mark: Linked with " + userGen.getName()).queue();
					return;
				}
			}
			channel.sendMessage(":x: Invalid code.").queue();
		}
	}

}
