package net.blixate.deadlight.kit.perks.survivor;

import org.bukkit.util.Vector;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;

public class Coward extends Perk {
	public void activate(PerkEvent event) {
		if(cooldown.isDone() && !user.getLobby().getKiller().getPlayer().isSneaking()) {
			double maxSpeed = 2d;
			double percent = (getTierProperty().getAsDouble() / 100);
			Vector dir = user.getDirection().normalize();
			dir = dir.multiply(maxSpeed * percent);
			dir = dir.setY(0.2f);
			user.getLobby().getKiller().addVelocity(dir);
			cooldown.start(10f);
		}
	}
}
