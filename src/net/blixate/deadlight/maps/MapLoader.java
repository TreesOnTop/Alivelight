package net.blixate.deadlight.maps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.maps.structure.StructureFile;

public class MapLoader {
	public static double PRIVATE_LOBBY_LOAD_AREA_X = 100.0D;
	
	public static int MAX_SIZE = 255;
	public static int Y_SIZE = 50;
	public static int MAP_LOADING_OFFSET = 100;
	public static int SPACING = 10;
	public static int Y_LOAD = 0;
	public static int X_OFFSET = 0;
	
	public static String DEBUG_FILE = "map_debug.dbg";
	
	static GameMap[] maps;
	static List<GameMap> playableMaps;
	
	static HashMap<String, StructureFile> structures;
	
	static GameMap forcedMap;
	
	public static void loadMaps() {
		List<String> cfgMaps = new ArrayList<>();
		File mapDirectory = new File(Deadlight.dataFolder, "maps");
		for(String fileName : mapDirectory.list()) {
			if(fileName.endsWith(".json")) {
				cfgMaps.add(fileName.substring(0, fileName.length()-5));
			}
		}
		playableMaps = new ArrayList<>();
		ArrayList<GameMap> tMaps = new ArrayList<>();
		structures = new HashMap<>();
		for (int b = 0; b < cfgMaps.size(); b++) {
			String map = cfgMaps.get(b);
			GameMap gameMap;
			StructureFile file;
			try {
				gameMap = new GameMap(map);
				file = new StructureFile(getStructureFile(map)).load();
				
			} catch (IOException|RuntimeException e) {
				Deadlight.error(e);
				continue;
			} 
			structures.put(map, file);
			tMaps.add(gameMap);
			if(!gameMap.beta) {
				playableMaps.add(gameMap);
			}
		}
		maps = tMaps.<GameMap>toArray(new GameMap[0]);
		Deadlight.debug("Loaded " + tMaps.size() + " maps");
	}
	
	public static GameMap getMap(int index) {
		return maps[index];
	}
	
	public static GameMap getMap(String name) {
		byte b;
		int i;
		GameMap[] arrayOfGameMap;
		for (i = (arrayOfGameMap = maps).length, b = 0; b < i; ) {
			GameMap map = arrayOfGameMap[b];
			if (map.getName().equals(name))
				return map; 
			b++;
		} 
		return null;
	}
	
	public static Location getMapLocation(int lobbyIdx) {
		World world = Bukkit.getWorld(Deadlight.DEFAULT_WORLD);
		int x = X_OFFSET;
		int y = Y_LOAD;
		int z = MAP_LOADING_OFFSET + (MAX_SIZE + SPACING) * lobbyIdx;
		return new Location(world, x, y, z);
	}
	
	public static Location loadMap(String name, int lobbyIdx) throws IOException {
		Location mapLocation = getMapLocation(lobbyIdx);
		StructureFile file = structures.get(name);
		file.build(mapLocation);
		return mapLocation;
	}
	
	public static StructureFile getStructure(String name) {
		return structures.get(name);
	}
	
	public static GameMap getRandomMap() {
		if(forcedMap != null) {
			GameMap map = forcedMap;
			forcedMap = null;
			return map;
		}
		return playableMaps.get(Deadlight.RNG.nextInt(playableMaps.size()));
	}
	
	public static File getStructureFile(String name) {
		return new File(Deadlight.dataFolder, "maps/" + name + ".bld");
	}
	
	public static GameMap[] getMaps() {
		return maps;
	}
	
	public static File getMapDebugFile() throws IOException {
		File f = new File(Deadlight.dataFolder, "maps/" + DEBUG_FILE);
		if (!f.exists())
			f.createNewFile(); 
		return f;
	}
	
	public static File getMapDebugFile(String name) throws IOException {
		File f = new File(Deadlight.dataFolder, "maps/" + name + "_" + DEBUG_FILE);
		if (!f.exists())
			f.createNewFile(); 
		return f;
	}
	
	public static File getMapFile(String name) {
		return new File(Deadlight.dataFolder, "maps/" + name + ".json");
	}

	public static MapRegistry createMapRegistry(String mapName) {
		MapRegistry registry = new MapRegistry(getMapFile(mapName));
		return registry;
	}

	public static void setForcedMap(GameMap map) {
		forcedMap = map;
	}
}

