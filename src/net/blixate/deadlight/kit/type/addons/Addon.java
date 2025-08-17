package net.blixate.deadlight.kit.type.addons;

import org.bukkit.configuration.ConfigurationSection;

public class Addon {
	
	String id;
	String name;
	String desc;
	AddonRarity rarity;
	
	public Addon(ConfigurationSection section) {
		this.id = section.getName();
		this.name = section.getString("name");
		this.desc = section.getString("desc");
		this.rarity = AddonRarity.valueOf(section.getString("rarity"));
	}
	
	public String getName() {
		return this.name;
	}
}
