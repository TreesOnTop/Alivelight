package net.blixate.deadlight.kit.cosmetics;

import org.bukkit.configuration.ConfigurationSection;

public class KillerSkin {
	
	private String name;
	public String head;
	public String armor;
	public String color;
	
	public int requirement;
	public String extra;
	
	public KillerSkin(String name, ConfigurationSection section) {
		this.name = name;
		this.head = section.getString("head");
		this.armor = section.getString("armor", "LEATHER");
		if(this.armor.equals("LEATHER")) {
			this.color = section.getString("color", "#000000");
		}
		this.extra = section.getString("extra", null);
	}
	
	public String getName() {
		return this.name;
	}
	
}
