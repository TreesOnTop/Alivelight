package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.effects.SpeedEffect;

public class Hope extends Perk {
	public void activate(PerkEvent event) {
		user.applyEffect(new SpeedEffect(0.07f, 1000 * getTierProperty().getAsInt()));
	}
}
