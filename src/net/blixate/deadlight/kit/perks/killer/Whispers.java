package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;

public class Whispers extends Perk {

	@Override
	public void activate(PerkEvent event) {
		user.getLobby().whispers = getTierProperty().getAsInt();
	}
	
}
