package net.blixate.deadlight.kit.type;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.lobby.AttackType;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.player.effects.MangledEffect;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.time.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class ScrubberType extends KillerType {
	class Trap {
		ItemFrame trap;
		Cooldown placeCooldown = new Cooldown();
		int x, y, z;
		
		public Trap(Location location) {
			this.x = location.getBlockX();
			this.y = location.getBlockY();
			this.z = location.getBlockZ();
		}
		
		public void place() {
			placeCooldown.start(2f);
			trap = Deadlight.getWorld().spawn(getLocation(), ItemFrame.class, (entity) -> {
				entity.setFacingDirection(BlockFace.UP, true);
				ItemStack item;
				if(hasAddon("camo_paint")) {
					item = new ItemStack(Material.MELON_SEEDS);
				}else {
					item = new ItemStack(Material.BEETROOT_SEEDS);
				}
				if(item != null) {
					ItemMeta meta = item.getItemMeta();
					meta.setCustomModelData(1);
					item.setItemMeta(meta);
					entity.setItem(item, false);
					
				}
				entity.setVisible(false);
				entity.setGlowing(true);
				entity.setFixed(false); // Doesn't allow the item frame to be destroyed if true... might need another way.
				user.getLobby().fragmentTeam.addEntry(entity.getUniqueId().toString());
				entity.getPersistentDataContainer().set(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER, user.getLobby().randomEntityLobbyId);
			});
		}
		
		public boolean activate() {
			if(placeCooldown.isDone()) {
				World w = Deadlight.getWorld();
				
				Particle.DustOptions options = new Particle.DustOptions(Color.GREEN, 5);
				if(hasAddon("cyanide_pellets")) {
					options = new Particle.DustOptions(Color.BLUE, 2);
				}
				w.spawnParticle(Particle.REDSTONE, x, y+2, z, 150, 1, 1, 1, 1, options);
				w.playSound(getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 5, 2);
				return true;
			}
			return false;
		}
		
		public void destroy() {
			trap.remove();
		}
		
		public Location getLocation() {
			return new Location(Deadlight.getWorld(), x, y, z);
		}
	}
	
	Cooldown trapPlaceCooldown;
	ArrayList<Trap> traps = new ArrayList<>();
	int maxTraps = 6;
	int detectionRadius = 3;
	int effectRadius = 6;
	
	@Override
	public void equipKiller(PlayerInventory inv) {
		quickEquip(user, inv);
		trapPlaceCooldown = new Cooldown();
		ItemStack item = new ItemStack(Material.BEETROOT_SEEDS);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Plague Traps" + INPUT_TYPE_RIGHT_CLICK);
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
		inv.setItem(1, item);
		// addons
		if(hasAddon("good_springs")) {
			detectionRadius += 1;
		}
		if(hasAddon("loose_springs")) {
			effectRadius += 1;
		}
		if(hasAddon("trigger_finger")) {
			detectionRadius -= 1;
		}
		if(hasAddon("janitor_bag")) {
			maxTraps += 1;
		}
		if(hasAddon("bloody_janitor_bag")) {
			maxTraps += 2;
			detectionRadius -= 1;
			effectRadius -= 1;
		}
	}
	
	@Override
	public void rightClickItem(ItemStack item, Block clickedBlock, BlockFace face) {
		if(!item.getType().equals(Material.BEETROOT_SEEDS)) {
			return;
		}
		Location location = null;
		if(clickedBlock == null) {
			if(user.getLocation().getBlock().getRelative(BlockFace.UP).getType() != Material.AIR)
				location = user.getLocation();
		}else {
			if(face != BlockFace.UP) {
				Block block = clickedBlock.getRelative(BlockFace.UP);
				if(block.getType() != Material.AIR) {
					location = user.getLocation();
				}
			}else {
				location = clickedBlock.getRelative(BlockFace.UP).getLocation();
			}
		}
		if(location == null) return;
		if(trapPlaceCooldown.isDone()) {
			placeTrap(location);
			trapPlaceCooldown.start(.5f);
		}
	}
	
	public void placeTrap(Location location) {
		if(traps.size() < maxTraps) {
			Trap trap = new Trap(location);
			try {
				trap.place();
			}catch(Throwable t) {
				user.send("scrubber_cant_place_trap");
				return;
			}
			
			traps.add(trap);
			user.send("scrubber_set_trap", ""+traps.size(), ""+maxTraps);
		}
	}
	
	@Override
	public void cleanup() {
		int trapSize = traps.size();
		for(int i = 0; i < trapSize; i++) {
			Trap trap = traps.get(0);
			trap.destroy();
			traps.remove(0);
		}
	}
	
	// activate traps
	@Override
	public void tick() {
		ArrayList<Trap> activatedTraps = new ArrayList<Trap>();
		for(Trap trap : traps) {
			ParticlesUtil.spawnParticle(Particle.SMOKE_NORMAL, trap.getLocation().add(.5,.1,.5), 1, 0, 0.2, 0.2, 0.2);
			boolean activated = false;
			for(DLUser player : user.getLobby().getSurvivors()) {
				if(player.hasEffect(InfectedEffect.class)) {
					continue;
				}
				if(player.getPlayer().isSneaking() && !hasAddon("trigger_finger")) {
					continue;
				}
				// activated!
				if(player.getLocation().distance(trap.getLocation()) <= detectionRadius) {
					if((activated = trap.activate())) {
						activatedTraps.add(trap);
						user.addScoreEvent(new ScoreEvent("scrubber_trap_activated", ScoreType.BLOOD));
						trap.destroy();
					}
				}
			}
			if(activated) {
				for(DLUser player : user.getLobby().getSurvivors()) {
					if(player.getLocation().distance(trap.getLocation()) <= effectRadius) {
						player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 7 * 20, 2));
						if(!hasAddon("cyanide_pellets")) {
							player.applyEffect(new InfectedEffect(hasAddon("double_dose") ? 15000 : 10000));
						}else {
							user.getLobby().lobbyDamage(1, user, player, AttackType.TRAP);
						}
						if(hasAddon("shrapnel")) {
							player.applyEffect(new MangledEffect(30000));
							player.getMatchData().survivorHealingProgress = 0f;
						}
						if(hasAddon("glow_dust")) {
							player.glowToKiller(5000);
						}
					}
				}
			}
		}
		for(Trap t : activatedTraps) {
			traps.remove(t);
		}
		
	}
	
	public void hitEntity(Entity entity) {
		if(entity instanceof ItemFrame) {
			Trap selectedTrap = null;
			for(Trap trap : traps) {
				if(trap.trap.getUniqueId().equals(entity.getUniqueId())) {
					selectedTrap = trap;
					break;
				}
			}
			if(selectedTrap != null) {
				selectedTrap.destroy();
				traps.remove(selectedTrap);
			}
		}
	}
}
