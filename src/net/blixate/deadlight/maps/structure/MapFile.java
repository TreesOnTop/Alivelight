package net.blixate.deadlight.maps.structure;

import org.bukkit.Location;
import org.bukkit.block.Block;

/** An abstract class to represent a map file. */
public abstract class MapFile {
	int sizeX, sizeY, sizeZ;
	
	public abstract void build(Location pos);
	
	public Block getBlockAt(Location offset, int x, int y, int z) {
		return offset.getWorld().getBlockAt(offset.getBlockX() + x, offset.getBlockY() + y, offset.getBlockZ() + z);
	}
	
	public Location getLocationAt(Location offset, int x, int y, int z) {
		return new Location(offset.getWorld(), offset.getBlockX() + x, offset.getBlockY() + y, offset.getBlockZ() + z);
	}
}
