package net.blixate.deadlight.lobby;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.util.time.Cooldown;

public final class LobbyManager {
	
	Lobby[] lobbies;
	Cooldown[] lobbyLocks;
	
	public LobbyManager(int maxLobbies) {
		lobbies = new Lobby[maxLobbies];
		lobbyLocks = new Cooldown[maxLobbies];
		// initalize some data
		for(int i = 0; i < maxLobbies; i++) {
			lobbyLocks[i] = new Cooldown();
		}
	}
	
	public void lockLobby(int index, double time) {
		lobbyLocks[index].start(time);
	}
	
	public Lobby getLobby(int index) {
		if(index > lobbies.length || index < 0) {
			return null;
		}
		return lobbies[index];
	}
	
	public void setLobby(int index, Lobby lobby) {
		if(index > lobbies.length || index < 0) {
			return;
		}
		lobbies[index] = lobby;
	}
	
	public void removeLobby(int index) {
		if(index > lobbies.length || index < 0) {
			return;
		}
		lobbies[index] = null;
	}
	
	public Lobby[] getLobbies() {
		return lobbies;
	}
	
	public static LobbyManager getInstance() {
		return Deadlight.getLobbyManager();
	}

	public int getCount() {
		return lobbies.length;
	}

	public boolean isLobbyInhabitable(int index) {
		return lobbies[index] == null && lobbyLocks[index].isDone();
	}
}
