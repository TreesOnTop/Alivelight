package net.blixate.deadlight.maps.environment;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.PureChatColor;
import net.blixate.deadlight.util.time.TimeParser;
import net.blixate.deadlight.util.time.Timer;
import net.blixate.deadlight.util.time.TimerListener;
import net.md_5.bungee.api.ChatColor;

public class PlayerSoul implements TimerListener {
	public static final NamespacedKey SOUL_KEY = new NamespacedKey(Deadlight.inst, "soulId");
	public static final NamespacedKey UNSTACKABLE_KEY = new NamespacedKey(Deadlight.inst, "timestamp");
	
	ArmorStand entity;
	UUID owner;
	Timer bleedout;
	Lobby lobby;
	
	BukkitTask task;
	
	public PlayerSoul(Lobby lobby) {
		this.bleedout = new Timer(this);
		this.lobby = lobby;
	}
	
	public void spawn(DLUser owner, Location deathLocation) {
		World world = deathLocation.getWorld(); // short hand
		entity = world.spawn(deathLocation, ArmorStand.class, (entity) -> {
			entity.setBasePlate(false);
			entity.setVisible(false);
			entity.setGravity(false);
			entity.setSmall(true);
			entity.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
			entity.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING);
			entity.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING);
			entity.addEquipmentLock(EquipmentSlot.HAND, LockType.ADDING);
			entity.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING);
			entity.getPersistentDataContainer().set(SOUL_KEY, PersistentDataType.STRING, owner.getUUID().toString());
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta)head.getItemMeta();
			meta.setOwningPlayer(owner.getPlayer());
			head.setItemMeta(meta);
			entity.getEquipment().setHelmet(head);
			entity.setCustomName(PureChatColor.AQUA + "SOUL OF " + owner.getName());
			entity.setCustomNameVisible(true);
			entity.getPersistentDataContainer().set(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER, lobby.randomEntityLobbyId);
		});
		this.owner = owner.getUUID();
		bleedout.start(owner.mpd.soulExpireTime);
		task = Bukkit.getScheduler().runTaskTimer(Deadlight.inst, () -> tick(), 0, 5);
	}
	
	public void tick() {
		ParticlesUtil.spawnParticle(Particle.SOUL_FIRE_FLAME, entity.getEyeLocation().subtract(0, 0.1, 0), 2, 0.01, 0.05, 0.1, 0.05);
		entity.setHeadPose(entity.getHeadPose().add(0, 0.25f, 0));
		DLUser owner = PlayerManager.getUser(this.owner);
		entity.setCustomName(PureChatColor.AQUA + "☠ " + ChatColor.BOLD + owner.getName() + ChatColor.GRAY + " " + TimeParser.toFancyTime(bleedout.timeLeft()));
	}
	
	public UUID getOwnerUUID() {
		return owner;
	}
	
	public ArmorStand getEntity() {
		return entity;
	}
	
	public BukkitTask getTask() {
		return task;
	}
	
	public Timer getBleedoutTimer() {
		return bleedout;
	}
	
	public ItemStack getItemStack() {
		OfflinePlayer headOwner = Bukkit.getOfflinePlayer(owner);
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta)head.getItemMeta();
		meta.setDisplayName(PureChatColor.AQUA + "☠ " + ChatColor.BOLD + headOwner.getName());
		meta.setLore(Lists.newArrayList(ChatColor.GRAY + "Place into Uncursed Portal Fragment", ChatColor.DARK_GRAY + "(End Portal Frame)"));
		meta.setOwningPlayer(headOwner);
		meta.getPersistentDataContainer().set(SOUL_KEY, PersistentDataType.STRING, owner.toString());
		meta.getPersistentDataContainer().set(UNSTACKABLE_KEY, PersistentDataType.LONG, System.currentTimeMillis());
		head.setItemMeta(meta);
		return head;
	}
	
	public String getPlayerName() {
		return getPlayerName(owner);
	}
	
	@Override
	public void onForceStop() {}

	@Override
	public void onFinish(Timer timer) {
		lobby.soulExpire(PlayerManager.getUser(this.getOwnerUUID()), this);
	}
	
	public static String getOwnerUUID(Entity entity) {
		PersistentDataContainer container = entity.getPersistentDataContainer();
		String uuidString = container.get(SOUL_KEY, PersistentDataType.STRING);
		return uuidString;
	}
	
	public static String getOwnerUUID(ItemStack item) {
		PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
		String uuidString = container.get(SOUL_KEY, PersistentDataType.STRING);
		return uuidString;
	}
	
	public static String getPlayerName(String uuid) {
		return Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
	}
	
	public static String getPlayerName(UUID uuid) {
		return Bukkit.getOfflinePlayer(uuid).getName();
	}
}
