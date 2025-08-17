package net.blixate.deadlight.kit.perks.killer;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;

public class Combo extends Perk {

	@Override
	public void activate(PerkEvent event) {
		user.hitCooldown.stop();
		user.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
		user.getPlayer().removePotionEffect(PotionEffectType.SLOW);
		user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * getTierProperty().getAsInt(), 0, false, false, true));
	}

}
