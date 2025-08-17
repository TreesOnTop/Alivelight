package net.blixate.deadlight.util.time;

public interface TimerListener {
	
	public void onForceStop();
	
	public void onFinish(Timer timer);
	
}
