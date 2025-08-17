package net.blixate.deadlight.kit.type;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.util.time.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class JackOLanternType extends KillerType {
	
	SkeletonHorse joustingHorse;
	Cooldown abilityCooldown = new Cooldown();
	
	boolean ridingHorse = false;
	
	@Override
	public void equipKiller(PlayerInventory inv) {
		quickEquip(user, inv);
		ItemStack item = new ItemStack(Material.LEATHER_HORSE_ARMOR);
		LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Summon the Dark Steed" + INPUT_TYPE_RIGHT_CLICK);
		meta.setColor(Color.BLACK);
		item.setItemMeta(meta);
		inv.setItem(1, item);
	}
	
	@Override
	public void rightClickItem(ItemStack item, Block clickedBlock, BlockFace face) {
		if(!item.getType().equals(Material.LEATHER_HORSE_ARMOR)) {
			return;
		}
		// we are already riding the horse...
		if(user.getPlayer().isInsideVehicle()) {
			return;
		}
		if(!abilityCooldown.isDone()) {
			user.sendActionbar("ability_cooldown", abilityCooldown.secondsLeft());
			return;
		}
		// We didn't dismount the horse properly? Delete the old one!!!
		if(joustingHorse != null) {
			joustingHorse.remove();
		}
		World world = user.getLocation().getWorld(); 
		joustingHorse = world.spawn(user.getLocation(), SkeletonHorse.class, (entity) -> {
			entity.getInventory().setSaddle(new ItemStack(Material.SADDLE));
			entity.setTamed(true);
			entity.setAdult();
			entity.setAI(true);
			
			double movementSpeed = 0.2;
			double jumpHeight = 1.5;
			if(hasAddon("noble_steed")) {
				movementSpeed += 0.1;
				jumpHeight += 0.25;
			}
			if(hasAddon("engraved_horse_armor")) {
				((HorseInventory)entity.getInventory()).setArmor(new ItemStack(Material.IRON_HORSE_ARMOR));
				movementSpeed -= 0.05;
			}
			entity.setJumpStrength(jumpHeight);
			entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(movementSpeed);
			entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2);
			if(hasAddon("horse_muzzle")) {
				entity.setSilent(true);
			}
			entity.getPersistentDataContainer().set(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER, user.getLobby().randomEntityLobbyId);
		});
		joustingHorse.addPassenger(user.getPlayer());
		ridingHorse = true;
		world.playSound(joustingHorse.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
		world.spawnParticle(Particle.SMOKE_NORMAL, user.getLocation(), 50, 1, 1, 1, 0.05f);
	}
	
	public void cleanup() {
		if(joustingHorse != null) {
			joustingHorse.remove();
		}
	}
	
	public void tick() {
		if(ridingHorse && hasAddon("horse_hunter")) {
			Location closest = null;
			for(DLUser surv : user.getLobby().getSurvivors()) {
				Location loc = surv.getLocation();
				if(closest == null) {
					closest = loc;
					continue;
				}else {
					if(loc.distance(user.getLocation()) < closest.distance(user.getLocation())) {
						closest = loc;
					}
				}
			}
			user.getPlayer().playSound(closest, Sound.BLOCK_NOTE_BLOCK_BASS, 2, 1);
		}
	}
	
	public void playToAll(Sound sound, float pitch) {
		Lobby lobby = user.getLobby();
		for(DLUser user : lobby.getPlayers()) {
			user.playSound(sound, pitch);
		}
	}
	
	public void perkEvent(PerkEvent event) {
		if(event.getType().equals(PerkEventType.SURVIVOR_REVIVE)) {
			playToAll(Sound.ENTITY_VEX_CHARGE, 0);
		}
		
		if(event.getType().equals(PerkEventType.OBJECTIVE_BREAK)) {
			playToAll(Sound.ENTITY_VEX_CHARGE, 1);
		}
		
		if(event.getType().equals(PerkEventType.SURVIVOR_DEATH)) {
			playToAll(Sound.ENTITY_RAVAGER_CELEBRATE, 1);
		}
		
		if(event.getType().equals(PerkEventType.ATTEMPT_PLAYER_HIT)) {
			if(hasAddon("lunatic")) {
				if(ridingHorse) {
					DLUser victim = event.getUserParam();
					victim.applyEffect(new InfectedEffect(10000));
				}
			}
		}
		if(event.getType().equals(PerkEventType.SURVIVOR_HIT)) {
			if(ridingHorse) {
				dismount(user.getPlayer().getVehicle());
				playToAll(Sound.ENTITY_WITCH_CELEBRATE, 2);
			}
		}
		if(event.getType().equals(PerkEventType.KILLER_PUNCH)) {
			if(ridingHorse) {
				Entity vehicle = user.getPlayer().getVehicle();
				if(vehicle instanceof SkeletonHorse) {
					dismount(vehicle);
					user.hitCooldown.start(5f);
					user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, true, true, true));
					ridingHorse = false;
					event.getUserParam().addScoreEvent(new ScoreEvent("jacko_horse_destroy", ScoreType.BLOOD));
				}
				
			}
		}
	}
	
	public void survivorHitEntity(Entity entity, DLUser survivor) {
		if(entity instanceof SkeletonHorse) {
			if(!hasAddon("engraved_armor")) {
				dismount(entity);
				user.hitCooldown.start(3f);
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, true, true, true));
				ridingHorse = false;
				survivor.addScoreEvent(new ScoreEvent("jacko_horse_destroy", ScoreType.BLOOD));
			}
		}
	}
	
	public void dismount(Entity entity) {
		if(entity instanceof SkeletonHorse) {
			entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 2);
			entity.remove();
			ridingHorse = false;
			if(hasAddon("conquest")) {
				abilityCooldown.start(10f);
			}else {
				abilityCooldown.start(15f);
			}
			user.hitCooldown.start(0.5f);
			user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1, true, true, true));
		}
	}
	
	public boolean isRidingHorse() {
		return ridingHorse;
	}
	
	public boolean canAttack() {
		if(hasAddon("lunatic")) {
			return !ridingHorse;
		}
		return true;
	}
	
	public int getDefaultTerrorRadius() {
		if(ridingHorse && !hasAddon("padded_horse_shoe")) {
			return 32;
		}else {
			return 24;
		}
	}
}
