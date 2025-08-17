package net.blixate.deadlight.kit.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.maps.environment.MapObject;
import net.blixate.deadlight.maps.environment.PortalFragment;
import net.blixate.deadlight.maps.environment.states.BlockedState;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.time.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class CloakerType extends KillerType {
	
	public boolean invisible;
	
	Cooldown appearCooldown;
	Cooldown hitScoreEventCooldown;
	Cooldown silentTerror;
	
	Cooldown retaliateCooldown;
	
	public CloakerType() {
		tickInterval = 5;
	}
	
	@Override
	public void equipKiller(PlayerInventory inv) {
		quickEquip(user, inv);
		appearCooldown = new Cooldown();
		hitScoreEventCooldown = new Cooldown();
		silentTerror = new Cooldown();
		retaliateCooldown = new Cooldown();
		updateItem();
	}
	
	public void perkEvent(PerkEvent event) {
		if(event.getType().equals(PerkEventType.SURVIVOR_HIT)) {
			if(!hitScoreEventCooldown.isDone()) {
				user.addScoreEvent(new ScoreEvent("cloaker_cloak_hit", ScoreType.BLOOD));
				hitScoreEventCooldown.stop();
			}
		}
		if(event.getType().equals(PerkEventType.OBJECTIVE_BREAK)) {
			if(hasAddon("silent_sabotage") && invisible) {
				for(DLUser survivor : user.getLobby().getPlayers()) {
					for(MapObject fragment : user.getLobby().getGenerators()) {
						Location survLoc = survivor.getLocation();
						Location genLoc = fragment.getPosition().getLocation(user.getLobby().getMapLocation());
						if(survLoc.distance(genLoc) < 10) {
							survivor.glowToKiller(5000);
						}
					}
				}
			}
			if(hasAddon("in_the_dark")) {
				if(invisible) {
					PortalFragment fragment = (PortalFragment)event.getParams()[0];
					if(!fragment.hasState(BlockedState.class)) {
						fragment.addState(new BlockedState(5000));
					}
				}
			}
		}
		if(event.getType().equals(PerkEventType.ATTEMPT_PLAYER_HIT)) {
			if(hasAddon("ash_mix")) {
				if(invisible) {
					DLUser victim = event.getUserParam();
					victim.push(user.getDirection().normalize(), 2);
				}
			}
			if(hasAddon("plague_mix")) {
				if(invisible) {
					DLUser victim = event.getUserParam();
					victim.applyEffect(new InfectedEffect(8000));
					setInvisible(false);
				}
			}
		}
		if(event.getType().equals(PerkEventType.KILLER_PUNCH)) {
			if(!retaliateCooldown.isDone()) {
				DLUser hitter = event.getUserParam();
				user.push(hitter.getDirection(), 2);
				user.hitCooldown.add(2);
				retaliateCooldown.stop();
			}
		}
	}
	
	@Override
	public void rightClickItem(ItemStack item, Block clickedBlock, BlockFace face) {
		if(item.getType().equals(Material.GLOWSTONE_DUST) || item.getType().equals(Material.GUNPOWDER)) {
			if(!appearCooldown.isDone()) {
				if(!invisible) {
					user.sendActionbar("ability_cooldown", ""+appearCooldown.secondsLeft());
				}
				return;
			}
			if(item.getType().equals(Material.GLOWSTONE_DUST)) {
				setInvisible(false);
			}else if(item.getType().equals(Material.GUNPOWDER)) {
				setInvisible(true);
			}
		}
	}
	
	public void updateItem() {
		PlayerInventory inv = user.getInventory();
		ItemStack item;
		if(invisible) {
			item = new ItemStack(Material.GLOWSTONE_DUST);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Reappear Dust" + INPUT_TYPE_RIGHT_CLICK);
			item.setItemMeta(meta);
		}else {
			item = new ItemStack(Material.GUNPOWDER);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Disappear Dust" + INPUT_TYPE_RIGHT_CLICK);
			item.setItemMeta(meta);
		}
		inv.setItem(1, item);
	}
	
	public void setInvisible(boolean invis) {
		invisible = invis;
		updateItem();
		if(invis) {
			user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
			user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, hasAddon("extra_speed") ? 2 : 1, false, false, false));
			user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1, false, false, false));
			user.sendActionbar("cloaker_cloak");
			ParticlesUtil.spawnParticle(Particle.FLAME, user.getLocation(), 10, 0, 1, 1, 1);
			World world = user.getLocation().getWorld();
			world.playSound(user.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 4, 1);
			for(DLUser player : user.getLobby().getPlayers()) {
				if(!player.isKiller()) {
					player.getPlayer().hidePlayer(Deadlight.inst, user.getPlayer());
				}
			}
			
			appearCooldown.start(0.75f);
		}else {
			if(hasAddon("silencer")) {
				silentTerror.start(10);
			}else {
				silentTerror.start(5);
			}
			user.getPlayer().setSprinting(false);
			user.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
			user.getPlayer().removePotionEffect(PotionEffectType.SPEED);
			user.getPlayer().removePotionEffect(PotionEffectType.JUMP);
			if(!hasAddon("speed_dust")) {
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1, false, false, false));
			}
			
			user.sendActionbar("cloaker_uncloak");
			ParticlesUtil.spawnParticle(Particle.FLAME, user.getLocation(), 20, 0, 1, 1, 1);
			World world = user.getLocation().getWorld();
			world.playSound(user.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 4, 2);
			for(DLUser player : user.getLobby().getPlayers()) {
				if(!player.isKiller()) {
					player.getPlayer().showPlayer(Deadlight.inst, user.getPlayer());
				}
				if(hasAddon("silent_shock")) {
					if(player.getLocation().distance(user.getLocation()) < 10) {
						player.glowToKiller(2000);
					}
				}
			}
			
			hitScoreEventCooldown.start(4f);
			appearCooldown.start(3f);
			if(hasAddon("in_the_dark")) {
				user.hitCooldown.start(3f);
			}else {
				user.hitCooldown.start(2.5f);
			}
			retaliateCooldown.start(2f);
			user.glowToSurvivors(1000);
		}
	}
	
	public void tick() {
		if(invisible && !hasAddon("in_the_dark")) {
			for(DLUser survivor : user.getLobby().getSurvivors()) {
				survivor.getPlayer().spawnParticle(Particle.WHITE_ASH, user.getLocation().add(0, 1, 0), 2, 0, .2, .5, .2);
			}
		}
	}
	
	// Hide terror radius if invisible
	@Override
	public int getTerrorRadius() {
		if(invisible || !silentTerror.isDone()) {
			return 0;
		}
		return 16;
	}
	
	public boolean canAttack() {
		return !invisible;
	}
}
