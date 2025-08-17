package net.blixate.deadlight.player.effects;

import net.blixate.deadlight.player.DLUser;

public class ExposedEffect extends Effect {
	
	public ExposedEffect(long dur) {
		this.duration = dur;
	}
	
	public ExposedEffect() {
		this.duration = -1;
	}
	
	@Override
	public void apply(DLUser user) {
		
	}

	@Override
	public void remove(DLUser user) {
		
	}

}
