package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class Survelliance extends Perk {
	
	public void activate(PerkEvent event) {
		if(cooldown.isDone()) {
			DLUser target = event.getUserParam();
			target.glowToKiller((long)(getTierProperty().getAsDouble() * 1000L));
			cooldown.start(10);
		}
	}
}
