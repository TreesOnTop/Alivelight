package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;

public class Miserly extends Perk {

	@Override
	public void activate(PerkEvent event) {
		float percent = (getTierProperty().getAsFloat() / 100f);
		getData().bloodMultiplier += percent;
		getData().itemDropChance += 33;
	}

}
