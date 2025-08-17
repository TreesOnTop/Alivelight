package net.blixate.deadlight.kit.perks.survivor;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.player.effects.BrokenEffect;
import net.blixate.deadlight.player.effects.ExhaustedEffect;

public class Camouflage extends Perk {

	@Override
	public void activate(PerkEvent event) {
		if(user.getPlayer().isSneaking() && !user.hasEffect(ExhaustedEffect.class)) {
			PotionEffect effect = new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 8, 0, false, false, false);
			user.getPlayer().addPotionEffect(effect);
			user.applyEffect(new BrokenEffect(8000));
			user.applyEffect(new ExhaustedEffect(this.getTierProperty().getAsInt() * 1000));
		}
	}
}
