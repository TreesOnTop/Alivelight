package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class Radar extends Perk {
	
	public void activate(PerkEvent event) {
		if(cooldown.isDone()) {
			DLUser killer = user.getLobby().getKiller();
			killer.glowToSurvivors(5000L);
			cooldown.start(getTierProperty().getAsDouble());
		}
	}
	
}
