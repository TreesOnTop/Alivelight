package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.player.effects.UndetectableEffect;

public class Stalker extends Perk {
	
	long uid = 0;
	
	@Override
	public void activate(PerkEvent event) {
		UndetectableEffect effect = new UndetectableEffect(getTierProperty().getAsInt() * 1000);
		user.applyEffect(effect);
		uid = effect.getId();
		if(user.getLobby().getObsession() != null) {
			user.getLobby().getObsession().glowToKiller(20000);
		}else {
			user.send("perk_stalker_cant_activate");
		}
	}
	
	public void event(PerkEvent event) {
		if(event.getType().equals(PerkEventType.SURVIVOR_HIT)) {
			user.removeEffectWithId(uid);
		}
	}

}
