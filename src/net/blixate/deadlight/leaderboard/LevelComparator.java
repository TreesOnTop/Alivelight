package net.blixate.deadlight.leaderboard;

import java.util.Comparator;

import net.blixate.deadlight.leaderboard.Leaderboard.LeaderboardMember;
import net.blixate.deadlight.player.DLUser;

public class LevelComparator implements Comparator<LeaderboardMember> {
	public int compare(LeaderboardMember o1, LeaderboardMember o2) {
		final int totalLevel1 = o1.level + (DLUser.MAX_LEVEL * o1.prestige);
		final int totalLevel2 = o2.level + (DLUser.MAX_LEVEL * o2.prestige);
		return totalLevel1 < totalLevel2 ? 1 : totalLevel1 > totalLevel2 ? -1 : 0; // we dont want these to be the same, but whatever
	}
}