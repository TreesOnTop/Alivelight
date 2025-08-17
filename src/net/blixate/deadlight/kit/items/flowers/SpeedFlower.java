package net.blixate.deadlight.kit.items.flowers;

import java.util.HashSet;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.DLUser;

public class SpeedFlower extends FlowerTotem {
	public static final int RADIUS = 10;
	
	HashSet<DLUser> affected = new HashSet<>();
	
	public void tick(Lobby lobby) {
		for(DLUser user : lobby.getSurvivors()) {
			if(location.distance(user.getLocation()) < RADIUS) {
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 0));
			}
		}
	}
}
