package net.blixate.deadlight.maps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import net.blixate.deadlight.Deadlight;

public class MapClearer {
	private int idx;
	private BukkitTask task;
	private Location location;
	private int y;
	Material material;
	private int maxX = MapLoader.MAX_SIZE, maxY = 100, maxZ = MapLoader.MAX_SIZE;
	
	public MapClearer(int lobbyIdx) {
		this.idx = lobbyIdx;
		this.location = MapLoader.getMapLocation(idx);
		this.y = maxY;
		this.material = Material.AIR;
	}
	
	public MapClearer setMaterial(Material material) {
		this.material = material;
		return this;
	}
	
	public MapClearer setSize(int sizeX, int sizeY, int sizeZ) {
		this.maxX = sizeX;
		this.maxY = sizeY;
		this.maxZ = sizeZ;
		return this;
	}
	
	public void run() {
		this.y = maxY;
		task = Bukkit.getScheduler().runTaskTimer(Deadlight.inst, () -> tick(), 0, 1);
	}
	
	public void tick() {
		for(int x = location.getBlockX(); x < location.getBlockX() + maxX; x++) {
			for(int z = location.getBlockZ(); z < location.getBlockZ() + maxZ; z++) {
				Block block = Deadlight.getWorld().getBlockAt(x, y, z);
				if(block.getType() != material) {
					block.setType(material, false);
				}
			}
		}
		y--;
		if(y < 0) {
			task.cancel();
		}
	}
}
