package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class SoulFinder extends Perk {
	
	public void activate(PerkEvent event) {
		DLUser user = event.getUserParam();
		user.glowToKiller(getTierProperty().getAsInt() * 1000);
	}
	
}
