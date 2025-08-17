package net.blixate.deadlight.maps.environment;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.maps.environment.states.BlockedState;
import net.blixate.deadlight.maps.environment.states.RegressingState;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.player.stats.DeadlightStat;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.time.Timer;
import net.blixate.deadlight.util.time.TimerListener;

public class PortalFragment extends SurvivorObjective implements TimerListener {
	
	public ArmorStand display = null;
	public String reviving = null;
	
	public Timer reviveTimer;
	
	public long lastProgressed; // the last time a survivor clicked this generator
	
	public PortalFragment(Lobby lobby, MapPosition pos) {
		super(lobby, pos, ObjType.GENERATOR);
	}
	
	public void tick(Lobby lobby) {
		Location loc = position.getLocation(offset).add(0.5, 0.75, 0.5);
		if(reviving != null) {
			display.setHeadPose(display.getHeadPose().add(0, 0.25f, 0));
			ParticlesUtil.spawnParticle(Particle.SOUL_FIRE_FLAME, loc.add(0,0.1,0), 5, 0.005, 0.25, 0.25, 0.25);
		}
		if(!lobby.isEndGame()) {
			if(progress < 100) {
				if(this.hasState(BlockedState.class)) {
					ParticlesUtil.spawnParticle(Particle.FLAME, loc, 5, 0.005, 0.25, 0.25, 0.25);
					updateText();
				} else {
					ParticlesUtil.spawnParticle(Particle.PORTAL, loc, 10, 0.005, 0.25, 0.25, 0.25);
					updateText();
				}
			}
			
			if(this.hasState(RegressingState.class) && progress > 5) {
				progress -= 1f/6f;
				ParticlesUtil.spawnParticle(Particle.SMOKE_NORMAL, loc, 10, 0.005, 0.25, 0.25, 0.25);
				updateText();
			}
		}
	}
	
	public void removeDisplayModel() {
		if(this.display != null)
			display.remove();
	}
	
	public boolean isReviving() {
		return reviving != null;
	}
	
	public void setReviving(String uuid, Lobby lobby) {
		reviving = uuid;
		if(uuid == null) {
			return;
		}
		Location loc = getPosition().getLocation(offset).add(0.5,0.2,0.5);
		final UUID ownerUUID = UUID.fromString(uuid);
		final OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
		World world = loc.getWorld();
		display = world.spawn(loc, ArmorStand.class, (entity) -> {
			entity.setBasePlate(false);
			entity.setVisible(false);
			entity.setGravity(false);
			entity.setSmall(true);
			entity.setInvulnerable(true);
			entity.setMarker(true);
			entity.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			entity.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING);
			entity.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING);
			entity.addEquipmentLock(EquipmentSlot.HAND, LockType.ADDING);
			entity.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING);
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta)head.getItemMeta();
			meta.setOwningPlayer(owner);
			head.setItemMeta(meta);
			entity.getEquipment().setHelmet(head);
			entity.addScoreboardTag("revivingHead");
			entity.getPersistentDataContainer().set(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER, lobby.randomEntityLobbyId);
		});
		reviveTimer = new Timer(this);
		reviveTimer.start(lobby.settings.reviveTime);
		this.lobby = lobby;
	}
	
	public void activateObjective(DLUser user) {
		user.genCooldown.start(user.getLobby().settings.genCooldown);
		user.addExp(5);
		user.addBloodSilent(2);
		lastProgressed = System.currentTimeMillis();
		if(this.hasState(RegressingState.class)) {
			this.removeAllStatesOfClass(RegressingState.class);
			Deadlight.getPerkManager().callEvent(user.getLobby().getKiller(), PerkEventType.REGRESSION_STOPPED, user);
			user.getStatTracker().incrementStat(DeadlightStat.REGRESSION_INTERRUPTS);
		}
	}
	
	public void destroy(DLUser player) {
		if(progress <= 0) {
			if(player != null) player.sendActionbar("actionbar_no_progress");
			return;
		}
		if(this.hasState(RegressingState.class)) {
			if(player != null) player.sendActionbar("killer_sabotage_regressing");
			return;
		}
		Vector half = new Vector(0.5, 0.5, 0.5);
		ParticlesUtil.spawnParticle(Particle.SMOKE_LARGE, this.getPosition().getLocation(offset).add(half), 20, 0, 0.5, 0.5, 0.5);
		progress -= lobby.settings.killerBreakAmount;
		this.addState(new RegressingState());
		if(progress < 0) {
			progress = 0;
		}
		if(player != null) {
			player.genCooldown.start(lobby.settings.killerBreakCooldown);
			player.addScoreEvent(new ScoreEvent("gen_sabotage", ScoreType.BLOOD));
			player.addExp(75);
			player.sendActionbar("actionbar_gen_destroy", new String[] {
					""+lobby.settings.killerBreakAmount,
					""+((int)progress)
			});
			Deadlight.getPerkManager().callEvent(player, PerkEventType.OBJECTIVE_BREAK, this);
		}
	}
	
	public void finish(Lobby lobby, boolean endGameFinish) {
		setType(Material.END_PORTAL_FRAME);
		removeHighlightShulker();
		this.states.clear();
		this.progress = 100f;
		if(!endGameFinish) {
			lobby.gensDone++;
			lobby.getKiller().send("killer_generator_completed");
			for(DLUser user : lobby.getSurvivors()) {
				user.send("survivor_generator_completed");
				user.addScoreEvent(new ScoreEvent("gen_complete", ScoreType.BLOOD));
				user.addExp(100);
			}
		}
	}

	@Override
	public void onForceStop() {
		removeDisplayModel();
		reviving = null;
		reviveTimer = null;
		display = null;
	}

	@Override
	public void onFinish(Timer timer) {
		if(!isReviving()) {
			Deadlight.debug("Timer finished but we aren't reviving anyone?");
			return;
		}
		DLUser user = PlayerManager.getUser(UUID.fromString(reviving));
		if(user == null) {
			onForceStop();
		}
		if(lobby.isSpectating(user)) {
			this.lobby.sendGlobal("player_soul_revive_global", PlayerSoul.getPlayerName(reviving));
			lobby.survivorRevive(PlayerManager.getUser(UUID.fromString(reviving)));
			lobby.revives ++;
			onForceStop();
		}
		
	}
	
	public boolean isActive() {
		return System.currentTimeMillis() < lastProgressed + (1000 * 10);
	}
	
	public long getLastProgressed() {
		return lastProgressed;
	}
}
