package net.blixate.deadlight.player.effects;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLUser;

public class SpeedEffect extends Effect {

	private float percent;
	
	public SpeedEffect(float percent, long duration) {
		this.percent = percent;
		this.duration = duration;
	}
	
	@Override
	public void apply(DLUser user) {
		float walkSpeed = user.getSpeed();
		walkSpeed += 0.2f * percent;
		user.setSpeed(walkSpeed);
		Deadlight.debug("Applied Speed effect to " + user.getName());
	}

	@Override
	public void remove(DLUser user) {
		float walkSpeed = user.getSpeed();
		walkSpeed -= 0.2f * percent;
		user.setSpeed(walkSpeed);
		Deadlight.debug("Removed Speed effect from " + user.getName());
	}

}
