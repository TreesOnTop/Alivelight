package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class NOED extends Perk {
	
	public void activate(PerkEvent event) {                                                
		for(DLUser surv : user.getLobby().getSurvivors()) {
			surv.glowToKiller(10000);
		}
		user.getLobby().settings.reviveTime += getTierProperty().getAsDouble();
	}
}
