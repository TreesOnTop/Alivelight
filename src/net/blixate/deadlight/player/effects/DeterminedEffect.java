package net.blixate.deadlight.player.effects;

import net.blixate.deadlight.player.DLUser;

public class DeterminedEffect extends Effect {
	
	public DeterminedEffect(long dur) {
		this.duration = dur;
	}
	
	public DeterminedEffect() {
		this.duration = -1;
	}
	
	@Override
	public void apply(DLUser user) {
		
	}

	@Override
	public void remove(DLUser user) {
		
	}

}
