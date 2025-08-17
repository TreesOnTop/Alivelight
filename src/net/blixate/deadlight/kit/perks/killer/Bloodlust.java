package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;

public class Bloodlust extends Perk {

	public void activate(PerkEvent event) {
		if(event.getUserParam().mpd.revived) {
			return;
		}
		float percent = (getTierProperty().getAsFloat() / 100f);
		user.setSpeed(user.getSpeed() + (0.2f * percent));
	}
	
	public void event(PerkEvent event) {
		if(event.getType().equals(PerkEventType.LOBBY_START)) {
			user.setSpeed((float) (user.getSpeed() - ((float)0.2f * 0.07)));
		}
	}
}
