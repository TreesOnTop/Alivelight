package net.blixate.deadlight.player;

/** Only used for perks */
public enum Alignment {
	KILLER, SURVIVOR;

	public String getPerkClass(String id) {
		return "net.blixate.deadlight.kit.perks." + this.name().toLowerCase() + "." + id;
	}
	
	@Override
	public String toString() {
		if(this == KILLER) {
			return "Killer";
		}
		else if(this == SURVIVOR) {
			return "Survivor";
		}
		return "Unknown Alignment";
	}
}
