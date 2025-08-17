package net.blixate.deadlight.maps.environment;

import org.bukkit.Location;
import org.bukkit.Particle;

import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.MatchSettings;
import net.blixate.deadlight.maps.environment.states.BlockedState;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.ParticlesUtil;

public class ExitPortal extends SurvivorObjective {

	public ExitPortal(Lobby lobby, MapPosition pos) {
		super(lobby, pos, ObjType.PORTAL);
	}
	
	public void tick(Lobby lobby) {
		Location loc = position.getLocation(offset).add(0.5, 0.75, 0.5);
		if(lobby.isEndGame()) {
			if(this.hasState(BlockedState.class)) {
				ParticlesUtil.spawnParticle(Particle.FLAME, loc, 10, 0.005, 0.25, 0.25, 0.25);
			}else {
				ParticlesUtil.spawnParticle(Particle.END_ROD, loc, 10, 0.005, 0.25, 0.25, 0.25);
			}
		}
	}
	
	public void activateObjective(DLUser user) {
		MatchSettings settings = user.getLobby().settings;
		cool.start(settings.portalCooldown);
		user.addExp(10);
		user.addBloodSilent(4);
	}
}
