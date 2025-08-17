package net.blixate.deadlight.kit.perks.survivor;

import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.util.time.Timer;
import net.blixate.deadlight.util.time.TimerListener;

public class DesperateMeasures extends Perk implements TimerListener {
	
	Timer timer;
	
	public DesperateMeasures() {
		timer = new Timer(this, 60);
		addTimer(timer);
	}
	
	@Override
	public void activate(PerkEvent event) {
		if(cooldown.isDone()) {
			getData().uncursingSpeed += getSpeed();
			getData().healingSpeed += getSpeed();
			timer.start();
			cooldown.start(getTierProperty().getAsDouble());
		}
	}

	@Override
	public void onFinish(Timer timer) {
		onForceStop();
	}

	@Override
	public void onForceStop() {
		getData().uncursingSpeed -= getSpeed();
		getData().healingSpeed -= getSpeed();
	}
	
	public double getSpeed() {
		return 0.25;
	}
}
