package net.blixate.deadlight.commands;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.blixate.deadlight.Deadlight;

public class SpawnCommand extends DLCmd {
	
	@Override
	public void execute() {
		if(args.size() == 0) {
			if(user.isInMatch()) {
				user.getLobby().disconnect(user);
				user.teleportToSpawn();
			}
			else {
				user.teleportToSpawn(false);
			}
			user.send("user_tp_spawn");
		}else if(args.size() == 1) {
			if(checkArgEqualIgnoreCase(0, "set")) {
				try {
					Deadlight.setNewSpawn(user.getPlayer().getLocation());
				} catch (IOException e) {
					user.send("user_set_spawn_error", e.getMessage());
					e.printStackTrace();
					return;
				}
				user.send("user_set_spawn");
			}
		}
	}
	
	public static Location load(File f) {
		FileConfiguration spawnFile = YamlConfiguration.loadConfiguration(f);
		String world = spawnFile.getString("spawn.world");
		double x = spawnFile.getDouble("spawn.x");
		double y = spawnFile.getDouble("spawn.y");
		double z = spawnFile.getDouble("spawn.z");
		float yaw = (float)spawnFile.getDouble("spawn.yaw");
		float pitch = (float)spawnFile.getDouble("spawn.pitch");
		Location spawn;
		spawn = new Location(Bukkit.getWorld(world), x, y, z);
		spawn.setYaw(yaw);
		spawn.setPitch(pitch);
		return spawn;
	}
	public static void save(Location loc, File f) throws IOException {
		FileConfiguration spawnFile = YamlConfiguration.loadConfiguration(f);
		spawnFile.set("spawn.world", loc.getWorld().getName());
		spawnFile.set("spawn.x", loc.getX());
		spawnFile.set("spawn.y", loc.getY());
		spawnFile.set("spawn.z", loc.getZ());
		spawnFile.set("spawn.yaw", loc.getYaw());
		spawnFile.set("spawn.pitch", loc.getPitch());
		spawnFile.save(f);
	}

	
}
