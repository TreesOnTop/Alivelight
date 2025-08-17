package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;

public class LeftForDead extends Perk {

	public void activate(PerkEvent event) {
		user.setSpeed(user.getSpeed() + (0.2f * ((float)(getTierProperty().getAsInt())/100)));
		user.send("perk_leftfordead_activate");
	}

}
