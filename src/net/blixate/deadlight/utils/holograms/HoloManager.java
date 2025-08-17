package net.blixate.deadlight.utils.holograms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Lists;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.util.file.JSONRegistry;

public final class HoloManager {
	public static final NamespacedKey HOLO_KEY = new NamespacedKey(Deadlight.inst, "hologram");
	public static final NamespacedKey HOLO_ID = new NamespacedKey(Deadlight.inst, "hologramId");
	
	public static ArrayList<Hologram> holograms = new ArrayList<>();
	public static JSONRegistry hologramsFile;
	
	private HoloManager() {}
	
	public static void loadPermanentHolograms() {
		File file = new File(Deadlight.dataFolder, "holograms.json");
		if(!file.exists()) {
			return;
		}
		hologramsFile = new JSONRegistry(file);
		JSONArray holoList = hologramsFile.getArray("holograms");
		for(Object object : holoList) {
			JSONObject json = (JSONObject)object;
			Hologram holo = new Hologram(json);
			holo.spawn();
			holograms.add(holo);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void savePermanentHolograms() {
		if(hologramsFile == null) {
			File file = new File(Deadlight.dataFolder, "holograms.json");
			if(!file.exists() && getPermanentHolograms().size() > 0) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}else {
				return;
			}
			hologramsFile = new JSONRegistry(file);
		}
		JSONArray hologramArray = new JSONArray();
		for(Hologram hologram : getPermanentHolograms()) {
			if(hologram.isPermanent()) {
				hologramArray.add(hologram.getJSONObject());
			}
		}
		hologramsFile.set("holograms", hologramArray);
		hologramsFile.save();
	}
	
	public static List<Hologram> getPermanentHolograms() {
		List<Hologram> holos = new ArrayList<>();
		for(Hologram hologram : holograms) {
			if(hologram.isPermanent()) {
				holos.add(hologram);
			}
		}
		return holos;
	}
	
	public static List<Hologram> getHolograms() {
		return Lists.newArrayList(holograms);
	}
	
	public static void reloadAllHolograms() {
		// just to make sure
		for(World world : Bukkit.getWorlds()) {
			for(Entity entity : world.getEntitiesByClass(ArmorStand.class)) {
				if(isHologram(entity)) {
					entity.remove();
				}
			}
		}
		for(Hologram holo : holograms) {
			if(!holo.exists()) {
				holo.spawn();
			}
		}
	}
	
	public static void removeAllHolograms() {
		for(Hologram holo : holograms) {
			if(holo.exists()) {
				holo.remove();
			}
		}
		// just to make sure
		for(World world : Bukkit.getWorlds()) {
			for(Entity entity : world.getEntitiesByClass(ArmorStand.class)) {
				if(isHologram(entity)) {
					entity.remove();
				}
			}
		}
	}
	
	public static Hologram spawnHologram(Location loc, String text) {
		Hologram holo = new Hologram(loc.clone(), text);
		holo.spawn();
		holograms.add(holo);
		return holo;
	}
	
	public static Hologram spawnTemporaryHologram(Location loc, String text, double duration) {
		final Hologram holo = new Hologram(loc.clone(), text);
		holo.spawn();
		Bukkit.getScheduler().runTaskLater(Deadlight.inst, () -> {
			holo.remove();
		}, (long)(20d * duration));
		return holo;
	}
	
	public static Hologram createPermanentHologram(String id, Location loc, String text) {
		final Hologram holo = new Hologram(loc.clone(), text);
		holo.createPermanentHologram(id);
		holo.spawn();
		holograms.add(holo);
		return holo;
	}
	
	public static boolean isHologram(Entity entity) {
		return entity.getPersistentDataContainer().has(HOLO_KEY, PersistentDataType.BYTE);
	}
}
