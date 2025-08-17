package net.blixate.deadlight.kit.perks.survivor;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;

public class FleetFooted extends Perk {
	static AttributeModifier HEALTH_MODIFIER = new AttributeModifier("player_health", -2, Operation.ADD_NUMBER);
	
	public void activate(PerkEvent event) {
		user.applyModifier(Attribute.GENERIC_MAX_HEALTH, HEALTH_MODIFIER);
		float percent = 0.2f * (getTierProperty().getAsFloat() / 100f);
		user.getPlayer().setWalkSpeed(user.getPlayer().getWalkSpeed() + percent);
	}
}
