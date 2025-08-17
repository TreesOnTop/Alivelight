package net.blixate.deadlight.kit.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public enum Items {
	BANDAGE("Bandage", "Right-click to heal 1 heart.", Material.PAPER, 8),
	SHREAK("Carter's Shriek", "Right-click to distract the Killer.", Material.BLAZE_POWDER, 8),
	MINTY_FRESH("Minty Fresh", "Right-click the Killer to stun.\nRight-click a survivor to accelerate.", Material.DRAGON_BREATH, 8),
	FLASH("Flash Powder", "Right-click to start your getaway.", Material.GLOWSTONE_DUST, 12),
	AIR_BAG("Bag of Air", "Right-click to propel yourself forward.", Material.FLOWER_POT, 16),
	GEN_TRAP("Fragment Key", "Adds 20% progress to a right-clicked objective\nUnblocks the portal fragment.", Material.TRIPWIRE_HOOK, 16),
	ZOOM("Weakened Warp Star", "Right-click to warp to your target location", Material.FIREWORK_STAR, 16),
	/** Flower of Knowledge, radius healing speed */
	FLOWER_TULIP("Flower of Knowledge", "Placable flower totem.\nRadiates &eHealing Speed\nRadiates &eUncursing Speed\n12m Radius", Material.RED_TULIP, 20),
	/** Flower of Healing, radius health */
	FLOWER_DAISY("Flower of Healing", "Placable flower totem.\nRadiates &eHealth\n8m Radius", Material.OXEYE_DAISY, 20),
	/** Flower of Speed, radius speed */
	FLOWER_LILY("Flower of Speed", "Placable flower totem.\nRadiates &eSpeed\n10m Radius", Material.LILY_OF_THE_VALLEY, 20),
	/** Flower of Undying, prevents death */
	FLOWER_WITHER("Flower of Undying", "Placable flower totem.\nProtects against a deadly hit,\nbut destroys the totem in the process.\n15m Radius", Material.WITHER_ROSE, 20);
	//GAMBLERS_DICE("Gambler's Dice", "Applies a random effect:\n1. Gain 1 heart\n2. Gain 25% Speed for 10 seconds\n3. Lose 1 heart\n4. Teleport to the Killer\n5. Gain -25% Speed for 10 seconds.\n6. Does nothing.", Material.RED_MUSHROOM_BLOCK, 20);
	
	public String name;
	public String desc;
	public Material icon;
	public long cost;
	
	Items(String name, String desc, Material item, long cost) {
		this.name = name;
		this.desc = desc;
		this.icon = item;
		this.cost = cost;
	}

	public ItemStack stack() {
		ItemStack item = new ItemStack(icon, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + name);
		item.setItemMeta(meta);
		return item;
	}
	
	public boolean is(ItemStack stack) {
		if(stack == null) return false;
		if(stack.getItemMeta() == null) return false;
		if(stack.getItemMeta().getDisplayName() == null) return false;
		return (stack.getItemMeta().getDisplayName().equals(ChatColor.GREEN + name));
	}
	
	public boolean isFlowerTotem() {
		return name().startsWith("FLOWER_");
	}

	public static Items get(String itemName) {
		for(Items item : values()) {
			if(item.name().equalsIgnoreCase(itemName)) {
				return item;
			}
		}
		return null;
	}
	
	public static boolean exists(String itemName) {
		return (get(itemName) != null);
	}
}
