package net.blixate.deadlight.kit.type;

public enum KillerTypeDifficulty {
	UNKNOWN, EASY, MEDIUM, HARD, VERY_HARD;
	
	public String toString() {
		switch(this) {
		case UNKNOWN: return "&7Unknown";
		case EASY: return "&aEasy";
		case MEDIUM: return "&6Medium";
		case HARD: return "&cHard";
		case VERY_HARD: return "&4Very Hard";
		}
		return "";
	}
}
