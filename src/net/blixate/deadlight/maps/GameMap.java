package net.blixate.deadlight.maps;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.maps.environment.MapObject;
import net.blixate.deadlight.maps.environment.MapPosition;
public class GameMap {
	public static final int MAX_EXIT_PORTALS = 2;
	public static final int MAX_GENERATORS = 6;
	public static final String MAP_VERSION = "1.2";
	
	MapRegistry file;
	
	String mapName;
	
	Location offset;
	
	String version;
	
	boolean disabled = false;
	boolean beta = false;
	
	MapPosition[] generators;
	MapPosition[] exitPortals;
	MapPosition[] hatchPoints;
	MapPosition[] spawns;
	
	public GameMap(String name) {
		this.mapName = name;
		File f = new File(Deadlight.dataFolder, "maps/" + name + ".json");
		if (!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		file = new MapRegistry(f);
		this.spawns = file.arrayToMapPosition(file.spawns);
		this.generators = file.arrayToMapPosition(file.generators);
		this.exitPortals = file.arrayToMapPosition(file.portals);
		this.disabled = file.getBoolean("disabled");
		this.beta = file.getBoolean("beta");
	}
	
	public void startMap(MapObject[] gens, MapObject[] portals) {
		byte b;
		int i;
		MapObject[] arrayOfMapObject;
		for (i = (arrayOfMapObject = gens).length, b = 0; b < i; ) {
			MapObject gen = arrayOfMapObject[b];
			gen.generate();
			b++;
		} 
		for (i = (arrayOfMapObject = portals).length, b = 0; b < i; ) {
			MapObject portal = arrayOfMapObject[b];
			portal.generate();
			b++;
		} 
	}
	
	public Location[] createSpawns(Location offset) {
		MapPosition spawn1 = this.spawns[Deadlight.RNG.nextInt(this.spawns.length)];
		MapPosition spawn2 = null;
		double highestDistance = 0.0D;
		byte b;
		int i;
		MapPosition[] arrayOfMapPosition;
		for (i = (arrayOfMapPosition = this.spawns).length, b = 0; b < i; ) {
			MapPosition spawn = arrayOfMapPosition[b];
			double distance = spawn.distanceTo(spawn1);
			if (distance > highestDistance) {
				spawn2 = spawn;
				highestDistance = spawn.distanceTo(spawn1);
			} 
			b++;
		} 
		return new Location[] { spawn1.getLocation(offset), spawn2.getLocation(offset) };
	}
	
	public String getName() {
		return this.mapName;
	}
	
	public String getFancyName() {
		return this.file.name;
	}
	
	public String toString() {
		return this.file.name == null ? getName() : getFancyName();
	}
	
	public void setOffsetPosition(Location loc) {
		this.offset = loc;
	}
	
	public void addSpawn(MapPosition location) {
		if (this.spawns == null)
			this.spawns = new MapPosition[0]; 
		MapPosition[] spawn = new MapPosition[this.spawns.length + 1];
		System.arraycopy(this.spawns, 0, spawn, 0, this.spawns.length);
		spawn[this.spawns.length] = location;
		this.spawns = spawn;
	}
	
	public void addHatch(MapPosition location) {
		if (this.hatchPoints == null)
			this.hatchPoints = new MapPosition[0]; 
		MapPosition[] spawn = new MapPosition[this.hatchPoints.length + 1];
		System.arraycopy(this.hatchPoints, 0, spawn, 0, this.hatchPoints.length);
		spawn[this.hatchPoints.length] = location;
		this.hatchPoints = spawn;
	}
	
	public void addGenerator(MapPosition location) {
		if (this.generators == null)
			this.generators = new MapPosition[0]; 
		MapPosition[] gens = new MapPosition[this.generators.length + 1];
		System.arraycopy(this.generators, 0, gens, 0, this.generators.length);
		gens[this.generators.length] = location;
		this.generators = gens;
	}
	
	public void addExitPortal(MapPosition location) {
		if (this.exitPortals == null)
			this.exitPortals = new MapPosition[0]; 
		MapPosition[] exitPortals2 = new MapPosition[this.exitPortals.length + 1];
		System.arraycopy(this.exitPortals, 0, exitPortals2, 0, this.exitPortals.length);
		exitPortals2[this.exitPortals.length] = location;
		this.exitPortals = exitPortals2;
	}
	
	public void saveSettings() throws IOException {
		Deadlight.debug("Saving map data for " + mapName);
		this.file.set("name", mapName);
		this.file.set("version", MAP_VERSION);
		this.file.set("spawns", this.spawns);
		this.file.set("generators", this.generators);
		this.file.set("portals", this.exitPortals);
		this.file.set("disabled", disabled);
		this.file.set("beta", beta);
		
		boolean success = this.file.save();
		if(success) {
			Deadlight.debug("Successfully saved map data!");
		} else {
			Deadlight.debug("Something went wrong while saving the map data.");
		}
	}
	
	public Location getOffset() {
		return this.offset;
	}
	
	public MapPosition[] getGenerators() {
		return this.generators;
	}
	
	public MapPosition[] getSpawns() {
		return this.spawns;
	}
	
	public MapPosition[] getPortals() {
		return this.exitPortals;
	}
	
	public MapRegistry getRegistry() {
		return this.file;
	}

	public void clearAllData() {
		spawns = new MapPosition[0];
		generators = new MapPosition[0];
		exitPortals = new MapPosition[0];
		offset = null;
	}

	public boolean isBeta() {
		return beta;
	}

	public void setBeta(boolean b) {
		this.beta = b;
		if(b && !MapLoader.playableMaps.contains(this)) {
			MapLoader.playableMaps.add(this);
		}else if(!b && MapLoader.playableMaps.contains(this)) {
			MapLoader.playableMaps.remove(this);
		}
	}
}