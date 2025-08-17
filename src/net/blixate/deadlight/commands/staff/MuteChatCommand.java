package net.blixate.deadlight.commands.staff;

import org.bukkit.Bukkit;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.commands.DLCmd;
import net.blixate.deadlight.util.FormatUtil;

public class MuteChatCommand extends DLCmd {
	
	public MuteChatCommand() {
		super("deadlight.mutechat", "/mutechat", true);
	}
	
	@Override
	public void execute() {
		muteChat();
	}

	public static void muteChat() {
		Deadlight.muteChat = !Deadlight.muteChat;
		if(Deadlight.muteChat) {
			Bukkit.getOnlinePlayers().forEach((p) -> p.sendMessage(FormatUtil.color("&cChat has been muted!")));
		}else {
			Bukkit.getOnlinePlayers().forEach((p) -> p.sendMessage(FormatUtil.color("&cChat has been unmuted!")));
		}
	}
	
}
