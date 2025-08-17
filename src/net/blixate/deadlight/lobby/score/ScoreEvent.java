package net.blixate.deadlight.lobby.score;

import org.bukkit.configuration.ConfigurationSection;

import net.blixate.deadlight.Deadlight;

public class ScoreEvent {
	
	private ScoreType type;
	private long amount;
	private String name;
	private boolean silent;
	private float multiplier = 1f;
	
	public ScoreEvent(String id, ScoreType type) {
		this.type = type;
		ConfigurationSection section = Deadlight.scoreEvents.getConfigurationSection(type.name().toLowerCase() + "." + id);
		this.name = section.getString("name", "Unknown Score Event");
		this.amount = section.getLong("amount", 0);
		this.silent = section.getBoolean("silent", false);
	}
	
	public void addMultiplier(float factor) {
		multiplier += factor;
	}
	
	public long getAmount() {
		return (long) (this.amount * multiplier);
	}
	
	public String getName() {
		return this.name;
	}
	
	public ScoreType getType() {
		return this.type;
	}
	
	public boolean isSilent() {
		return this.silent;
	}
}
