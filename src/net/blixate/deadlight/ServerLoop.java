package net.blixate.deadlight;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import net.blixate.deadlight.discord.DiscordManager;
import net.blixate.deadlight.discord.VerificationCode;
import net.blixate.deadlight.kit.offerings.OfferingManager;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.PlayerLoop;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.utils.holograms.HoloManager;

public class ServerLoop implements Runnable {
	
	public BukkitTask task;
	
	/* Timers */
	HashMap<String, Integer> timers;
	
	public ServerLoop() {
		timers = new HashMap<>();
	}
	
	/**
	 * Run every second the server is online.
	 * This is separate from {@link net.blixate.deadlight.player.PlayerLoop#run} and does not interact with player loops at all.
	 * 
	 * @see PlayerLoop
	 */
	public void run() {
		/* Send a Tip to all players */
		if(timer("tip")) {
			if(Bukkit.getOnlinePlayers().size() > 1) {
				String tip = MsgConfig.getTip();
				Bukkit.getOnlinePlayers().forEach((Player p) -> p.sendMessage(FormatUtil.color("&9&lTIP &b" + tip)));
			}
		}
		/* Auto-save player data */
		if(timer("save")) { Deadlight.inst.savePlayers(); }
		// Clear inactive things
		if(timer("holo")) { HoloManager.reloadAllHolograms(); }
		if(timer("offering")) {
			OfferingManager.clearInactive();
		}
		if(timer("discord check")) {
			if(DiscordManager.codes != null && !DiscordManager.codes.isEmpty()) {
				ArrayList<VerificationCode> newCodes = new ArrayList<>();
				for(VerificationCode code : DiscordManager.codes) {
					if(!code.isExpired()) {
						newCodes.add(code);
					}
				}
				DiscordManager.codes = newCodes;
			}
		}
		try {
			for(Lobby lobby : Deadlight.getLobbyManager().getLobbies()) {
				if(lobby == null) continue;
				lobby.tick();
			}
		}catch(Throwable e) {
			if(Deadlight.debug) {
				e.printStackTrace();
			}
		}
		
		// make shift entity garbage collecter
		if(timer("entity check timer")) {
			int removedEntities = 0;
			for(Entity entity : Deadlight.getWorld().getEntities()) {
				if(entity.getPersistentDataContainer().has(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER)) {
					boolean lobbyExists = false;
					int identifier = entity.getPersistentDataContainer().get(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER);
					for(Lobby lobby : Deadlight.getLobbyManager().getLobbies()) {
						if(lobby == null) continue;
						if(lobby.randomEntityLobbyId == identifier) { lobbyExists = true; break; }
					}
					if(!lobbyExists) {
						entity.remove();
						removedEntities ++;
					}
				}
			}
			if(removedEntities > 0) {
				Deadlight.debug("Entity Ticker: Removed " + removedEntities + " entities.");
			}
		}
	}
	
	private boolean timer(String timer) {
		int defaultValue = Deadlight.inst.getConfig().getInt("server loop." + timer + " timer");
		if(!timers.containsKey(timer)) {
			timers.put(timer, defaultValue);
		}
		timers.put(timer, timers.get(timer) - 1);
		if(timers.get(timer) <= 0) {
			timers.put(timer, defaultValue);
			return true;
		}
		return false;
	}
	
}
