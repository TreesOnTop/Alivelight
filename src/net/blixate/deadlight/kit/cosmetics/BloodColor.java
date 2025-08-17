package net.blixate.deadlight.kit.cosmetics;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;
import net.md_5.bungee.api.ChatColor;

public enum BloodColor {
	RED(Color.RED, Material.RED_WOOL, 0),
	GREEN(Color.GREEN, Material.GREEN_WOOL, 50000),
	BLUE(Color.BLUE, Material.BLUE_WOOL, 50000),
	PURPLE(Color.PURPLE, Material.PURPLE_WOOL, 50000),
	WHITE(Color.WHITE, Material.WHITE_WOOL, 50000),
	YELLOW(Color.YELLOW, Material.YELLOW_WOOL, 50000),
	OLIVE(Color.OLIVE, Material.GREEN_TERRACOTTA, 50000),
	ORANGE(Color.ORANGE, Material.ORANGE_WOOL, 50000);
	
	Color color;
	Material icon;
	long cost;
	
	BloodColor(Color color, Material mat, long cost) {
		this.color = color;
		this.icon = mat;
		this.cost = cost;
	}

	ItemStack getItem(DLUser user) {
		ItemStack item = new ItemStack(icon);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.of(new java.awt.Color(color.asRGB())) + properName());
		List<String> lore = new ArrayList<>();
		lore.add("&8Blood Color");
		lore.add("");
		if(!user.purchasedBloodColors.contains(this.name()) && cost > 0) {
			lore.add("&7Click to purchase for &e" + FormatUtil.formatLong(cost) + " Blood");
		} else if(user.bloodColor == this) {
			lore.add("&aSelected");
		}else {
			lore.add("&7Click to select");
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public String properName() {
		return name().toUpperCase().charAt(0) + name().toLowerCase().substring(1);
	}

	public Color getColor() {
		return color;
	}
}
