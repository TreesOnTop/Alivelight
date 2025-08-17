package net.blixate.deadlight.commands;

import net.blixate.deadlight.discord.DiscordManager;
import net.blixate.deadlight.discord.VerificationCode;
import net.blixate.deadlight.util.FormatUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class VerifyCommand extends DLCmd {
	
	@Override
	public void execute() {
		VerificationCode code = new VerificationCode(user.getUUID(), 5);
		DiscordManager.codes.add(code);
		// Create a message that can be copied
		TextComponent text = new TextComponent(FormatUtil.color("&9&lDiscord&b "));
		text.addExtra(FormatUtil.color("&bUse &e!verify " + code + "&b on the Discord to link your account."));
		text.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "!verify " + code));
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(FormatUtil.color("&bClick here to copy the command!"))));
		user.getPlayer().spigot().sendMessage(text);
	}

}
