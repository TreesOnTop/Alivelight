package net.blixate.deadlight.queue;

import java.util.HashSet;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.items.SpawnItems;

public class QueueLobby {
	public static final int MAX_PLAYERS_IN_QUEUE = 5;
	
	public HashSet<DLUser> players;

	public HashSet<DLUser> votesToStart;
	
	public QueueGui inventory;
	
	public QueueLobby() {
		players = new HashSet<>();
		votesToStart = new HashSet<>();
		this.inventory = new QueueGui(this);
	}
	
	public void addDLUser(DLUser player) {
		players.add(player);
		if(isFull()) {
			start();
		}else {
			updateItem(player);
		}
		inventory.update();
	}
	
	public void removeDLUser(DLUser player) {
		players.remove(player);
		votesToStart.remove(player);
		updateItem(player);
		inventory.update();
	}
	
	public void updateItem(DLUser player) {
		PlayerInventory inv = player.getInventory();
		ItemStack item = inv.getItem(2);
		if(item == null) {
			return;
		}
		if(item.getType().equals(Material.GRAY_DYE) || item.getType().equals(Material.LIME_DYE)) {
			if(player.isQueued()) {
				item = SpawnItems.leaveQueue.get();
			}else {
				item = SpawnItems.joinQueue.get();
			}
			inv.setItem(2, item);
		}
	}
	
	public boolean isFull() {
		return players.size() >= MAX_PLAYERS_IN_QUEUE;
	}
	
	public void start() {
		if(players.size() == 0) {
			return;
		}
		if(Deadlight.locked) {
			return;
		}
		int index = 0;
		while(index < Deadlight.getLobbyManager().getCount()) {
			if(Deadlight.getLobbyManager().isLobbyInhabitable(index)) {
				break;
			}
			index++;
		}
		if(index == Deadlight.getLobbyManager().getCount()) {
			for(DLUser p : players) {
				removeDLUser(p);
				p.send("user_no_lobbies_found");
			}
			return;
		}
		Lobby lobby = new Lobby(index, players.toArray(new DLUser[0]));
		Deadlight.getLobbyManager().setLobby(index, lobby);
		try {
			lobby.start();
		}catch(Throwable t) {
			Lobby.StartingState state = lobby.forceStop();
			Deadlight.inst.getLogger().log(Level.SEVERE, "Failed to start lobby at state " + state.toString());
			Deadlight.error(t);
			if(!players.isEmpty()) {
				String err = t.getClass().getSimpleName();
				if(t.getStackTrace().length > 0) {
					StackTraceElement top = t.getStackTrace()[0];
					err += ", " + top.getClassName() + ":" + top.getLineNumber();
				}else {
					err += ", no stack trace available.";
				}
				for(DLUser player : players) {
					player.send("queue_error", err);
				}
			}
		}
		clearQueue();
	}
	
	public void clearQueue() {
		/* Make sure no instances exist */
		DLUser[] iter = players.toArray(new DLUser[0]);
		for(DLUser p : iter) {
			removeDLUser(p);
		}
		players.clear();
		votesToStart.clear();
		inventory.update();
	}
}
