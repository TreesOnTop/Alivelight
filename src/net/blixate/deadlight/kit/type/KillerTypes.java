package net.blixate.deadlight.kit.type;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.configuration.ConfigurationSection;

import net.blixate.deadlight.Deadlight;

public enum KillerTypes {
	SCRUBBER("Scrubber", ScrubberType.class),
	HUNTER("Hunter", HunterType.class),
	CLOAKER("Cloaker", CloakerType.class),
	WARPER("Warper", WarperType.class),
	JACK("Jack o' Lantern", JackOLanternType.class),
	VISITOR("Visitor", VisitorType.class),
	NECROMANCER("Necromancer", NecromancerType.class);
	
	Class<? extends KillerType> cls;
	String name;
	boolean includeInRandomize = false;
	
	KillerTypes(String name, Class<? extends KillerType> clazz) {
		this(name, clazz, true);
	}
	
	KillerTypes(String name, Class<? extends KillerType> clazz, boolean includeInRandom) {
		this.cls = clazz;
		this.name = name;
		this.includeInRandomize = includeInRandom;
	}
	
	public String getName() {
		return this.name;
	}
	
	public KillerType create() {
		try {
			return cls.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ConfigurationSection getData() {
		return Deadlight.abilities.getConfigurationSection("killer types." + name().toLowerCase());
	}
	
	public File getDocumentFile() {
		return null;
	}
	
	public static KillerTypes getByClass(Class<? extends KillerType> clazz) {
		for(KillerTypes type : KillerTypes.values()) {
			if(type.cls.equals(clazz)) {
				return type;
			}
		}
		return null;
	}
}
