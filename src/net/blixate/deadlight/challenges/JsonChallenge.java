package net.blixate.deadlight.challenges;

import net.blixate.deadlight.player.stats.DeadlightStat;

public class JsonChallenge {
	
	String text;
	String stat;
	int req;
	int reward = 20;
	
	int index;
	
	public JsonChallenge(int challengeId, String text, String stat, int req) {
		this.text = text;
		this.stat = stat;
		this.req = req;
		this.index = challengeId;
	}
	
	public String getText() {
		return this.text.replace("$", "" + req);
	}
	
	public DeadlightStat getStat() {
		return DeadlightStat.valueOf(stat);
	}
	
	public int getRequirement() {
		return req;
	}

	public int getReward() {
		return reward;
	}
	
}
