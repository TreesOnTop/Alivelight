package net.blixate.deadlight.kit.items.flowers;

import java.util.HashSet;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.DLUser;

public class KnowledgeFlower extends FlowerTotem {
	public static final int RADIUS = 12;
	
	public static final double UNCURSING = 0.25;
	public static final double HEALING = 0.50;
	
	// used to keep track of who we gave the healing buff to
	HashSet<DLUser> affected = new HashSet<>();
	
	public void tick(Lobby lobby) {
		try {
			for(DLUser user : lobby.getSurvivors()) {
				if(location.distance(user.getLocation()) <= RADIUS && !affected.contains(user)) {
					affected.add(user);
					user.mpd.healingSpeed += HEALING;
					user.mpd.uncursingSpeed += UNCURSING;
					user.sendActionbar("flower_healing_speed");
				}else if(affected.contains(user) && location.distance(user.getLocation()) > RADIUS) {
					user.mpd.healingSpeed -= HEALING;
					user.mpd.uncursingSpeed -= UNCURSING;
					user.sendActionbar("flower_healing_speed_left");
					affected.remove(user);
				}
			}
		}catch(Throwable t) {
			Deadlight.error(t);
		}
	}
	
	public void destroy() {
		for(DLUser user : affected) {
			user.mpd.healingSpeed -= HEALING;
			user.mpd.uncursingSpeed -= UNCURSING;
		}
		affected.clear();
	}
}
