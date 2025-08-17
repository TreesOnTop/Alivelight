package net.blixate.deadlight.kit.perks;

import java.lang.reflect.InvocationTargetException;

import net.blixate.config.ConfigArray;
import net.blixate.config.ConfigProperty;
import net.blixate.config.ConfigSection;
import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.Alignment;

public class PerkRegistry {
	public String id;
	public String name;
	public String description;
	public PerkEventType event;
	public Alignment alignment;
	public boolean disabled = false;
	public ConfigArray properties;
	
	public Class<?> perkClass;
	
	public PerkRegistry(String id, Alignment alignment, ConfigSection section) {
		this.id = id;
		this.alignment = alignment;
		try {
			perkClass = Class.forName(alignment.getPerkClass(this.id));
		} catch (ClassNotFoundException e) {
			perkClass = null;
			Deadlight.debug("Implementation for " + id + " not found!");
		}
		name = getPropertyRequired(section, "name").getAsString();
		description = getPropertyRequired(section, "description").getAsString();
		var event = getPropertyIfExists(section, "event").getAsString();
		if(event != null) {
			this.event = PerkEventType.valueOf(event);
		}
		
		var disabledProperty = getPropertyIfExists(section, "disabled");
		if(disabledProperty != null && !disabledProperty.isNull()) {
			disabled = disabledProperty.getAsBoolean();
		}
		var tierProps = getPropertyIfExists(section, "tier");
		if(tierProps != null && !tierProps.isNull()) {
			properties = tierProps.asArray();
		}
	}
	
	public String getDescription(int tier) {
		return "";
	}
	
	public String getTierProperty(int tier) {
		if(tier > 3) {
			return "??";
		}
		return getTier(tier).getAsInt() + "";
	}
	
	public ConfigProperty getTier(int tier) {
		return properties.get(tier - 1);
	}
	
	public Perk createInstance() {
		if(!hasImplementation()) {
			return null;
		}
		try{
			return (Perk) perkClass.getConstructor().newInstance();
		}
		catch(IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean hasImplementation() {
		return perkClass != null;
	}
	
	/** Return a property in the config section, only if it exists. If it doesn't, this returns null */
	public static ConfigProperty getPropertyIfExists(ConfigSection section, String property) {
		if(section.hasProperty(property)) {
			return section.getProperty(property);
		}
		return null;
	}
	/** Return a property in the config section, only if it exists. If it doesn't, throw an error. */
	public static ConfigProperty getPropertyRequired(ConfigSection section, String property) {
		if(section.hasProperty(property)) {
			return section.getProperty(property);
		}
		throw new RuntimeException("Invalid perk registry: "+section.getName()+" doesn't have " + property + "!");
	}
}
