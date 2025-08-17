package net.blixate.deadlight.commands;

import net.blixate.deadlight.util.FormatUtil;
import net.md_5.bungee.api.ChatColor;

public class MusicCommand extends DLCmd {

	@Override
	public void execute() {
		/*if(user.isInMatch()) {
			user.getPlayer().sendMessage(ChatColor.RED + "You cannot toggle music while in a game.");
			return;
		}*/
		if(hasArg(1)) {
			if(this.checkArgEqualIgnoreCase(0, "help")) {
				sendHelp();
			}else if(this.checkArgEqualIgnoreCase(0, "on")) {
				user.setMusicEnabled(true);
				user.getPlayer().sendMessage(ChatColor.GREEN + "Music enabled!");
			}else if(this.checkArgEqualIgnoreCase(0, "off")) {
				user.setMusicEnabled(false);
				user.getPlayer().sendMessage(ChatColor.RED + "Music disabled!");
			}
		}else {
			sendHelp();
		}
	}
	
	public void sendHelp() {
		String message = "&a&lMusic Help\n";
		message += "&a/music help&e: Displays this help message.\n";
		message += "&a/music (on/off)&e: Toggles whether to play music.";
		user.getPlayer().sendMessage(FormatUtil.color(message));
	}

}
