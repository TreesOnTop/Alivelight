package net.blixate.deadlight.maps;

import java.io.File;

import org.json.simple.JSONArray;

import net.blixate.deadlight.maps.environment.MapPosition;
import net.blixate.deadlight.util.file.JSONRegistry;

public class MapRegistry extends JSONRegistry {
	
	String name;
	String version;
	
	long[][] generators;
	long[][] spawns;
	long[][] portals;
	// unimplemented as of now
	long[][] totems;
	long[][] hatchSpawns;
	long[][] chests;
	
	public MapRegistry(File file) {
		super(file);
	}
	
	public void load() {
		name = getString("name");
		version = getString("version");
		generators = load2DArray("generators");
		portals = load2DArray("portals");
		spawns = load2DArray("spawns");
	}
	
	public long[][] load2DArray(String property) {
		JSONArray array = getArray(property);
		long[][] result = new long[array.size()][];
		for(int i = 0; i < array.size(); i++) {
			JSONArray arr = (JSONArray) array.get(i);
			result[i] = new long[arr.size()];
			for(int j = 0; j < 3; j++) {
				result[i][j] = (Long)arr.get(j);
			}
		}
		return result;
	}
	
	public MapPosition[] arrayToMapPosition(long[][] pos) {
		MapPosition[] positions = new MapPosition[pos.length];
		for(int i = 0; i < pos.length; i++) {
			positions[i] = new MapPosition((int)pos[i][0], (int)pos[i][1], (int)pos[i][2]);
		}
		return positions;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray toArray(MapPosition[] positions) {
		JSONArray array = new JSONArray();
		for(int i = 0; i < positions.length; i++) {
			MapPosition position = positions[i];
			JSONArray pos = new JSONArray();
			pos.add(position.x);
			pos.add(position.y);
			pos.add(position.z);
			array.add(pos);
		}
		return array;
	}
	
	public void set(String key, String value) {
		defaults.put(key, value);
	}
	
	public void set(String key, MapPosition[] value) {
		defaults.put(key, toArray(value));
	}
}