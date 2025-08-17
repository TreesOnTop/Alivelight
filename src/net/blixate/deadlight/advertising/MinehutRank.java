package net.blixate.deadlight.advertising;

import org.json.simple.JSONObject;

import net.blixate.deadlight.util.FormatUtil;
import net.md_5.bungee.api.ChatColor;

public class MinehutRank {
	
	public String id;
	public String name;
	public ChatColor color;
	public ChatColor chatColor;
	public ChatColor prefixColor;
	public long ordinal;
	
	public String prefix;
	public String prefixLegacy;
	public String prefixMini;
	public boolean isStaffRank;
	public long chatDelaySeconds;
	
	String[] extraPermissions;
	String[] extraAuras;
	int adRateLimit;
	int adMonthlyLimit;
	boolean subscription;
	
	@SuppressWarnings({ "unchecked" })
	public MinehutRank(JSONObject object) {
		this.id = (String) object.getOrDefault("id", null);
		this.name = (String) object.getOrDefault("name", null);
		this.color = ChatColor.of((String) object.getOrDefault("color", "GRAY"));
		this.chatColor = ChatColor.of((String) object.getOrDefault("chatColor", "GRAY"));
		this.prefixColor = ChatColor.of((String) object.getOrDefault("prefixColor", "GRAY"));
		this.ordinal = (long) object.getOrDefault("ordinal", -1);
		
		this.prefix = (String) object.getOrDefault("prefix", null);
		this.prefixLegacy = (String) object.getOrDefault("prefixLegacy", null);
		this.prefixMini = (String) object.getOrDefault("prefixMini", null);
		this.isStaffRank = (boolean) object.getOrDefault("staff", false);
		this.chatDelaySeconds = (long) object.getOrDefault("chatDelaySeconds", 0);
	}
	
	public String formatPlayer(String playerName) {
		return prefixColor + FormatUtil.color(prefixLegacy) + playerName;
	}
	
	public String toString() {
		return "MinehutRank{id=" + this.id + ",name=" + this.name + ",prefix=" + this.prefix + "}";
	}
}
