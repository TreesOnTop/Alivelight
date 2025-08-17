package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.maps.environment.MapObject;
import net.blixate.deadlight.maps.environment.SurvivorObjective;
import net.blixate.deadlight.maps.environment.states.BlockedState;

public class BloodWarden extends Perk {
	
	boolean hasActivated = false;
	
	public void activate(PerkEvent event) {
		if(user.getLobby().isEndGame()) {
			if(!hasActivated) {
				Lobby lobby = user.getLobby();
				for(MapObject portal : lobby.getPortals()) {
					SurvivorObjective obj = (SurvivorObjective)portal;
					obj.addState(new BlockedState((long)(getTierProperty().getAsDouble() * 1000L)));
				}
				hasActivated = true;
				lobby.sendGlobal("perk_warden_activate");
			}
		}
	}

}
