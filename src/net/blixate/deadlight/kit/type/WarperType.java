package net.blixate.deadlight.kit.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.player.effects.UndetectableEffect;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.time.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class WarperType extends KillerType {
	
	Cooldown cooldown;
	Cooldown reloading;
	boolean isReloading;
	
	Cooldown warpHitCooldown;
	
	int maxAmmo = 2;
	int range = 32;
	int ammo;
	int reloadTime = 6;
	float warpSpeed = 2;
	float cooldownTime = 2f;
	float warpVolume = 5;
	
	public WarperType() {
		tickInterval = 1;
	}
	
	@Override
	public void equipKiller(PlayerInventory inv) {
		quickEquip(user, inv);
		cooldown = new Cooldown();
		reloading = new Cooldown();
		warpHitCooldown = new Cooldown();
		ItemStack item = new ItemStack(Material.FIREWORK_STAR);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Warp Star"+ INPUT_TYPE_CLICK);
		item.setItemMeta(meta);
		inv.setItem(1, item);
		
		if(hasAddon("batteries")) {
			maxAmmo += 1;
			range -= 8;
			reloadTime += 1;
		}
		if(hasAddon("alkaline")) {
			reloadTime -= 2;
		}
		if(hasAddon("power_cell")) {
			maxAmmo += 2;
			reloadTime += 2;
			cooldownTime += 0.5f;
		}
		if(hasAddon("radium_infused")) {
			range += 16;
		}
		if(hasAddon("lightweight_steel")) {
			warpSpeed += 1f;
			range += 8;
		}
		if(hasAddon("override")) {
			warpVolume -= 2f;
		}
		ammo = maxAmmo;
		
	}
	
	@Override
	public void leftClickItem(ItemStack item, Block clickedBlock, BlockFace face) {
		if(isReloading) {
			return;
		}
		if(!item.getType().equals(Material.FIREWORK_STAR)) {
			return;
		}
		if(cooldown.isDone()) {
			if(ammo <= 0) {
				user.sendActionbar("warper_recharge_hint");
				return;
			}
			Location originalLocation = user.getLocation();
			ammo--;
			ParticlesUtil.spawnParticle(Particle.REVERSE_PORTAL, originalLocation.add(0,1,0), 20, 0.2, 0, 1, 0);
			user.sendActionbar("warper_charges", ""+ammo, ""+maxAmmo);
			user.push(new Vector(0, 1, 0), warpSpeed);
			cooldown.start(cooldownTime / 2);
			for(DLUser survivor : user.getLobby().getPlayers()) {
				survivor.getPlayer().playSound(user.getLocation(), Sound.ENTITY_BAT_TAKEOFF, SoundCategory.MASTER, warpVolume, 1);
			}
			warpHitCooldown.start(3f);
			if(hasAddon("thrusters")) {
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 1, false, false, false));
			}
		} else {
			user.sendActionbar("ability_cooldown", cooldown.secondsLeft());
		}
	}
	
	@Override
	public void rightClickItem(ItemStack item, Block clickedBlock, BlockFace face) {
		if(isReloading) {
			return;
		}
		if(!item.getType().equals(Material.FIREWORK_STAR)) {
			return;
		}
		if(cooldown.isDone()) {
			if(ammo <= 0) {
				user.sendActionbar("warper_recharge_hint");
				return;
			}
			Location originalLocation = user.getLocation();
			Location targetLocation = getTargetLocation();
			if(targetLocation == null) {
				return;
			}
			ammo--;
			ParticlesUtil.spawnParticle(Particle.REVERSE_PORTAL, originalLocation.add(0,1,0), 20, 0.2, 0, 1, 0);
			user.sendActionbar("warper_charges", ""+ammo, ""+maxAmmo);
			targetLocation.setYaw(user.getLocation().getYaw());
			targetLocation.setPitch(user.getLocation().getPitch());
			user.getPlayer().teleport(targetLocation);
			cooldown.start(cooldownTime);
			user.hitCooldown.start(1.7f);
			for(DLUser survivor : user.getLobby().getPlayers()) {
				survivor.getPlayer().playSound(user.getLocation(), Sound.ENTITY_BAT_TAKEOFF, SoundCategory.MASTER, warpVolume * 2, 1);
			}
			ParticlesUtil.spawnParticle(Particle.REVERSE_PORTAL, targetLocation, 5, 0.2, 1, 1, 1);
			if(originalLocation.distance(targetLocation) > 20) {
				if(hasAddon("radium_infused")) {
					user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1, false, false, false));
					for(DLUser player : user.getLobby().getSurvivors()) {
						if(player.getLocation().distance(targetLocation) < 5) {
							player.applyEffect(new InfectedEffect(20000));
						}
					}
					cooldown.start(cooldownTime + 0.5f);
				}
			}
			if(hasAddon("time_and_space")) {
				user.applyEffect(new UndetectableEffect(3000));
			}
			warpHitCooldown.start(3f);
		} else {
			user.sendActionbar("ability_cooldown", cooldown.secondsLeft());
		}
	}
	
	public void perkEvent(PerkEvent event ) {
		if(event.getType().equals(PerkEventType.PRESS_F)) {
			if(isReloading) {
				return;
			}
			if(ammo == maxAmmo) {
				return;
			}
			isReloading = true;
			reloading.start(reloadTime);
			if(!hasAddon("rechargable")) {
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, reloadTime * 20, 0));
			}
		}
		if(event.getType().equals(PerkEventType.SURVIVOR_HIT)) {
			if(!warpHitCooldown.isDone()) {
				user.addScoreEvent(new ScoreEvent("warper_warp_hit", ScoreType.BLOOD));
			}
		}
	}
	
	public void tick() {
		if(isReloading) {
			if(reloading.isDone()) {
				// reload
				ammo = maxAmmo;
				isReloading = false;
				user.playSound(Sound.BLOCK_NOTE_BLOCK_BELL, 1);
				user.sendActionbar("warper_recharged");
			}else {
				user.sendActionbar("warper_recharge", reloading.secondsLeft());
			}
		}
		if(hasAddon("lazer")) {
			if(user.getHeldItem() != null) {
				if(!user.getHeldItem().getType().equals(Material.FIREWORK_STAR)) {
					return;
				}
				Location targetLocation = getTargetLocation();
				if(targetLocation != null) {
					user.getPlayer().spawnParticle(Particle.REVERSE_PORTAL, targetLocation, 1, 0, 0, 0, 0);
					user.getPlayer().spawnParticle(Particle.REVERSE_PORTAL, targetLocation.add(0, 1, 0), 1, 0, 0, 0, 0);
				}
			}
		}
	}
	
	private Location getTargetLocation() {
		Player player = user.getPlayer();
		RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), user.getDirection(), range);
		if(result != null && result.getHitBlock() != null) {
			Block tpBlock = result.getHitBlock().getRelative(result.getHitBlockFace());
			if(!tpBlock.getType().isSolid())
				return tpBlock.getLocation().add(0.5, 0.5, 0.5);
		}
		return null;
	}
	
	@Override
	public float getMovementSpeed() {
		return 0.18f;
	}

}
