package net.blixate.deadlight.player.stats;

import java.util.List;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.challenges.AssignedChallenge;
import net.blixate.deadlight.challenges.JsonChallenge;
import net.blixate.deadlight.kit.items.Items;
import net.blixate.deadlight.player.DLUser;

public class StatTracker {
	
	private DLUser data;
	
	public StatTracker(DLUser data) {
		this.data = data;
	}
	
	public void incrementStat(DeadlightStat stat) {
		/*if(data.getLobby().requiredGens < 5) {
			return;
		}*/
		if(data.isInMatch()) {
			if(!data.getLobby().canTrackChallenges) {
				return;
			}
		}
		Deadlight.debug("Incremented Stat " + stat.name());
		int i = 0;
		for(AssignedChallenge challenge : data.dailyChallenges) {
			if(challenge == null) continue;
			JsonChallenge json = Deadlight.getChallengeManager().getChallengeById(challenge.category, challenge.id);
			if(json.getStat().equals(stat)) {
				challenge.progress ++;
				if(challenge.progress == challenge.requirement) {
					data.send("daily_completed", "Challenge #" + i);
				}
			}
			i++;
		}
	}
	
	public void trackPerkUse(List<String> perks) {
		
	}
	
	public void trackItemUse(Items item) {
		
	}
}
