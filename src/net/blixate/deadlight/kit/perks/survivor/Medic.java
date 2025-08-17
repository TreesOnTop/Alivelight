package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.BrokenEffect;

public class Medic extends Perk {
	
	public void activate(PerkEvent event) {
		getData().healingSpeed += (float) ((double)getTierProperty().getAsInt()) / 100d;
		user.applyEffect(new BrokenEffect());
	}
	
	public void event(PerkEvent event) {
		if(event.getType().equals(PerkEventType.SURVIVOR_HEAL)) {
			DLUser healed = event.getUserParam();
			healed.addHealth(2);
		}
	}

}
