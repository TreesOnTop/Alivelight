package net.blixate.deadlight.util.time;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import net.blixate.deadlight.Deadlight;

public class Timer implements Runnable {
	double duration;
	TimerListener listener;
	BukkitTask task;
	
	long timeStamp = -1;
	
	public Timer(TimerListener l, double duration) {
		this.duration = duration;
		this.listener = l;
	}
	
	public Timer(TimerListener l) {
		this.listener = l;
	}
	
	public void start(double duration) {
		this.duration = duration;
		start();
	}
	
	public void start() {
		timeStamp = System.currentTimeMillis() + (long)(1000 * duration);
		task = Bukkit.getScheduler().runTaskTimer(Deadlight.inst, this, 0, 20);
	}
	
	public void stop() {
		if(task != null) {
			task.cancel();
		}
		listener.onForceStop();
		task = null;
	}
	
	public long timeLeft() {
		return timeStamp - System.currentTimeMillis();
	}

	@Override
	public void run() {
		if(System.currentTimeMillis() > timeStamp) {
			if(listener != null) {
				listener.onFinish(this);
			}
			if(task != null)
				task.cancel();
		}
	}
}
