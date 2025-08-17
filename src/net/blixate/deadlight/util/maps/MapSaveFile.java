package net.blixate.deadlight.util.maps;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;

import net.blixate.deadlight.Deadlight;

public class MapSaveFile {
	
	File file;
	
	public MapSaveFile(String name) {
		this.file = new File(Deadlight.dataFolder, name);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public HashMap<Integer, String> load() {
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		if(yml.contains("maps")) {
			HashMap<Integer, String> data = new HashMap<>();
			for(String key : yml.getConfigurationSection("maps").getKeys(false)) {
				data.put(Integer.parseInt(key), yml.getString("maps." + key));
			}
			return data;
		}
		return new HashMap<Integer, String>();
	}
	
	public void save(HashMap<Integer, String> data) {
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
		for(Entry<Integer, String> entry : data.entrySet()) {
			yml.set("maps." + entry.getKey(), entry.getValue());
		}
		try {
			yml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
