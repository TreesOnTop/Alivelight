package net.blixate.deadlight.player.effects;

import net.blixate.deadlight.player.DLUser;

public class HemorrhageEffect extends Effect {
	
	int tier;
	
	public HemorrhageEffect(int tier, long duration) {
		this.duration = duration;
		this.tier = tier;
	}
	
	@Override
	public void apply(DLUser user) {
		user.getPlayerLoop().bloodSize += tier;
	}

	@Override
	public void remove(DLUser user) {
		user.getPlayerLoop().bloodSize -= tier;
	}
	
}
