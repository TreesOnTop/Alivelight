package net.blixate.deadlight.kit.type;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.lobby.AttackType;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.player.effects.MangledEffect;
import net.blixate.deadlight.util.NameGenerator;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.time.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class NecromancerType extends KillerType {
	
	public static String minionSkullTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U5YmE4ZDY3ZTQxNjFlZjhlNzQyZmI3YmYxOThiNjE0MTJmMjRjMmY4MmI1NDU4MTA4Y2RlOTkyOTNlNzdhMSJ9fX0=";
	
	public static class Minion {
		LivingEntity entity;
		long spawnTime;
		Cooldown explodeCooldown = new Cooldown();
		int hitsBeforeDestruction = 1;
		
		public Minion(LivingEntity entity) {
			this.entity = entity;
			this.spawnTime = System.currentTimeMillis();
			this.explodeCooldown.start(4);
		}
		
		public Location getLocation() {
			return entity.getLocation();
		}
		
		public String getName() {
			return entity.getCustomName();
		}
		
		public long getSpawnTime() {
			return spawnTime;
		}
	}
	
	public static NamespacedKey MINION_OWNER = new NamespacedKey(Deadlight.inst, "minionOwner");
	
	AttributeModifier summonerSpeedModifier = new AttributeModifier("minion_speed", -0.05, Operation.ADD_NUMBER);
	PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1, false, false, false);
	PotionEffect plagueSlowness = new PotionEffect(PotionEffectType.SLOW, 10*20, 0, false, false, false);
	
	NameGenerator nameGenerator = new NameGenerator();
	Cooldown summonCooldown = new Cooldown();
	Cooldown teleportCooldown = new Cooldown();
	Cooldown syphonCooldown = new Cooldown();
	ArrayList<Minion> minions = new ArrayList<>();
	private Minion[] minionCheckList;
	int maxMinions = 3;
	double range = 40;
	float cooldown = 3f;
	
	@Override
	public void equipKiller(PlayerInventory inv) {
		quickEquip(user, inv);
		ItemStack item = new ItemStack(Material.WOODEN_HOE);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Summoning Scythe" + INPUT_TYPE_RIGHT_CLICK);
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
		inv.setItem(1, item);
		if(hasAddon("critters")) {
			maxMinions += 1;
		}
		if(hasAddon("speed_scythe")) {
			cooldown -= 1;
		}
		minionCheckList = new Minion[maxMinions];
	}
	
	@Override
	public void cleanup() {
		while(!minions.isEmpty()) {
			removeZombie(minions.get(0));
		}
	}
	
	@Override
	public void rightClickItem(ItemStack item, Block clickedBlock, BlockFace face) {
		if(!item.getType().equals(Material.WOODEN_HOE)) {
			return;
		}
		if(!summonCooldown.isDone()) {
			user.sendActionbar("ability_cooldown", summonCooldown.secondsLeft());
			return;
		}
		if(minions.size() >= maxMinions) {
			return;
		}
		Zombie zombie = createMinion(user.getLocation().add(0, 0.5, 0));
		if(hasAddon("engraved_blade")) {
			zombie.setVelocity(user.getDirection().normalize().multiply(3));
		}else {
			zombie.setVelocity(user.getDirection().normalize().multiply(2));
		}
		Minion minion = new Minion(zombie);
		if(hasAddon("crimson")) {
			minion.hitsBeforeDestruction ++;
		}
		minions.add(minion);
		user.sendActionbar("necromancer_summoned", zombie.getCustomName(), ""+minions.size(), ""+maxMinions);
		ParticlesUtil.spawnParticle(Particle.PORTAL, user.getLocation(), 10, 0, 1, 1, 1);
		summonCooldown.start(cooldown);
	}
	
	public Zombie createMinion(Location location) {
		Deadlight.debug("Starting entity spawn...");
		World world = location.getWorld();
		Zombie minion = world.spawn(location, Zombie.class, (entity) -> {
			user.getLobby().killerTeam.addEntry(entity.getUniqueId().toString());
			entity.setBaby();
			entity.setSilent(true);
			entity.setCustomName(nameGenerator.pickName());
			entity.setCustomNameVisible(true);
			entity.getPersistentDataContainer().set(MINION_OWNER, PersistentDataType.STRING, user.getUUID().toString());
			entity.getEquipment().setHelmet(getHead(minionSkullTexture, "Necromancer"));
			if(hasAddon("crimson")) {
				entity.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
				entity.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
				entity.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
			}else {
				entity.getEquipment().setChestplate(leatherArmor(Material.LEATHER_CHESTPLATE));
				entity.getEquipment().setLeggings(leatherArmor(Material.LEATHER_LEGGINGS));
				entity.getEquipment().setBoots(leatherArmor(Material.LEATHER_BOOTS));
			}
			if(hasAddon("summoner_scythe")) {
				entity.getEquipment().setItemInMainHand(new ItemStack(Material.TNT));
				entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(summonerSpeedModifier);
			}else {
				if(hasAddon("sharded_glass")) {
					entity.getEquipment().setItemInMainHand(new ItemStack(Material.PRISMARINE_SHARD));
				}else {
					entity.getEquipment().setItemInMainHand(new ItemStack(Material.STICK));
				}
			}
			if(entity.isInsideVehicle()) {
				entity.getVehicle().remove();
			}
			entity.getPersistentDataContainer().set(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER, user.getLobby().randomEntityLobbyId);
		});
		if(hasAddon("spring_boots")) {
			minion.addPotionEffect(jump);
		}
		Deadlight.debug("Completed!");
		return minion;
	}
	
	public ItemStack leatherArmor(Material mat) {
		ItemStack armor = new ItemStack(mat);
		LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
		meta.setColor(Color.BLACK);
		armor.setItemMeta(meta);
		return armor;
	}
	
	@Override
	public void perkEvent(PerkEvent event) {
		if(event.getType().equals(PerkEventType.PRESS_F)) {
			if(minions.size() > 0) {
				World world = user.getLocation().getWorld();
				Player player = user.getPlayer();
				org.bukkit.util.Vector direction = user.getDirection();
				RayTraceResult result = world.rayTraceEntities(player.getEyeLocation().add(direction.multiply(1.5)), direction, range, new Predicate<Entity>() {
					public boolean test(Entity hitEntity) {
						if(hitEntity == null || !(hitEntity instanceof LivingEntity))
							return false;
						LivingEntity entity = (LivingEntity)hitEntity;
						if(entity instanceof Zombie) {
							return true;
						}
						return false;
					}
				});
				Minion minion;
				if(result == null) {
					minion = minions.get(0);
				}else {
					minion = getMinionByEntity(result.getHitEntity());
				}
				Location location = minion.getLocation();
				if(hasAddon("dipped_blade")) {
					if(!teleportCooldown.isDone()) {
						user.sendActionbar("ability_cooldown", teleportCooldown.secondsLeft());
						return;
					}
					user.getPlayer().teleport(location);
					removeZombie(minion);
					location.getWorld().playSound(location, Sound.ENTITY_WITHER_AMBIENT, 2, 2);
					teleportCooldown.start(20f);
				}else {
					removeZombie(minion);
					ParticlesUtil.spawnParticle(Particle.PORTAL, location, 10, 0, 1, 1, 1);
					user.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, 2);
					user.sendActionbar("necromancer_recall", minion.getName());
					summonCooldown.start(1f);
				}
			}
			else {
				user.sendActionbar("necromancer_no_minions");
			}
		}
	}
	
	@Override
	public void hitEntity(Entity entity) {
		if(entity instanceof Zombie) {
			if(hasAddon("syphon")) {
				if(!user.getPlayer().hasPotionEffect(PotionEffectType.SPEED) && syphonCooldown.isDone()) {
					user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, false, false, false));
				}
			}
			user.send("necromancer_killer_minion_killed", entity.getCustomName());
			user.playSound(Sound.ENTITY_WITHER_SKELETON_HURT, 1);
			removeZombie(entity);
			ParticlesUtil.spawnBlock(entity.getLocation().add(0, .5, 0), Material.REDSTONE_BLOCK, 20);
		}
	}
	
	@Override
	public void survivorHitEntity(Entity entity, DLUser survivor) {
		if(entity instanceof Zombie) {
			if(survivor.isKiller()) return;
			Zombie zombie = (Zombie)entity;
			Minion minion = getMinionByEntity(zombie);
			minion.hitsBeforeDestruction --;
			if(minion.hitsBeforeDestruction <= 0) {
				user.send("necromancer_minion_killed", entity.getCustomName());
				user.playSound(Sound.ENTITY_WITHER_SKELETON_HURT, 1);
				survivor.addScoreEvent(new ScoreEvent("necromancer_destroy_minion", ScoreType.BLOOD));
				ParticlesUtil.spawnBlock(entity.getLocation().add(0, .5, 0), Material.REDSTONE_BLOCK, 20);
				removeZombie(minion);
			}else {
				user.playSound(Sound.ENTITY_WITHER_SKELETON_STEP, 2);
				zombie.setMaximumNoDamageTicks(0);
				zombie.setTarget(null);
				zombie.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 3, false, false, false));
				zombie.setVelocity(survivor.getDirection().normalize().multiply(1.5));
			}
			
		}
	}
	
	@Override
	public void entityHitSurvivor(Entity entity, DLUser survivor) {
		if(entity instanceof Zombie) {
			if(hasAddon("summoner_scythe")) {
				return;
			}
			Zombie zombie = (Zombie)entity;
			if(hasAddon("plague_minion")) {
				if(survivor.hasEffect(InfectedEffect.class)) {
					survivor.removeEffect(InfectedEffect.class);
				}
				survivor.applyEffect(new InfectedEffect(10000));
				survivor.getPlayer().addPotionEffect(plagueSlowness);
			}else {
				survivor.getLobby().lobbyDamage(1, user, survivor, AttackType.TRAP);
			}
			if(hasAddon("sharded_glass")) {
				survivor.applyEffect(new MangledEffect(30000));
			}
			user.addScoreEvent(new ScoreEvent("necromancer_minion_hit", ScoreType.BLOOD));
			user.playSound(Sound.ENTITY_WITHER_SKELETON_HURT, 2);
			removeZombie(zombie);
		}
	}
	
	public void tick() {
		for(Minion minion : minions) {
			Zombie zombie = (Zombie)minion.entity;
			Location location = minion.getLocation();
			if(zombie.getTarget() != null && zombie.getTarget() instanceof Player) {
				DLUser target = PlayerManager.getUser(zombie.getTarget());
				target.playSound(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 2, 0, location);
			}
		}
		if(hasAddon("summoner_scythe")) {
			minions.toArray(minionCheckList);
			for(Minion minion : minionCheckList) {
				if(minion == null) {
					continue;
				}
				if(minion.explodeCooldown.isDone()) {
					boolean explode = false;
					for(DLUser player : user.getLobby().getSurvivors()) {
						// check distance
						if(minion.getLocation().distance(player.getLocation()) < 3) {
							// explode
							explode = true;
							removeZombie(minion);
							ParticlesUtil.createFakeExplosion(minion.getLocation());
							break;
						}
					}
					if(explode) {
						for(DLUser player : user.getLobby().getSurvivors()) {
							// check distance
							if(minion.getLocation().distance(player.getLocation()) < 3) {
								user.getLobby().lobbyDamage(1, user, player, AttackType.TRAP);
							}
						}
						break;
					}
				}
			}
		}
	}
	
	public Minion getMinionByEntity(Entity entity) {
		for(Minion minion : minions) {
			if(minion.entity.getUniqueId().equals(entity.getUniqueId())) {
				return minion;
			}
		}
		return null;
	}
	
	public void removeZombie(Entity entity) {
		Minion minion = getMinionByEntity(entity);
		if(minion == null) {
			return;
		}
		removeZombie(minion);
	}
	
	public void removeZombie(Minion entity) {
		if(entity == null) {
			return;
		}
		entity.entity.remove();
		minions.remove(entity);
		user.getLobby().killerTeam.removeEntry(entity.entity.getUniqueId().toString());
	}
}
