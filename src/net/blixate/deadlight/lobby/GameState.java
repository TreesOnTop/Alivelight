package net.blixate.deadlight.lobby;

import org.bukkit.boss.BarColor;

public enum GameState {
	COMPLETE_FRAGMENTS, END_GAME_COLLAPSE;
	
	@Override
	public String toString() {
		switch(this) {
		case COMPLETE_FRAGMENTS: return "Fragments";
		case END_GAME_COLLAPSE: return "Portals";
		}
		return "???";
	}
	
	public TimeLimit createTimer(Lobby lobby) {
		switch(this) {
		case COMPLETE_FRAGMENTS: return new TimeLimit(lobby, "&9Match Time Limit", BarColor.BLUE);
		case END_GAME_COLLAPSE: return new TimeLimit(lobby, "&6End Game Collapse");
		}
		return null;
	}
	
	public double getTimeDuration() {
		switch(this) {
		case COMPLETE_FRAGMENTS: return 840;
		case END_GAME_COLLAPSE: return 150;
		}
		return Double.NaN;
	}
}
