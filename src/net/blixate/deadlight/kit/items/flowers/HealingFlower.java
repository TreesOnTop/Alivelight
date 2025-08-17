package net.blixate.deadlight.kit.items.flowers;

import java.util.HashMap;

import org.bukkit.Particle;

import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.ParticlesUtil;

public class HealingFlower extends FlowerTotem {
	public static final int RADIUS = 8;
	
	HashMap<DLUser, Integer> progress = new HashMap<>();
	
	public void tick(Lobby lobby) {
		for(DLUser user : lobby.getSurvivors()) {
			if(user.isMaxHealth()) continue;
			if(!progress.containsKey(user)) {
				progress.put(user, 0);
			}else {
				if(location.distance(user.getLocation()) < RADIUS) {
					progress.put(user, progress.get(user) + 1);
					ParticlesUtil.drawLineForPlayers(Particle.SCRAPE, location.clone().add(0.5, 0.5, 0.5), user.getLocation().add(0, 1, 0), 0.3, user.getPlayer());
					if(progress.get(user) > 25) {
						progress.put(user, 0);
						user.addHealth(2);
						user.send("flower_heal");
					}
				}
			}
		}
	}
}
