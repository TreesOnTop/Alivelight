package net.blixate.deadlight.maps.environment.states;

public class RegressingState extends ObjectiveState {
	
	public RegressingState() {
		super();
	}
	
	public RegressingState(long duration) {
		super(duration);
	}
	
	public boolean hasExpired() {
		return false;
	}
	
}
