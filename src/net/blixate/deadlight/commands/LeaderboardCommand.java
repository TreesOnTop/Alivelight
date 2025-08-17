package net.blixate.deadlight.commands;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.leaderboard.Leaderboard;
import net.blixate.deadlight.leaderboard.Leaderboard.LeaderboardMember;
import net.blixate.deadlight.player.DLUser;

public class LeaderboardCommand extends DLCmd {
	
	@Override
	public void execute() {
		if(hasArg(1) && user.checkPerm("deadlight.leaderboard")) {
			if(args.get(0).equalsIgnoreCase("clear")) {
				Deadlight.getLeaderboard().cache = null;
			}
			if(args.get(0).equalsIgnoreCase("size")) {
				user.getPlayer().sendMessage("Leaderboard is " + Deadlight.getLeaderboard().cache.size() + " members long.");
			}
			if(args.get(0).equalsIgnoreCase("update")) {
				Deadlight.getLeaderboard().findEligible();
			}
		}
		Deadlight.getLeaderboard().checkCache();
		sendLeaderboardMessage(user);
	}
	
	public void sendLeaderboardMessage(DLUser user) {
		user.send("leaderboard_header");
		for(int i = 0; i < Math.min(Deadlight.getLeaderboard().cache.size(), Leaderboard.TOP_RESULTS); i++) {
			if(i >= Deadlight.getLeaderboard().cache.size()) {
				break;
			}
			LeaderboardMember data = Deadlight.getLeaderboard().cache.get(i);
			user.send("leaderboard_line", "" + (i+1), data.getName(), ""+data.level, ""+data.prestige);
		}
		user.send("leaderboard_footer");
	}
}
