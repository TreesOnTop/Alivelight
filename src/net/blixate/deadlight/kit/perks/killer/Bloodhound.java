package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class Bloodhound extends Perk {

	@Override
	public void activate(PerkEvent event) {
		for(DLUser user : user.getLobby().getSurvivors()) {
			int level = getTierProperty().getAsInt();
			if(level >= 0) user.getPlayerLoop().bloodSize += 2;
			if(level >= 1) user.getPlayerLoop().setBloodMode(true, false);
			if(level >= 2) user.getPlayerLoop().setBloodMode(false, false);
		}
	}
	
}
