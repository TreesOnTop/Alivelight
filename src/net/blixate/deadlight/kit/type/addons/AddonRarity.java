package net.blixate.deadlight.kit.type.addons;

import org.bukkit.Material;

import net.blixate.deadlight.kit.offerings.Offering;
import net.blixate.deadlight.kit.offerings.OfferingManager;
import net.md_5.bungee.api.ChatColor;

public enum AddonRarity {
	ULTRA(Material.RED_DYE, ChatColor.RED, 15000, 30),
	RARE(Material.CYAN_DYE, ChatColor.DARK_AQUA, 5000, 15),
	COMMON(Material.LIME_DYE, ChatColor.GREEN, 1000, 10);
	
	public Material icon;
	public ChatColor color;
	public int cost;
	public int level;
	
	AddonRarity(Material icon, ChatColor color, int cost, int level) {
		this.icon = icon;
		this.color = color;
		this.cost = cost;
		this.level = level;
	}
	
	AddonRarity(Material icon, ChatColor color, int cost) {
		this(icon, color, cost, 0);
	}
	
	public String toString() {
		switch(this) {
		case ULTRA:
			return "Ultra Rare";
		case RARE:
			return "Rare";
		case COMMON:
			return "Common";
		default:
			return "Unknown";
		}
	}
	
	public long getCost() {
		return (long)((double)cost * (1 + OfferingManager.isOfferingActive(Offering.EXTERMINATOR_CREST) * 0.1));
	}
}
