package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.effects.ExhaustedEffect;
import net.blixate.deadlight.player.effects.MangledEffect;
import net.blixate.deadlight.player.effects.SpeedEffect;

public class Adrenaline extends Perk {
	
	public void activate(PerkEvent event) {
		user.addHealth(2);
		user.removeEffect(MangledEffect.class);
		user.removeEffect(ExhaustedEffect.class);
		user.applyEffect(new SpeedEffect(0.5f, 5000));
		user.applyEffect(new ExhaustedEffect(1000 * getTierProperty().getAsInt()));
	}
}
