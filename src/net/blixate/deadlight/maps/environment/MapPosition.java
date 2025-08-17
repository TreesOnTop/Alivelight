package net.blixate.deadlight.maps.environment;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import net.blixate.config.writer.ConfigSerializable;
import net.blixate.deadlight.Deadlight;
/**
 * Represents a position inside of a map, offset by where the map was loaded.
 * 
 * This is <b>{@code ConfigSerializable}</b>, meaning it can be serialized by BCFG File formats.
 */
public class MapPosition implements ConfigSerializable {
	World world;
	public int x, y, z;
	
	public MapPosition(int x2, int y2, int z2) {
		world = Bukkit.getWorld(Deadlight.DEFAULT_WORLD);
		x = x2;
		y = y2;
		z = z2;
	}
	
	public Location getLocation(Location offset) {
		if(offset == null) {
			Deadlight.debug("There is no offset for this position.");
			return null;
		}
		Location l = new Location(world, offset.getBlockX() + x, offset.getBlockY() + y, offset.getBlockZ() + z);
		return l;
	}
	
	public static MapPosition to(Location loc, Location offset) {
		return new MapPosition(
				loc.getBlockX()-offset.getBlockX(),
				loc.getBlockY()-offset.getBlockY(),
				loc.getBlockZ()-offset.getBlockZ());
	}
	
	/** Returns the distance between two MapPosition objects */
	public double distanceTo(MapPosition p) {
		return Math.sqrt(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2) + Math.pow(z - p.z, 2));
	}
	
	public Block getBlock(Location offset) {
		return getLocation(offset).getBlock();
	}

	@Override
	public String value() {
		return "{" + x + ", " + y + ", " + z + "}";
	}
	
	public String toString()
	{
		return "[" + x + ", " + y + ", " + z + "]";
	}
	public boolean equals(Object other) {
		if(!(other instanceof MapPosition)) return false;
		if(this == other) return true;
		MapPosition o = (MapPosition)other;
		return (o.world == world) && (o.x == x) && (o.y == y) && (o.z == z);
	}
}
