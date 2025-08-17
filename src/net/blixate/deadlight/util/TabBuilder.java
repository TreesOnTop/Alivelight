package net.blixate.deadlight.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabBuilder {
	ArrayList<String> results;
	public TabBuilder() {
		results = new ArrayList<String>();
	}
	
	public void addPlayers() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			results.add(p.getName());
		}
	}
	
	public void matchResults(String matchAgainst) {
		ArrayList<String> s = new ArrayList<String>();
		for(String str : results) {
			if(str.startsWith(matchAgainst)) {
				s.add(str);
			}
		}
		results = s;
	}
	
	/* Only match players */
	public void matchPlayers(String argument) {
		addPlayers();
		matchResults(argument);
	}
	
	public void addSelectors() {
		results.add("@a");
		results.add("@p");
		results.add("@r");
		results.add("@s");
		results.add("@e");
	}
	
	public void addCoordX(Player p) {
		results.add("~");
		results.add(p.getLocation().getX() + "");
	}
	
	public void addCoordY(Player p) {
		results.add("~");
		results.add(p.getLocation().getY() + "");
	}
	
	public void addCoordZ(Player p) {
		results.add("~");
		results.add(p.getLocation().getZ() + "");
	}
	
	public void addString(String s) {
		results.add(s);
	}
	
	public void addStrings(String...strings) {
		for(String s : strings) {
			results.add(s);
		}
	}
	
	public List<String> build() {
		return results;
	}
}
