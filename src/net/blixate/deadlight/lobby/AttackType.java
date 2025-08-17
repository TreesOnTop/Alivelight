package net.blixate.deadlight.lobby;

public enum AttackType {
	BASIC, // The killer swung his weapon at this survivor and hit them.
	ABILITY, // The killer injured this survivor using a damaging ability
	TRAP; // The killer wasn't even involved when they took damage.
}
