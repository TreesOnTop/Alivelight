package net.blixate.deadlight.util.maps;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.util.FormatUtil;

public class MapManager {
	static HashMap<Integer, String> data = new HashMap<Integer, String>();
	
	public static MapSaveFile file = new MapSaveFile("maps.yml");
	
	public static void init() {
		data = file.load();
	}
	public static void uninit() {
		file.save(data);
	}
	
	public static void addMapId(int mapId, String url) {
		data.put(mapId, url);
	}
	
	public static String getMapUrl(int mapId) {
		return data.get(mapId);
	}
	public static boolean hasImage(int id) {
		return data.containsKey(id);
	}
	public static int getMapWithImage(String url) {
		for(Entry<Integer, String> entry : data.entrySet()) {
			if(entry.getValue().equals(url)) {
				return entry.getKey();
			}
		}
		return -1;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getMapItem(int mapId, String name) {
		ItemStack map = new ItemStack(Material.FILLED_MAP);
		MapMeta meta = (MapMeta)map.getItemMeta();
		if(name != null) {
			meta.setDisplayName(FormatUtil.color(name));
		}
		meta.setMapView(Bukkit.getMap(mapId));
		map.setItemMeta(meta);
		return map;
	}
	
	@SuppressWarnings("deprecation")
	public static MapView getOrCreateMap(MapRenderer renderer, String url) {
		MapView view;
		if(MapManager.getMapWithImage(url) != -1) {
			int mapId = MapManager.getMapWithImage(url);
			view = Bukkit.getMap(mapId);
		}
		else {
			view = Bukkit.createMap(Deadlight.getWorld());
			view.getRenderers().clear();
			view.addRenderer(renderer);
		}
		addMapId(view.getId(), url);
		return view;
	}
	public static MapRenderer createRenderer(File file) {
		return new ImageRenderer(file);
	}
	
	public static MapRenderer createRenderer(String url) {
		ImageRenderer render = new ImageRenderer();
		render.load(url);
		return render;
	}
}
