package net.blixate.deadlight.player.effects;

import net.blixate.deadlight.player.DLUser;

public class ImpairedEffect extends Effect {

	private float percent;
	
	public ImpairedEffect(float percent, long duration) {
		this.percent = percent;
		this.duration = duration;
	}
	
	@Override
	public void apply(DLUser user) {
		user.getMatchData().uncursingSpeed -= percent;
		user.getMatchData().healingSpeed -= percent;
	}

	@Override
	public void remove(DLUser user) {
		user.getMatchData().uncursingSpeed += percent;
		user.getMatchData().healingSpeed += percent;
	}
}
