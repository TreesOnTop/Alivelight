package net.blixate.deadlight.lobby;

// Only use native values so it can be saved/loaded as a JSON file
public class MatchSettings {
	
	public boolean killerAlwaysGlow = false;
	public boolean survivorsAlwaysGlow = false;
	
	public boolean survivorsShowNametags = false;
	public boolean spectateAfterDeath = true;
	public boolean spectateAfterEscape = true;
	
	public float survivorHealth = 10;
	
	public int killerBreakAmount = 5;
	public double killerBreakCooldown = 4;
	
	public double killerTerrorRadius = 24;
	
	public double hitCooldown = 3;
	public double hitSlowdown = 3;
	public double hitBlindness = 3;
	
	public float genSpeed = 1;
	public double genCooldown = 0.6;
	
	public float healingSpeed = 1;
	public double healingCooldown = 0.15;
	
	public int portalSpeed = 1;
	public double portalCooldown = 0.25;
	
	public double matchTime = 720;
	public double endGameCollapseTime = 140;

	public double killerSpeed = 0.2;
	public double survivorSpeed = 0.2;
	
	public float reviveTime = 10f;
	public float soulExpireTime = 60;
}
