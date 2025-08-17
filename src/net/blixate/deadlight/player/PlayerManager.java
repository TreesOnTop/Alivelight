package net.blixate.deadlight.player;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.blixate.deadlight.Deadlight;

public class PlayerManager {
	static HashMap<UUID, DLUser> players = new HashMap<>();
	
	public static boolean exists(UUID uuid) {
		return players.containsKey(uuid);
	}
	
	public static boolean exists(Player p) {
		return exists(p.getUniqueId());
	}
	
	public static DLUser getUser(UUID uuid) {
		return players.get(uuid);
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static DLUser getUser(String username) {
		return players.get(Bukkit.getPlayer(username));
	}
	
	@SuppressWarnings("deprecation")
	public static DLData getOfflineUser(String username) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(username);
		if(player.isOnline()) {
			return getUser(player.getUniqueId());
		}
		if(player == null || !player.hasPlayedBefore()) {
			return null;
		}
		if(!PlayerManager.getPlayerFile(player.getUniqueId()).exists()) {
			
		}
		return getOfflineUser(player.getUniqueId());
	}
	
	public static DLData getOfflineUser(UUID uuid) {
		if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
			return getUser(uuid);
		}
		DLData data = new DLData(uuid);
		data.loadPlayerData();
		return data;
	}
	
	public static DLUser createUser(Player p) {
		DLUser user = new DLUser(p);
		if(players.put(p.getUniqueId(), user) != null) {
			Deadlight.debug(p.getUniqueId() + " was already registered!");
		}
		return user;
	}

	public static void removeUser(Player p) {
		players.remove(p.getUniqueId());
	}

	public static DLUser getUser(CommandSender sender) {
		return getUser(((Player)sender).getUniqueId());
	}
	
	public static DLUser getUser(Player p) {
		return getUser(p.getUniqueId());
	}
	
	public static Collection<DLUser> getPlayers() {
		return players.values();
	}
	
	public static File getPlayerFile(String uuid) {
		return new File(Deadlight.dataFolder, "data/" + uuid + ".json");
	}
	
	public static File getPlayerFile(UUID uuid) {
		return new File(Deadlight.dataFolder, "data/" + getUUIDString(uuid) + ".json");
	}
	
	public static String getUUIDString(UUID uuid) {
		return uuid.toString().replaceAll("-", "");
	}

	public static File getPlayerFolder() {
		return new File(Deadlight.dataFolder, "data");
	}
}
