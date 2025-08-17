package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class GearUp extends Perk {

	@Override
	public void activate(PerkEvent event) {
		// Only non-obsession players provide speed boost to obsession
		if(user.isObsession()) {
			return;
		}
		
		// Get the current obsession
		if(user.getLobby() == null) {
			System.err.println("GearUp: Player " + user.getName() + " has no lobby");
			return;
		}
		
		DLUser obsession = user.getLobby().getObsession();
		if(obsession == null) {
			System.err.println("GearUp: No obsession found for player " + user.getName() + " in lobby");
			return;
		}
		
		// Calculate the speed effect
		double effect = getTierProperty().getAsDouble() / 100d;
		boolean obsessionHasGearUp = obsession.hasPerk(getRegistry());
		
		// If obsession also has GearUp, double the effect from this player
		if(obsessionHasGearUp) {
			effect *= 2;
		}
		
		// Apply the effect to obsession's speed
		if(obsession.getMatchData() == null) {
			System.err.println("GearUp: Obsession " + obsession.getName() + " has no match data");
			return;
		}
		
		double oldSpeed = obsession.getMatchData().uncursingSpeed;
		obsession.getMatchData().uncursingSpeed += effect;
		
		System.out.println("GearUp: " + user.getName() + " (tier " + getTier() + ") gave " + obsession.getName() + 
			" +" + String.format("%.1f", effect * 100) + "% speed (obsession has GearUp: " + obsessionHasGearUp + 
			", speed: " + String.format("%.3f", oldSpeed) + " -> " + String.format("%.3f", obsession.getMatchData().uncursingSpeed) + ")");
	}
}
