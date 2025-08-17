package net.blixate.deadlight.maps.environment.states;

public class ObjectiveState {
	
	long duration;
	
	public ObjectiveState() {
		duration = -1;
	}
	
	public ObjectiveState(long time) {
		duration = System.currentTimeMillis() + time;
	}
	
	public boolean hasExpired() {
		return System.currentTimeMillis() > duration;
	}
	
}
