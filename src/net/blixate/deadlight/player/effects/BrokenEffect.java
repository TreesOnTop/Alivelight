package net.blixate.deadlight.player.effects;

import net.blixate.deadlight.player.DLUser;

public class BrokenEffect extends Effect {
	
	public BrokenEffect(long duration) {
		this.duration = duration;
	}
	
	public BrokenEffect() {
		this.duration = -1;
	}
	
	@Override
	public void apply(DLUser user) {
		
	}

	@Override
	public void remove(DLUser user) {
		
	}

}
