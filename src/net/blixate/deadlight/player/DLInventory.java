package net.blixate.deadlight.player;

import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/** 
 * This holds the storage data for DLUser's items. Not used for GUI inventorys.
 *
 * @see net.blixate.deadlight.gui.GuiInventory
 */
public class DLInventory {
	HashMap<String, Integer> items = new HashMap<>();
	
	public DLInventory() {
		items = new HashMap<>();
	}
	
	public void load(YamlConfiguration yml) {
		ConfigurationSection invSec = yml.getConfigurationSection("inventory");
		if(invSec == null) return; // nothing to load
		for(String key : invSec.getKeys(false)) {
			items.put(key, invSec.getInt(key));
		}
	}
	
	public void save(YamlConfiguration yml) {
		ConfigurationSection invSec = yml.getConfigurationSection("inventory");
		if(invSec == null) {
			invSec = yml.createSection("inventory");
		}
		for(String key : items.keySet()) {
			invSec.set(key, items.get(key));
		}
	}
	
	public HashMap<String, Integer> getItems() {
		return items;
	}
	
	public String[] getKeys() {
		return items.keySet().toArray(new String[0]);
	}

	public int getItem(String id) {
		return items.getOrDefault(id, 0);
	}

	public void addItem(String item) {
		items.put(item, getItem(item)+1);
	}

	public void removeItem(String item) {
		removeItem(item, 1);
	}
	
	public void removeItem(String item, int amount) {
		if(items.containsKey(item)) {
			items.put(item, getItem(item) - amount);
			if(getItem(item) == 0) {
				items.remove(item);
			}
		}
	}
}
