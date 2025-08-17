package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;

public class LeafWrap extends Perk {
	
	public void activate(PerkEvent event) {
		int chance = getTierProperty().getAsInt();
		user.getPlayerLoop().setBleedingChance(100-chance);
	}
	
}
