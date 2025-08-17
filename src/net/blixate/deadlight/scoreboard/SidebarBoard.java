package net.blixate.deadlight.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.blixate.deadlight.util.FormatUtil;
import net.md_5.bungee.api.ChatColor;

public class SidebarBoard {
	Objective obj;
	Scoreboard board;
	String title;
	int size = 0;
	boolean enabled;
	
	public SidebarBoard(String title) {
		this(title, Bukkit.getScoreboardManager().getNewScoreboard());
	}
	
	public SidebarBoard(String title, Scoreboard board) {
		this.title = title;
		this.board = board;
		this.enabled = true;
		newBoard();
	}
	
	public Scoreboard getBoard() {
		return board;
	}
	
	public void setEnabled(boolean b) {
		this.enabled = b;
	}
	
	public void addPlayer(Player player) {
		if(obj != null) { 
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			player.setScoreboard(board);
		}
	}
	
	private void newBoard() {
		if(!enabled) return;
		if(obj != null) {
			/* delete everything on this board */
			obj.unregister();
			for(Team t : board.getTeams()) {
				if(t.getName().startsWith("line"))
					t.unregister();
			}
		}
        obj = board.registerNewObjective("deadlight", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	@SuppressWarnings("deprecation")
	public void createLine(int lineNumber, String initialText) {
		if(!enabled) return;
		Team t = board.getTeam("line" + lineNumber);
		if(t == null) {
			t = board.registerNewTeam("line" + lineNumber);
		}
		/* This is used to get some color. This will not be displayed. */
		String entry = ChatColor.values()[lineNumber % ChatColor.values().length] + "";
		t.addEntry(entry);
		createStaticLine(lineNumber, entry);
		t.setPrefix(FormatUtil.color(initialText));
	}
	
	/* This is explicitly for lines that will NOT change POSITION OR VALUE! */
	public void createStaticLine(int lineNumber, String text) {
		if(!enabled) return;
		
		obj.getScore(text).setScore(lineNumber);
	}
	
	public void updateLine(int lineNumber, String newText) {
		if(!enabled) return;
		
		Team t = board.getTeam("line" + lineNumber);
		if(t == null) {
			createLine(lineNumber, newText);
			t = board.getTeam("line" + lineNumber);
		}
		String s = FormatUtil.color(newText);
		/* Check if this new text is different from the old text */
		if(t.getPrefix() != null && t.getPrefix().equals(s)) {
			return; // Its the same text, ignore.
		}
		t.setPrefix(s); // Different text!
	}
	
	public void updateSize(int size) {
		if(!enabled) return;
		
		if(this.size == size) return;
		this.size = size;
		
		newBoard();
		
		for(int i = 0; i < size; i++) {
			createLine(i, "");
		}
	}

	public int getSize() {
		return this.size;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void unregister() {
		obj.unregister();
	}
}
