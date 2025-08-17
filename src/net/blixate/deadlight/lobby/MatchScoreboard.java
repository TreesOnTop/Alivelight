package net.blixate.deadlight.lobby;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.blixate.deadlight.player.DLUser;

public class MatchScoreboard {
	
	Lobby lobby;
	HashMap<DLUser, Scoreboard> scoreboards;
	String title;
	
	public MatchScoreboard(Lobby lobby, String title) {
		this.lobby = lobby;
		this.title = title;
	}
	
	public void createPlayerScoreboard(DLUser user) {
		Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = board.registerNewObjective("deadlight", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
}
