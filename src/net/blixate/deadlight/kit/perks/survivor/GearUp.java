package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class GearUp extends Perk {

	@Override
	public void activate(PerkEvent event) {
		if(user.isObsession()) {
			return;
		}
		DLUser obsession = user.getLobby().getObsession();
		double effect = getTierProperty().getAsDouble() / 100d;
		if(obsession.hasPerk(getRegistry())) {
			effect *= 2;
		}
		user.getLobby().getObsession().getMatchData().uncursingSpeed += effect;
	}
}
