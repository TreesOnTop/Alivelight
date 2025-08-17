package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.maps.environment.MapObject;
import net.blixate.deadlight.maps.environment.SurvivorObjective;
import net.blixate.deadlight.player.DLUser;

public class NoWayOut extends Perk {
	
	public static final int MAX_TOKENS = 3;
	int tokens = 0;
	boolean activated = false;
	
	@Override
	public void activate(PerkEvent event) {
		if(user.getLobby().isEndGame()) {
			return;
		}
		if(tokens < MAX_TOKENS)
			tokens++;
	}
	
	public void event(PerkEvent event) {
		if(event.getType().equals(PerkEventType.LOBBY_START)) {
			if(user.getLobby().getObsession() == null) {
				user.send("perk_nowayout_cant_activate");
			}
		}
		if(event.getType().equals(PerkEventType.END_GAME_COLLAPSE_START)) {
			runDebuff();
		}
		if(event.getType().equals(PerkEventType.SURVIVOR_DEATH)) {
			DLUser victim = event.getUserParam();
			if(victim.isObsession() && !victim.isRevivePossible()) {
				user.send("perk_nowayout_cant_activate");
			}
		}
	}
	
	public void runDebuff() {
		if(activated) {
			return;
		}
		// check the obsession is alive
		DLUser obs = user.getLobby().getObsession();
		if(obs == null) return;
		if(!obs.isRevivePossible()) {
			return;
		}
		
		for(MapObject object : user.getLobby().getPortals()) {
			SurvivorObjective portal = (SurvivorObjective)object;
			portal.objectiveSpeedMultiplier -= (float)((getTierProperty().getAsFloat()/100f) * (float)tokens);
		}
		activated = true;
	}
	
}
