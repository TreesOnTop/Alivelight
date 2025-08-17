package net.blixate.deadlight.player.effects;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.lobby.AttackType;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.time.Timer;

public class KnockedEffect extends Effect {

	ArmorStand entity;
	
	// make sure they don't phase into the fucking ground (WHY IS THIS THE CASE AAA)
	Location returnLocation;
	
	public KnockedEffect(int i) {
		this.duration = i;
	}

	@Override
	public void apply(DLUser user) {
		returnLocation = user.getLocation();
		Location loc = user.getLocation().clone();
		loc = loc.subtract(0, 0.75, 0);
		entity = Deadlight.getWorld().spawn(loc, ArmorStand.class, (e) -> {
			e.setSmall(true);
			e.setAI(false);
			e.setInvisible(true);
			e.setInvulnerable(true);
			e.setBasePlate(false);
			e.setGravity(false);
			e.getPersistentDataContainer().set(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER, user.getLobby().randomEntityLobbyId);
		});
		entity.addPassenger(user.getPlayer());
		user.sendTitle("knocked_title", "knocked_subtitle");
		user.send("knocked");
		user.getLobby().getSurvivors().forEach((e) -> {
			if(e.getUUID().equals(user.getUUID())) return;
			e.send("knocked_other", user.getName());
		});
	}

	@Override
	public void remove(DLUser user) {
		Entity vehicle = user.getPlayer().getVehicle();
		if(vehicle != null) {
			if(vehicle.getType() == EntityType.ARMOR_STAND) {
				vehicle.removePassenger(user.getPlayer());
				vehicle.remove();
			}
			user.getPlayer().teleport(returnLocation);
		}
	}
	
	// If the survivor is picked up, we don't deal damage
	@Override
	public void onForceStop() {
		removeEffect();
	}

	// If their timer expires, we damage them.
	@Override
	public void onFinish(Timer timer) {
		removeEffect();
		user.activeEffects.remove(this);
		if(Deadlight.inst.getConfig().getBoolean("global.enable knockdown damage")) {
			user.getLobby().lobbyDamage(2, null, user, AttackType.TRAP);
			user.applyEffect(new DeterminedEffect(10000));
		}
	}
	
}
