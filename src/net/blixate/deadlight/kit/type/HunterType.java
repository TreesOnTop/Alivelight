package net.blixate.deadlight.kit.type;

import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.lobby.AttackType;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.player.effects.HemorrhageEffect;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.player.effects.MangledEffect;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.time.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class HunterType extends KillerType {
	
	Cooldown cooldown;
	Cooldown reloading;
	boolean isReloading;
	int maxAmmo = 6;
	int ammo = maxAmmo;
	int range = 32;
	int damage = 1;
	float shootingCooldown = 0.5f;
	double recoil = 0;
	float reloadTime = 4;
	int ammoEmptySeconds = 0;
	
	@Override
	public void equipKiller(PlayerInventory inv) {
		quickEquip(user, inv);
		cooldown = new Cooldown();
		reloading = new Cooldown();
		ItemStack item = new ItemStack(Material.IRON_HORSE_ARMOR);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Revolving Justice" + INPUT_TYPE_RIGHT_CLICK);
		meta.setCustomModelData(1);
		if(hasAddon("50cal")) {
			meta.setCustomModelData(2);
		}
		item.setItemMeta(meta);
		inv.setItem(1, item);
		// startup stuff
		if(hasAddon("50cal")) {
			damage += 1;
			shootingCooldown += 1f;
			recoil += 0.9;
			maxAmmo --;
		}
		if(hasAddon("quick_hand")) {
			reloadTime -= 1;
		}
		if(hasAddon("extra_ammo")) {
			maxAmmo += 2;
			reloadTime += 1;
		}
		if(hasAddon("longer_range")) {
			range += 16;
			recoil += 0.1;
		}
		if(hasAddon("quick_cylinder")) {
			shootingCooldown -= 0.4f;
		}
		if(hasAddon("hollow_point")) {
			reloadTime += 0.5f;
		}
		ammo = maxAmmo;
	}
	
	@Override
	public void rightClickItem(ItemStack item, Block clickedBlock, BlockFace face) {
		if(!item.getType().equals(Material.IRON_HORSE_ARMOR)) {
			return;
		}
		if(!isReloading && user.hitCooldown.isDone() && cooldown.isDone()) {
			if(ammo <= 0) {
				return;
			}
			cooldown.start(shootingCooldown);
			ammo --;
			user.sendActionbar("hunter_ammo_actionbar", ""+ ammo, ""+maxAmmo);
			// Quick access variables
			final double maxLength = 8;
			final Player player = user.getPlayer();
			final Location loc = player.getEyeLocation();
			final Vector direction = loc.getDirection();
			Location hitBlock = null;
			World world = loc.getWorld();
			if(!hasAddon("supressor")) {
				world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 5, 0);
			}else {
				world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.5f);
			}
			
			// checking second ray trace to see if we hit a block
			RayTraceResult result = world.rayTraceBlocks(player.getEyeLocation(), loc.getDirection(), range);
			if(result != null) {
				Block block = result.getHitBlock();
				if(block != null) {
					Vector pos = result.getHitPosition();
					ParticlesUtil.spawnBlock(pos.toLocation(world), block.getType(), 32);
					hitBlock = block.getLocation();
					player.sendBlockDamage(hitBlock, 0.5f);
				}
			}
			// Get what mob we have to damage
			// Move the starting position forward by multiplying 1.5, so that we don't hit the player holding the weapon.
			result = world.rayTraceEntities(player.getEyeLocation().add(direction.multiply(1.5)), direction, range, new Predicate<Entity>() {
				public boolean test(Entity hitEntity) {
					if(hitEntity == null || !(hitEntity instanceof LivingEntity))
						return false;
					LivingEntity entity = (LivingEntity)hitEntity;
					if(entity instanceof Player) {
						// Check this is a survivor we hit
						DLUser victim = PlayerManager.getUser(entity.getUniqueId());
						if(victim == null) {
							return false;
						}
						// make sure the victim isn't a spectator
						return !victim.isSpectating() && !victim.isKiller();
					}
					return false;
				}
			});
			if(recoil > 0) {
				// Do recoil after the entities have already been traced
				direction.setY(0);
				user.push(direction.multiply(-1).normalize(), recoil);
			}
			// Spawn a smooth line of smoke particles
			for(double d = 0; d <= maxLength; d += 0.1)
			{
				loc.add(loc.getDirection().multiply(2));
				if(loc.getBlock().getType() != Material.AIR) {
					break;
				}
				world.spawnParticle(damage > 1 ? Particle.FLAME : Particle.SMOKE_NORMAL, loc.getX(), loc.getY(), loc.getZ(), 3, 0, 0, 0, 0.05f);
			}
			if(result != null) {
				DLUser victim = PlayerManager.getUser(result.getHitEntity().getUniqueId());
				if(victim != null) {
					if(hitBlock == null || player.getEyeLocation().distance(hitBlock) > player.getEyeLocation().distance(victim.getLocation())) {
						if(!victim.getUUID().equals(player.getUniqueId())) {
							if(ammo == maxAmmo-1) {
								if(hasAddon("hollow_point")) {
									if(!victim.hasEffect(MangledEffect.class)) {
										victim.applyEffect(new MangledEffect(20000));
									}
									if(!victim.hasEffect(HemorrhageEffect.class)) {
										victim.applyEffect(new HemorrhageEffect(1, 20000));
									}
								}
							}
							Lobby lobby = user.getLobby();
							lobby.lobbyDamage(damage, user, victim, AttackType.ABILITY);
							player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
							if(hasAddon("flare")) {
								victim.glowToKiller(5000);
							}
							if(hasAddon("50cal")) {
								user.hitCooldown.start(user.getLobby().settings.hitCooldown + 3);
							}
							if(hasAddon("rusty_bullets")) {
								victim.applyEffect(new InfectedEffect(5000));
							}
							if(hasAddon("heavy_impact")) {
								victim.push(user.getDirection(), 6);
							}
							if(hasAddon("ricochet")) {
								DLUser nearest = null;
								for(DLUser nearby : lobby.getSurvivors()) {
									if(nearby.equals(victim)) continue;
									if(victim.getLocation().distance(nearby.getLocation()) < 4) {
										nearest = nearby;
									}
								}
								if(nearest != null) {
									lobby.lobbyDamage(damage, user, nearest, AttackType.ABILITY);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void perkEvent(PerkEvent event) {
		if(!user.getInventory().getItemInMainHand().getType().equals(Material.IRON_HORSE_ARMOR)) {
			return;
		}
		if(event.getType().equals(PerkEventType.PRESS_F)) {
			if(ammo >= maxAmmo) {
				return;
			}
			reloading.start(reloadTime);
			isReloading = true;
			sendReloading();
		}
	}
	
	public void sendReloading() {
		user.sendActionbar("hunter_reloading", ((int)(reloading.timeLeft()/1000) + "s"));
		user.playSound(Sound.ENTITY_ARMOR_STAND_BREAK, 1);
	}
	
	public void tick() {
		if(ammo == 0) {
			ammoEmptySeconds++;
			if(ammoEmptySeconds > 10) {
				user.sendActionbar("hunter_reload_hint");
				ammoEmptySeconds = 0;
			}
		}
		if(isReloading) {
			if(reloading.isDone()) {
				isReloading = false;
				// we are done reloading
				ammo = maxAmmo;
				user.sendActionbar("hunter_reload_complete");
				user.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2);
				ammoEmptySeconds = 0;
			}
			else {
				sendReloading();
			}
		}
	}
	
	@Override
	public int getTerrorRadius() {
		return 32;
	}
	
}
