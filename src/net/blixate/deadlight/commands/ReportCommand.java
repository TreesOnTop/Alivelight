package net.blixate.deadlight.commands;

import org.bukkit.Bukkit;

import net.blixate.deadlight.discord.DiscordManager;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;

public class ReportCommand extends DLCmd {

	public ReportCommand() {
		super(null, "/report <player> [reason]");
	}
	
	@Override
	public void execute() {
		if(hasNoArgs()) {
			this.errorUsage();
			return;
		}
		if(!user.reportCooldown.isDone()) {
			user.send("report_cooldown", ""+user.reportCooldown.secondsLeft());
			return;
		}
		String name = this.args.get(0);
		String reason = null;
		if(hasArg(2)) {
			reason = String.join(" ", args.subList(1, args.size()));
		}
		
		String sender = user.getName();
		for(DLUser player : PlayerManager.getPlayers()) {
			if(player.checkPerm("deadlight.staff")) {
				if(reason != null) player.send("report_reason", sender, name, reason);
				else player.send("report", sender, name);
			}
		}
		user.send("thanks_for_report", name);
		Bukkit.getConsoleSender().sendMessage(name + " was reported by " + sender + " for '" + reason + "'");
		try {
			DiscordManager.sendStaffAlert("Report by " + user.getName(), "Offender: " + name + "\nReason: " + reason);
		}catch(Throwable t) {
			t.printStackTrace();
		}
		user.reportCooldown.start(10f);
	}
}
