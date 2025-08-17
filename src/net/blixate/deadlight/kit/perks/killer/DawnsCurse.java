package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class DawnsCurse extends Perk {
	
	boolean hasActivated = false;
	
	public void activate(PerkEvent event) {
		if(user.getLobby().isEndGame()) return;
		for(DLUser user : user.getLobby().getSurvivors()) {
			if(!user.getMatchData().hasTakenDamage) {
				return;
			}
		}
		if(!hasActivated) {
			user.getLobby().setGeneratorsBlocked((long)(getTierProperty().getAsDouble() * 1000L));
			user.getLobby().sendGlobal("perk_dawnscurse_activate");
			hasActivated = true;
		}
	}
}
