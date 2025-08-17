package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.MangledEffect;

public class Protector extends Perk {

	@Override
	public void activate(PerkEvent event) {
		if(!cooldown.isDone()) return;
		int healCount = 0;
		for(DLUser nearby : user.getLobby().getSurvivors()) {
			if(nearby.getLocation().distance(user.getLocation()) < 10) {
				if(!nearby.hasEffect(MangledEffect.class) && !nearby.equals(user) && !nearby.isKiller()) {
					nearby.addHealth(2);
					healCount ++;
				}
			}
		}
		if(healCount > 0) {
			cooldown.start(getTierProperty().getAsDouble());
			user.send("perk_protector_activate", "" + healCount);
		}
	}

}
