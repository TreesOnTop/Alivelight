package net.blixate.deadlight.player;

import java.util.HashMap;
import java.util.UUID;

import net.blixate.deadlight.util.time.Cooldown;

/** 
 * The player's data for this lobby
 * 
 * Contains action speed, multipliers, and anything else a perk
 * may want to change.
 */
public class MatchPlayerData {
	
	public double uncursingSpeed = 1;
	public double poweringSpeed = 1;
	public double healingSpeed = 1;
	public double bloodMultiplier = 1;
	public double survivorHealingProgress = 0;
	
	public double soulExpireTime = 60;
	
	public double itemDropChance = 33;
	
	// glowing
	long globalGlow = -1; // For when all players should be glowing
	long killerGlow = -1; // For when the player should glow for the killer
	long survivorGlow = -1; // For when the player should glow for all survivors
	HashMap<UUID, Long> glowingToEntity = new HashMap<>();
	
	public boolean hasTakenDamage = false;
	public boolean allowRevive = true;
	public boolean revived = false;
	
	public boolean knocked = false;
	public Cooldown knockedTimer = new Cooldown();
	
	public MatchPlayerData() {
		
	}
	
	public void reset() {
		uncursingSpeed = 1f;
		poweringSpeed = 1f;
		healingSpeed = 1f;
		bloodMultiplier = 1f;
		survivorHealingProgress = 0;
		killerGlow = 0;
		survivorGlow = 0;
		globalGlow = 0;
		soulExpireTime = 60;
		knockedTimer = new Cooldown();
		glowingToEntity.clear();
	}
	
}
