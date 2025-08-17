package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.effects.UndetectableEffect;

public class CreepingHaste extends Perk {
	
	@Override
	public void activate(PerkEvent event) {
		if(cooldown.isDone()) {
			user.applyEffect(new UndetectableEffect(20000));
			cooldown.start(20 + getTierProperty().getAsDouble());
		}
	}
}
