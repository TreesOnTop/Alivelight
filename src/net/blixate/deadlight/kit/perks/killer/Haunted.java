package net.blixate.deadlight.kit.perks.killer;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.DLUser;

public class Haunted extends Perk {

	public void activate(PerkEvent event) {
		PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 20 * getTierProperty().getAsInt(), 0, false, false, true);
		for(DLUser survivor : user.getLobby().getSurvivors()) {
			if(survivor.getPlayer().hasPotionEffect(PotionEffectType.SLOW)) {
				survivor.getPlayer().removePotionEffect(PotionEffectType.SLOW);
			}
			survivor.getPlayer().addPotionEffect(effect);
		}
	}

}
