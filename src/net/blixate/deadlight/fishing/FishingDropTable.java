package net.blixate.deadlight.fishing;

import java.io.File;
import java.util.HashMap;

import org.json.simple.JSONObject;

import net.blixate.deadlight.util.file.JSONRegistry;
import net.blixate.deadlight.util.random.DeadlightLootTable;
import net.blixate.deadlight.util.random.DeadlightLootTable.DropResult;

/** Maintainable class for doing fishing drops */
public class FishingDropTable {
	
	HashMap<String, DeadlightLootTable> table;
	
	public FishingDropTable(File f) {
		JSONRegistry registry = new JSONRegistry(f);
		JSONObject fishingDrops = registry.getObject("fishing");
		table = new HashMap<>();
		for(Object key : fishingDrops.keySet()) {
			table.put((String)key, new DeadlightLootTable((JSONObject)fishingDrops.get(key)));
		}
	}
	
	public DeadlightLootTable getTable(FishingRods rod) {
		return table.get(rod.table);
	}
	
	public DropResult pick(FishingRods rod) {
		return getTable(rod).pick();
	}
}
