package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.effects.MangledEffect;

public class Selfless extends Perk {
	@Override
	public void activate(PerkEvent event) {
		if(cooldown.isDone() && !user.hasEffect(MangledEffect.class)) {
			getData().survivorHealingProgress += 50f;
			if(getData().survivorHealingProgress >= 100) {
				user.addHealth(2);
				getData().survivorHealingProgress -= 100;
				user.sendActionbar("actionbar_healed_self");
			}
			cooldown.start(getTierProperty().getAsDouble());
		}
	}
}
