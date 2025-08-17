package net.blixate.deadlight.kit.perks;

import net.blixate.deadlight.player.DLUser;

public class PerkEvent {
	private PerkEventType type;
	private Object[] parameters;
	
	public PerkEvent(PerkEventType type) {
		this.type = type;
		this.parameters = new Object[0];
	}
	
	public PerkEvent(PerkEventType type, Object...params) {
		this.type = type;
		this.parameters = params;
	}
	
	public PerkEventType getType() {
		return type;
	}
	
	public Object[] getParams() {
		return parameters;
	}
	
	public DLUser getUserParam() {
		return (DLUser)parameters[0];
	}
}
