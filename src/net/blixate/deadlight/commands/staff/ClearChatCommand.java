package net.blixate.deadlight.commands.staff;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.blixate.deadlight.commands.DLCmd;
import net.blixate.deadlight.util.FormatUtil;

public class ClearChatCommand extends DLCmd {
	
	public ClearChatCommand() {
		super("deadlight.clearchat", "/clearchat", true);
	}
	
	public void execute() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			for(int i = 0; i < 10; i++) {
				if(!p.hasPermission("deadlight.clearchat.bypass")) {
					p.sendMessage(FormatUtil.color("\n&f\n&f\n&f\n&f\n&f\n&f\n&f\n&f\n&f\n&f\n&f\n&f"));
				}
			}
			p.sendMessage(FormatUtil.color("&aChat has been cleared."));
		}
	}
	
}
