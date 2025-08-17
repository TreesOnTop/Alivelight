package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.effects.SpeedEffect;

public class Gust extends Perk {
	
	public void activate(PerkEvent event) {
		if(cooldown.isDone()) {
			user.applyEffect(new SpeedEffect(0.25f, getTierProperty().getAsInt() * 1000L));
			cooldown.start(getTierProperty().getAsDouble());
		}
	}
	
}
