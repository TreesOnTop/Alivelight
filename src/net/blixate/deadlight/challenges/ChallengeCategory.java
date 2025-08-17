package net.blixate.deadlight.challenges;

public enum ChallengeCategory {
	KILLER, SURVIVOR, NEUTRAL;
	
	public String toString() {
		switch(this) {
		case KILLER: return "Killer";
		case SURVIVOR: return "Survivor";
		case NEUTRAL: return "Neutral";
		}
		return "???";
	}
}
