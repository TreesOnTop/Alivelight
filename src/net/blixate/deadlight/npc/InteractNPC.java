package net.blixate.deadlight.npc;

import net.blixate.deadlight.util.PureChatColor;
import net.md_5.bungee.api.ChatColor;

public enum InteractNPC {
	FISHER("Sea Monster", PureChatColor.GREEN, "Rent-a-Rod"),
	DEMON("The Plague's Monster", PureChatColor.RED),
	AKURA("Mystery Man", PureChatColor.MAGENTA, "Mystery Market"),
	CRESCENT("Exterminator", PureChatColor.RED),
	KERMINSKI("Site Director", PureChatColor.RED),
	MILITARY("Military Officer", PureChatColor.RED),
	TUTORIAL("Tutorial", PureChatColor.GREEN, ChatColor.BOLD + "?"),
	CHALLENGES("Charlegis", PureChatColor.MAGENTA, "Daily Challenges"),
	GAMES("View Active Games", PureChatColor.GREEN, "Spectating");
	
	static {
		KERMINSKI.interactable = false;
	}
	
	String name;
	ChatColor nameColor;
	String aboveNameTag;
	
	boolean interactable = true;
	
	InteractNPC(String name, ChatColor nameColor) {
		this(name, nameColor, null);
	}
	
	InteractNPC(String name, ChatColor nameColor, String aboveNameTag) {
		this.name = name;
		this.nameColor = nameColor;
		this.aboveNameTag = aboveNameTag;
		
	}
	
	public boolean isInteractable(String npcName) {
		return this.name().equalsIgnoreCase(npcName) && !interactable;
	}
	
	public static InteractNPC get(String npcName) {
		for(InteractNPC npc : InteractNPC.values()) {
			if(npc.name().equalsIgnoreCase(npcName)) {
				return npc;
			}
		}
		return null;
	}
	
	public String toString() {
		return nameColor + name;
	}
}
