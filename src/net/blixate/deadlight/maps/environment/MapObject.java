package net.blixate.deadlight.maps.environment;

import org.bukkit.Location;

public class MapObject {
	protected MapPosition position;
	protected Location offset;
	
	public MapObject(MapPosition pos, Location offset) {
		this.position = pos;
		this.offset = offset;
	}
	
	public void generate() {
		
	}
	
	public MapPosition getPosition() {
		return this.position;
	}
}
