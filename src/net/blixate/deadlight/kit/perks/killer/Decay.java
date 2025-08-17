package net.blixate.deadlight.kit.perks.killer;

import org.bukkit.Location;
import org.bukkit.Particle;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.maps.environment.MapObject;
import net.blixate.deadlight.maps.environment.PortalFragment;
import net.blixate.deadlight.maps.environment.states.RegressingState;
import net.blixate.deadlight.util.ParticlesUtil;

public class Decay extends Perk {
	
	public void activate(PerkEvent event) {}
	
	public void tick() {
		Lobby lobby = user.getLobby();
		for(MapObject obj : lobby.getGenerators()) {
			PortalFragment gen = (PortalFragment)obj;
			if(!isActive(gen) && gen.progress > 50 && gen.getProgress() != 100) {
				gen.progress -= getTierProperty().getAsFloat();
				if(!gen.hasState(RegressingState.class)) {
					gen.addState(new RegressingState());
				}
				if(gen.progress < 0) {
					gen.progress = 0;
					continue;
				}
				Location loc = gen.getPosition().getLocation(lobby.getMapLocation()).add(0.5, 1.5, 0.5);
				ParticlesUtil.spawnParticle(Particle.SPELL_WITCH, loc, 5, 0, 1, 1, 1);
			}
		}
	}
	
	private boolean isActive(PortalFragment gen) {
		return System.currentTimeMillis() < gen.lastProgressed + (1000 * 20);
	}
}
