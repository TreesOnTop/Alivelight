package net.blixate.deadlight.kit.items.flowers;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.ExposedEffect;

public class UndyingFlower extends FlowerTotem {
	public static final int RADIUS = 15;
	
	HashMap<DLUser, Integer> progress = new HashMap<>();
	
	public void fatalHit(DLUser user, Lobby lobby) {
		if(location.distance(user.getLocation()) < RADIUS) {
			user.addHealth(2);
			user.removeEffect(ExposedEffect.class);
			user.send("flower_undied");
			user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1));
			lobby.getFlowers().remove(this);
			location.getBlock().setType(Material.AIR);
			lobby.getKiller().send("flower_undied_killer");
			if(!user.equals(owner)) {
				owner.send("flower_undied_owner");
				owner.addScoreEvent(new ScoreEvent("flower_save", ScoreType.BLOOD));
			}
		}
	}
}