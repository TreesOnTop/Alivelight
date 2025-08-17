package net.blixate.deadlight.player.effects;

import java.util.Random;

import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.time.TimeParser;
import net.blixate.deadlight.util.time.Timer;
import net.blixate.deadlight.util.time.TimerListener;

public abstract class Effect implements TimerListener {
	
	protected long duration;
	protected DLUser user;
	private Timer timer;
	private long id;
	
	public abstract void apply(DLUser user);
	
	public abstract void remove(DLUser user);
	
	// Returns duration in seconds
	public double getDuration() {
		return (double)duration / 1000d;
	}
	
	public boolean isInfiniteDuration() {
		return duration == -1;
	}
	
	public String getName() {
		String name = getClass().getSimpleName();
		return name.substring(0, name.length()-"Effect".length());
	}
	
	public long getId() {
		return id;
	}
	
	public String getTimeLeft() {
		if(isInfiniteDuration()) {
			return "∞";
		}
		return TimeParser.toFancyTime(timer.timeLeft());
	}
	
	public void startTimer(DLUser user) {
		if(!isInfiniteDuration()) {
			this.timer = new Timer(this);
			this.timer.start(getDuration());
			this.user = user;
			this.id = Math.abs(new Random().nextLong());
		}
	}

	@Override
	public void onForceStop() {
		removeEffect();
	}

	@Override
	public void onFinish(Timer timer) {
		removeEffect();
		user.activeEffects.remove(this);
	}
	
	protected void removeEffect() {
		remove(user);
	}
	
	public Timer getTimer() {
		return timer;
	}
}
