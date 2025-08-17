package net.blixate.deadlight.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONRegistry {

	protected File file;
	protected JSONObject json;
	protected JSONParser parser = new JSONParser();
	protected HashMap<String, Object> defaults = new HashMap<String, Object>();

	public JSONRegistry(File file) {
		this.file = file;
		reload();
	}
	
	public void load() {
		
	}
	
	public void reload() {
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				PrintWriter pw = new PrintWriter(file, "UTF-8");
				pw.print("{}");
				pw.flush();
				pw.close();
			}
			FileInputStream stream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
			json = (JSONObject) parser.parse(reader);
			load();
			reader.close();
			stream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void set(String key, Object data) {
		if(data == null) {
			defaults.remove(key);
			return;
		}
		defaults.put(key, data);
	}

	@SuppressWarnings("unchecked")
	public boolean save() {
		try {
			JSONObject toSave = new JSONObject();
			for (String s : defaults.keySet()) {
				Object o = defaults.get(s);
				if (o instanceof String) {
					toSave.put(s, getString(s));
				} else if (o instanceof Double) {
					toSave.put(s, getDouble(s));
				}else if (o instanceof Boolean) {
					toSave.put(s, getBoolean(s));
				} else if (o instanceof Integer) {
					toSave.put(s, getInteger(s));
				} else if (o instanceof JSONObject) {
					toSave.put(s, getObject(s));
				} else if (o instanceof JSONArray) {
					toSave.put(s, getArray(s));
				}
			}
		
			TreeMap<String, Object> treeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
			treeMap.putAll(toSave);
			
			Gson g = new GsonBuilder().create();
		 	String prettyJsonString = g.toJson(treeMap);
			java.io.FileWriter fw = new java.io.FileWriter(file);
			fw.write(prettyJsonString);
			fw.flush();
			fw.close();
		
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
 
	public String getRawData(String key) {
		return defaults.containsKey(key) ? defaults.get(key).toString()
				: (json.containsKey(key) ? json.get(key).toString() : key);
	}
	
	public String getString(String key) {
		return getRawData(key);
	}
	
	public boolean getBoolean(String key) {
		return Boolean.valueOf(getRawData(key));
	}
	
	public double getDouble(String key) {
		try {
			return Double.parseDouble(getRawData(key));
		} catch (Exception ex) { }
		return -1;
	}
	
	public double getInteger(String key) {
		try {
			return Integer.parseInt(getRawData(key));
		} catch (Exception ex) { }
		return -1;
	}
	
	public JSONObject getObject(String key) {
		return json.containsKey(key) ? (JSONObject) json.get(key)
			: (defaults.containsKey(key) ? (JSONObject) defaults.get(key) : new JSONObject());
	}
	
	public JSONArray getArray(String key) {
		return json.containsKey(key) ? (JSONArray) json.get(key)
			: (defaults.containsKey(key) ? (JSONArray) defaults.get(key) : new JSONArray());
	}
	
}