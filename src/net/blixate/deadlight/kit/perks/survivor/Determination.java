package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.effects.DeterminedEffect;

public class Determination extends Perk {
	
	private int totalHeals;
	
	@Override
	public void activate(PerkEvent event) {
		if(user.hasEffect(DeterminedEffect.class)) {
			return;
		}
		totalHeals++;
		if(totalHeals >= getTierProperty().getAsInt()) {
			totalHeals = 0;
			user.applyEffect(new DeterminedEffect());
		}
	}

}
