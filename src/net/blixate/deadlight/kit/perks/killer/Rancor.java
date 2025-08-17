package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.ExposedEffect;

public class Rancor extends Perk {
	
	public void activate(PerkEvent event) {
		user.glowToEntity(getTierProperty().getAsInt()*1000, user.getLobby().getObsession().getPlayer());
		for(DLUser surv : user.getLobby().getSurvivors()) {
			surv.glowToKiller(3000);
		}
	}
	
	public void event(PerkEvent e) {
		if(e.getType().equals(PerkEventType.ALL_OBJECTIVES_COMPLETED)) {
			Deadlight.debug("Perk event was called");
			DLUser obsession = user.getLobby().getObsession();
			if(obsession != null && !obsession.isSpectating()) {
				obsession.applyEffect(new ExposedEffect());
			}
		}
	}
}
