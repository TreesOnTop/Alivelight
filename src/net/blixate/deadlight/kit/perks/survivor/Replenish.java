package net.blixate.deadlight.kit.perks.survivor;

import org.bukkit.Sound;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.util.time.Cooldown;

public class Replenish extends Perk {

	boolean activated = false;
	boolean usedItem = false;
	Cooldown regainCooldown = new Cooldown();
	
	@Override
	public void activate(PerkEvent event) {
		usedItem = true;
		this.regainCooldown.start(this.getTierProperty().getAsInt());
	}
	
	public void tick() {
		if(activated) {
			return;
		}
		if(usedItem && regainCooldown.isDone()) {
			if(user.equippedItem == null) {
				user.send("perk_replenish_no_item");
			}
			Lobby.equipSurvivor(user);
			this.activated = true;
			user.playSound(Sound.BLOCK_BREWING_STAND_BREW, 1);
		}
	}

}
