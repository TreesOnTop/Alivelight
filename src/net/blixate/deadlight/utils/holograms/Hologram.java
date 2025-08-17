package net.blixate.deadlight.utils.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//Holograms are just a location and text
public class Hologram {
	
	
	private Location loc;
	private String text;
	private ArmorStand entity;
	private World world;
	
	private String permName = null;
	
	public Hologram(Location loc, String text) {
		this.loc = loc;
		this.text = text;
		this.world = loc.getWorld();
	}
	
	public Hologram(JSONObject object) {
		permName = (String)object.get("id");
		world = Bukkit.getWorld((String)object.get("world"));
		text = (String)object.get("text");
		JSONArray arr = (JSONArray)object.get("location");
		loc = new Location(world, (Double)arr.get(0), (Double)arr.get(1), (Double)arr.get(2));
	}
	
	public void createPermanentHologram(String id) {
		this.permName = id;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getJSONObject() {
		if(permName == null) {
			throw new IllegalStateException("Cannot get JSON of a non-permanent hologram!");
		}
		JSONObject object = new JSONObject();
		
		JSONArray arr = new JSONArray();
		arr.add(loc.getX());
		arr.add(loc.getY());
		arr.add(loc.getZ());
		object.put("id", permName);
		object.put("location", arr);
		object.put("world", world.getName());
		object.put("text", text);
		return object;
	}
	
	public boolean isPermanent() {
		return permName != null;
	}
	
	public void spawn() {
		entity = world.spawn(loc, ArmorStand.class, (entity) -> {
			if(isPermanent()) {
				entity.getPersistentDataContainer().set(HoloManager.HOLO_KEY, PersistentDataType.BYTE, (byte)3);
				entity.getPersistentDataContainer().set(HoloManager.HOLO_ID, PersistentDataType.STRING, permName);
			}else {
				entity.getPersistentDataContainer().set(HoloManager.HOLO_KEY, PersistentDataType.BYTE, (byte)1);
			}
			
			entity.setInvisible(true);
			entity.setInvulnerable(true);
			entity.setSmall(true);
			entity.setArms(false);
			entity.setGravity(false);
			entity.setBasePlate(false);
			entity.setCustomNameVisible(true);
			entity.setCustomName(text);
		});
	}
	
	public void remove() {
		if(exists()) {
			entity.remove();
		}
	}
	
	public boolean exists() {
		return entity != null && !entity.isDead();
	}
}