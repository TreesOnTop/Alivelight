package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class Marked extends Perk {

	public void activate(PerkEvent event) {
		for(DLUser survivor : user.getLobby().getSurvivors()) {
			survivor.glowToKiller(getTierProperty().getAsInt() * 1000L);
		}
	}
	
}
