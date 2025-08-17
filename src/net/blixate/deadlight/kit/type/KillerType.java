package net.blixate.deadlight.kit.type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.type.addons.AddonManager;
import net.blixate.deadlight.kit.type.addons.AddonRarity;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.KillerData;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.HeadUtils;
import net.md_5.bungee.api.ChatColor;

public abstract class KillerType {
	
	public static final String INPUT_TYPE_LEFT_CLICK = ChatColor.GRAY + " (Left-Click)";
	public static final String INPUT_TYPE_RIGHT_CLICK = ChatColor.GRAY + " (Right-Click)";
	public static final String INPUT_TYPE_CLICK = ChatColor.GRAY + " (Left/Right Click)";
	
	protected double movementSpeed = 0.2;
	protected int terrorRadius = 24;
	protected DLUser user;
	protected int tickInterval = 20;
	
	private BukkitTask tickTask;
	private List<String> addons;
	
	public void setup(DLUser user) {
		this.user = user;
		KillerTypes type = KillerTypes.getByClass(this.getClass());
		ConfigurationSection cfg = type.getData();
		if(getKillerData().getAddons() != null && getKillerData().getAddons().size() > 0) {
			addons = new ArrayList<String>();
			ConfigurationSection addonCfg = cfg.getConfigurationSection("addons");
			for(String augment : getKillerData().getAddons()) {
				AddonRarity rarity = AddonRarity.valueOf(addonCfg.getString(augment + ".rarity"));
				// charge
				BigInteger cost = BigInteger.valueOf(rarity.getCost());
				if(user.blood.compareTo(cost) >= 0) {
					user.blood = user.blood.subtract(cost);
					addons.add(augment);
				}else {
					user.send("addon_no_funds", AddonManager.getNameById(type, augment));
				}
			}
		}
		// 0 = disabled
		if(tickInterval != 0) {
			tickTask = Bukkit.getScheduler().runTaskTimer(Deadlight.inst, () -> tick(), tickInterval, tickInterval);
		}
	}
	
	public KillerTypes getType() {
		return KillerTypes.getByClass(this.getClass());
	}
	
	public boolean hasAddon(String name) {
		if(addons == null) {
			return false;
		}
		return addons.contains(name);
	}
	
	public void initCleanup() {
		if(tickTask != null) {
			tickTask.cancel();
			tickTask = null;
		}
		cleanup();
	}
	
	public DLUser getPlayer() {
		return user;
	}
	
	public KillerData getKillerData() {
		return user.getKillerData();
	}
	
	/**
	 * Killer Class Functions
	 */
	
	/** Give the killer their items. */
	public abstract void equipKiller(PlayerInventory inv);
	
	/** Called whenever a perk event is called. */
	public void perkEvent(PerkEvent event) {}
	
	/** Called every second. */
	public void tick() {}
	
	public void particleTick() {}
	
	public void cleanup() {}
	
	public void clickItem(ItemStack item, Block clickedBlock) {}
	
	public void rightClickItem(ItemStack item, Block clickedBlock, BlockFace face) {}
	public void leftClickItem(ItemStack item, Block clickedBlock, BlockFace face) {}
	public void hitEntity(Entity entity) {}
	public void survivorHitEntity(Entity entity, DLUser survivor) {}
	public void entityHitSurvivor(Entity entity, DLUser survivor) {}
	
	public float getMovementSpeed() {
		return (float) movementSpeed;
	}
	
	public int getTerrorRadius() {
		return terrorRadius;
	}
	
	public Sound getTerrorSound() {
		return Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
	}
	
	public float getTerrorPitch() {
		return 0.4f;
	}
	
	public boolean canAttack() {
		return true;
	}
	
	protected static ItemStack makeItem(Material mat, String name) {
		return makeItem(mat, name, null);
	}
	
	protected static ItemStack makeItem(Material mat, String name, String lore) {
		ItemStack item = new ItemStack(mat, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(FormatUtil.color(name));
		if(lore != null) {
			meta.setLore(Lists.newArrayList(FormatUtil.color(lore).split("\n")));
		}
		item.setItemMeta(meta);
		return item;
	}
	
	protected ItemStack getHead() {
		return getHead(getType().getData().getString("head"), getType().getData().getString("name"));
	}
	
	protected static ItemStack getHead(String head, String name) {
		ItemStack item = HeadUtils.createPlayerHead(head);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + name);
		item.setItemMeta(meta);
		return item;
	}
	
	protected static ItemStack getHead(String killerName) {
		return getHead(KillerTypes.valueOf(killerName.toUpperCase()).getData().getString("head"), killerName);
	}
	
	protected static ItemStack getItem(String killerName, Material material) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + killerName);
		item.setItemMeta(meta);
		return item;
	}
	
	// don't waste time on helmet, it'll be a player head or block
	protected static ItemStack[] getLeatherArmor(String killerName) {
		ConfigurationSection section = KillerTypes.valueOf(killerName.toUpperCase()).getData();
		ItemStack[] items = {new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_BOOTS)};
		for(ItemStack item : items) {
			LeatherArmorMeta leather = (LeatherArmorMeta)item.getItemMeta();
			String colorString = section.getString("color");
			if(!colorString.startsWith("#")) {
				colorString = "#" + colorString;
			}
			java.awt.Color color = java.awt.Color.decode(colorString);
			int armorColor = color.getRGB() & 0x00FFFFFF;
			leather.setColor(Color.fromRGB(armorColor));
			leather.setDisplayName(ChatColor.RED + killerName);
			item.setItemMeta(leather);
		}
		return items;
	}
	
	public ConfigurationSection getSkin(String skinId) {
		return getType().getData().getConfigurationSection("skins." + skinId);
	}
	
	public void quickEquip(DLUser user, PlayerInventory inv) {
		inv.setItem(0, makeItem(user.killerItem.material, "&6" + user.killerItem.name, "&7" + user.killerItem.description));
		if(user.killerData.get(user.killerType.name()).inDarkCommand) {
			String name = getSkin("DARK_COMMAND").getString("name");
			inv.setHelmet(getHead(getSkin("DARK_COMMAND").getString("head"), name));
			inv.setChestplate(getItem(name, Material.NETHERITE_CHESTPLATE));
			inv.setLeggings(getItem(name, Material.NETHERITE_LEGGINGS));
			inv.setBoots(getItem(name, Material.NETHERITE_BOOTS));
		}else {
			ItemStack[] armor = getLeatherArmor(getType().name());
			inv.setHelmet(getHead(getType().name()));
			inv.setChestplate(armor[0]);
			inv.setLeggings(armor[1]);
			inv.setBoots(armor[2]);
		}
	}
}
