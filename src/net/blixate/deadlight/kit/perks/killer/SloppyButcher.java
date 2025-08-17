package net.blixate.deadlight.kit.perks.killer;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.HemorrhageEffect;
import net.blixate.deadlight.player.effects.MangledEffect;

public class SloppyButcher extends Perk {
	
	@Override
	public void activate(PerkEvent event) {
		if(event.getParams().length < 1) {
			return;
		}
		DLUser victim = event.getUserParam();
		victim.applyEffect(new MangledEffect());
		victim.applyEffect(new HemorrhageEffect(1, getTierProperty().getAsInt() * 1000));
		victim.send("mangled");
	}
}
