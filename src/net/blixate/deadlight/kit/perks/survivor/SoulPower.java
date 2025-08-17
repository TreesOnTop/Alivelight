package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.effects.SpeedEffect;

public class SoulPower extends Perk {

	public void setup() {
		user.mpd.soulExpireTime += getTierProperty().getAsInt();
	}
	
	@Override
	public void activate(PerkEvent event) {
		event.getUserParam().applyEffect(new SpeedEffect(0.5f, 30000));
	}

}
