package net.blixate.deadlight.kit.type.addons;

import org.bukkit.configuration.ConfigurationSection;

import net.blixate.deadlight.kit.type.KillerTypes;

public class AddonManager {
	
	public static ConfigurationSection getAddonSectionByKiller(KillerTypes killer) {
		return killer.getData().getConfigurationSection("addons");
	}
	
	public static Addon getAddonById(KillerTypes killer, String id) {
		return new Addon(getAddonSectionByKiller(killer).getConfigurationSection(id));
	}
	
	public static String getNameById(KillerTypes killer, String id) {
		return getAddonSectionByKiller(killer).getConfigurationSection(id).getString("name");
	}
}
