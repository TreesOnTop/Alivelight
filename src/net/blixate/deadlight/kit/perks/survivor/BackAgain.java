package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.effects.ExhaustedEffect;
import net.blixate.deadlight.player.effects.SpeedEffect;

public class BackAgain extends Perk {

	@Override
	public void activate(PerkEvent event) {
		user.addHealth(4);
		user.applyEffect(new SpeedEffect(0.5f, 20000));
		user.applyEffect(new ExhaustedEffect(1000 * getTierProperty().getAsInt()));
	}

}
