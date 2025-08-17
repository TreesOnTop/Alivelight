package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;

public class Looter extends Perk {

	@Override
	public void activate(PerkEvent event) {
		float percent = (getTierProperty().getAsFloat() / 100f);
		getData().bloodMultiplier += percent;
	}

}
