package net.blixate.deadlight.kit.offerings;

import net.blixate.deadlight.player.DLUser;

public enum Offering {
	XP_BOOSTER_x2("XP Booster (25%)", "Boosts all XP gained by 25% for 2 hours.", 7_200_000, 8),
	XP_BOOSTER_x4("XP Booster (50%)", "Boosts all XP gained by 50% for 1 hour.", 3_600_000, 4),
	BLOOD_BOOSTER_x2("Blood Booster (25%)", "Boosts all Blood gained by 25% for 2 hours.", 7_200_000, 10),
	BLOOD_BOOSTER_x4("Blood Booster (50%)", "Boosts all Blood gained by 50% for 1 hour.", 3_600_000, 5),
	FIRST_BORN_CHILD("First Born Child", "Boosts all Blood and XP gained by 25% for 1 hour.", 3_600_000, 5),
	DEAL_WITH_THE_DEVIL("Deal with the Devil", "Grants a higher chance of becoming the Killer."),
	LULLABY("Mother's Lullaby","Grants a higher chance of becoming the Obsession."),
	BIG_BASS("The Big Bass", "Lowers Fishing prices by 15% for 1 hour.", 3_600_000, 5),
	EXTERMINATOR_CREST("Exterminator Crest", "Boosts all Killer XP gained by 100% for 1 hour.", 3_600_000, 5),
	EXTERMINATOR_MOD_KIT("Exterminator Mod Kit", "Reduces Augment prices by 10% for 2 hours.", 7_200_000, 10);
	
	String name;
	String desc;
	long duration;
	int limit;
	
	Offering() {}
	
	Offering(String name, String desc) {
		this(name, desc, -1, -1);
	}
	
	Offering(String name, String desc, long duration, int limit) {
		this.name = name;
		this.desc = desc;
		this.duration = duration;
		this.limit = limit;
	}
	
	public String getName() {
		return name;
	}

	public String getDescription() {
		return desc;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public void use(DLUser burner) {
		switch(this) {
		case DEAL_WITH_THE_DEVIL:
			burner.killerWeight += 10;
			break;
		case LULLABY:
			burner.obsessionWeight += 10;
			break;
		default:
			break;
		}
	}
}
