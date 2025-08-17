package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.lobby.score.ScoreEvent;

public class HealthyObsession extends Perk {

	@Override
	public void activate(PerkEvent event) {
		if(user.isObsession()) {
			user.addHealth(2);
		}
	}
	
	public void event(PerkEvent event) {
		if(event.getType().equals(PerkEventType.SCORE_EVENT)) {
			ScoreEvent scoreEvent = (ScoreEvent) event.getParams()[0];
			if(scoreEvent.getName().equals("obsession_healed")) {
				scoreEvent.addMultiplier(getTierProperty().getAsFloat() / 100f);
			}
		}
	}

}
