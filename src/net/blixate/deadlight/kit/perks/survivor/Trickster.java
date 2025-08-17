package net.blixate.deadlight.kit.perks.survivor;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.lobby.AttackType;
import net.blixate.deadlight.player.effects.ExposedEffect;
import net.blixate.deadlight.player.effects.MangledEffect;

public class Trickster extends Perk {

	boolean hasActivated = false;
	
	@Override
	public void activate(PerkEvent event) {
		if(!hasActivated && !user.hasEffect(ExposedEffect.class)) {
			Object attackTypeObject = event.getParams()[0];
			AttackType attack = (AttackType)attackTypeObject;
			if(attack == AttackType.BASIC) {
				user.getPlayer().setHealth(4);
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 5, 0, false, false, false));
				user.applyEffect(new MangledEffect(getTierProperty().getAsInt() * 1000));
				hasActivated = true;
			}
		}
	}

}
