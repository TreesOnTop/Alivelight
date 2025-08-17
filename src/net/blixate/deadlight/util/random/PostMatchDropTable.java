package net.blixate.deadlight.util.random;

import java.io.File;
import java.util.HashMap;

import javax.annotation.Nullable;

import org.json.simple.JSONObject;

import net.blixate.deadlight.util.file.JSONRegistry;

public class PostMatchDropTable {
	
	// keys
	// If the two other conditions aren't met, use this table.
	public static final String KEY_GLOBAL = "global";
	// If the survivor escapes
	public static final String KEY_SURVIVOR_WIN = "escape";
	// If the killer kills everybody
	public static final String KEY_KILLER_WIN = "nooneEscapesDeath";
	
	HashMap<String, DeadlightLootTable> table;
	
	public PostMatchDropTable(File f) {
		JSONRegistry registry = new JSONRegistry(f);
		JSONObject drops = registry.getObject("postmatch");
		table = new HashMap<>();
		for(Object key : drops.keySet()) {
			table.put((String)key, new DeadlightLootTable((JSONObject)drops.get(key)));
		}
	}
	
	public @Nullable DeadlightLootTable getTable(String key) {
		if(!table.containsKey(key)) {
			return null;
		}
		return table.get(key);
	}
	
}
