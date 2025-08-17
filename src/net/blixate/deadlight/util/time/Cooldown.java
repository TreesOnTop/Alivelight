package net.blixate.deadlight.util.time;

public class Cooldown {
	private long cooldown;
	
	public boolean isDone() {
		return System.currentTimeMillis() > cooldown;
	}
	
	public void start(float seconds) {
		cooldown = System.currentTimeMillis() + (int)(seconds * 1000);
	}
	
	public void start(double seconds) {
		cooldown = System.currentTimeMillis() + (int)(seconds * 1000);
	}
	
	public void add(double time) {
		cooldown += (int)(time * 1000);
	}
	
	public void stop() {
		cooldown = -1;
	}
	
	public long timeLeft() {
		return cooldown - System.currentTimeMillis();
	}
	
	public String secondsLeft() {
		double left = ((double)timeLeft()) / 1000d;
		left = Math.round(left * 100d) / 100d;
		return "" + left;
	}
}
