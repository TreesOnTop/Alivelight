package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.player.effects.SpeedEffect;

public class ThirstForBlood extends Perk {
	
	int tokens;
	
	public void activate(PerkEvent event) {
		if(tokens < 10) {
			tokens++;
		}
	}
	
	public void event(PerkEvent event) {
		if(event.getType().equals(PerkEventType.END_GAME_COLLAPSE_START)) {
			if(tokens == 0) {
				return;
			}
			user.applyEffect(new SpeedEffect((tokens * getTierProperty().getAsInt())/100f, -1));
		}
	}
	
}
