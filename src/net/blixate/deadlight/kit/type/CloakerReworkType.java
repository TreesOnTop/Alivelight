package net.blixate.deadlight.kit.type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.KnockedEffect;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.time.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class CloakerReworkType extends KillerType {
	
	public static final Material CHARGED = Material.GLOWSTONE_DUST;
	public static final Material CHARGE = Material.GUNPOWDER;
	public static final Material AMBUSH = Material.OAK_SAPLING;
	
	public static final float MINIMUM_CHARGE_Y = 0.1f;
	public static final float MAXIMUM_CHARGE_Y = 0.8f;
	
	enum ChargeStatus {
		NOT_CHARGED, CHARGING, CHARGED, PROCESS, KNOCK_DOWN;
	}
	
	int maxAmbushes;
	int ambushesLeft;
	Location[] ambushSpots;
	int ambushTriggered;
	Cooldown ambushTimer;
	Cooldown ambushScoreEvent;
	
	Cooldown chargeCooldown;
	Cooldown chargeTimer;
	Cooldown chargeDuration;
	
	Cooldown inCharge;
	
	Cooldown knockableTimer;
	
	ChargeStatus status;
	
	float assaultActiveDuration = 10; // How long an assault is active before it's automatically uncharged
	float inAssaultDuration = 4; // Time window where you are "in a charge" and can knock down survivors
	float assaultPenaltyDuration = 5; // Duration of penalty if you fail to knock someone
	int knockedDownDuration = 60000; // How long the player is knocked down
	float assaultCooldownDuration = 20; // How long until you can perform a charge again
	float assaultVelocity = 3; // How fast your charge is
	float chargingDuration = 5; // How long it takes to charge up your assault
	
	float ambushTeleportDuration = 2; // Time window where you can teleport to your ambush
	float ambushRange = 5; // AoE range of your ambush activation
	
	public CloakerReworkType() {
		tickInterval = 5;
	}
	
	@Override
	public void equipKiller(PlayerInventory inv) {
		quickEquip(user, inv);
		
		ambushTimer = new Cooldown();
		chargeCooldown = new Cooldown();
		chargeTimer = new Cooldown();
		inCharge = new Cooldown();
		knockableTimer = new Cooldown();
		chargeDuration = new Cooldown();
		
		maxAmbushes = 3;
		ambushesLeft = maxAmbushes;
		ambushSpots = new Location[ambushesLeft];
		ambushScoreEvent = new Cooldown();
		
		updateItems();
	}
	
	public void perkEvent(PerkEvent event) {
		if(event.getType().equals(PerkEventType.PRESS_F)) {
			if(!ambushTimer.isDone()) {
				user.getPlayer().teleport(ambushSpots[ambushTriggered]);
				ambushSpots[ambushTriggered] = null;
				user.playSound(Sound.BLOCK_NOTE_BLOCK_CHIME, 2);
				user.hitCooldown.start(1f);
				ambushScoreEvent.start(6f);
			}
		}
		if(event.getType().equals(PerkEventType.SURVIVOR_HIT)) {
			if(!ambushScoreEvent.isDone()) {
				user.addScoreEvent(new ScoreEvent("cloaker_ambushed", ScoreType.BLOOD));
			}
		}
		if(event.getType().equals(PerkEventType.ATTEMPT_PLAYER_HIT)) {
			if(status == ChargeStatus.PROCESS) {
				DLUser hit = event.getUserParam();
				if(!hit.isKiller()) {
					hit.applyEffect(new KnockedEffect(knockedDownDuration));
					status = ChargeStatus.NOT_CHARGED;
					hit.addHealth(2);
					user.addScoreEvent(new ScoreEvent("cloaker_knocked", ScoreType.BLOOD));
					updateItems();
				}
			}else if(status != ChargeStatus.NOT_CHARGED) {
				status = ChargeStatus.NOT_CHARGED;
				updateItems();
				user.sendActionbar("cloaker_charge_cancelled");
			}
		}
	}
	
	@Override
	public void rightClickItem(ItemStack item, Block clickedBlock, BlockFace face) {
		if(item.getType().equals(CHARGED)) {
			if(status == ChargeStatus.CHARGING) {
				status = ChargeStatus.NOT_CHARGED;
				updateItems();
				user.sendActionbar("cloaker_charge_cancelled");
				return;
			}
			if(status != ChargeStatus.CHARGED) {
				return;
			}
			user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (inAssaultDuration * 20), 200));
			user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (inAssaultDuration * 20), 250));
			Vector direction = user.getDirection();
			if(direction.getY() < MINIMUM_CHARGE_Y) {
				direction.setY(MINIMUM_CHARGE_Y);
			}
			if(direction.getY() > MAXIMUM_CHARGE_Y) {
				direction.setY(MAXIMUM_CHARGE_Y);
			}
			user.push(direction, assaultVelocity);
			chargeCooldown.start(assaultCooldownDuration);
			status = ChargeStatus.PROCESS;
			inCharge.start(inAssaultDuration);
		}
		if(item.getType().equals(CHARGE)) {
			if(status == ChargeStatus.CHARGING) return;
			// start charge
			if(status == ChargeStatus.CHARGED) {
				status = ChargeStatus.NOT_CHARGED;
				user.sendActionbar("cloaker_uncharged");
				return;
			}
			if(!chargeCooldown.isDone()) {
				user.sendActionbar("ability_cooldown", ""+chargeCooldown.secondsLeft());
				return;
			}
			chargeTimer.start(5f);
			status = ChargeStatus.CHARGING;
		}
		if(item.getType().equals(AMBUSH)) {
			if(ambushesLeft > 0) {
				ambushSpots[maxAmbushes-ambushesLeft] = clickedBlock.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
				ambushesLeft--;
				updateItems();
				user.send("cloaker_set_ambush", ""+ambushesLeft);
				user.playSound(Sound.BLOCK_NOTE_BLOCK_CHIME, 0);
			}
		}
	}
	
	public void updateItems() {
		PlayerInventory inv = user.getInventory();
		ItemStack item;
		ItemMeta meta;
		item = new ItemStack(status == ChargeStatus.CHARGED ? CHARGED : CHARGE);
		meta = item.getItemMeta();
		meta.setDisplayName(status == ChargeStatus.CHARGED ? FormatUtil.color("&c&lAssault&8") + INPUT_TYPE_RIGHT_CLICK : FormatUtil.color("&a&lCharge&8") + INPUT_TYPE_RIGHT_CLICK);
		List<String> lore = new ArrayList<>();
		if(status == ChargeStatus.CHARGED) {
			lore.add(FormatUtil.color("&7Left-click to perform assault"));
			lore.add(FormatUtil.color("&7Right-click to remove charge"));
		}else {
			lore.add(FormatUtil.color("&7Right-click to charge"));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(1, item);
		// Ambushes
		if(ambushesLeft > 0) {
			ItemStack ambushItem = new ItemStack(AMBUSH, ambushesLeft);
			meta = ambushItem.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Set Ambush" + INPUT_TYPE_RIGHT_CLICK);
			ambushItem.setItemMeta(meta);
			inv.setItem(2, ambushItem);
		}else {
			if(inv.getItem(2) != null && inv.getItem(2).getType() != Material.AIR)
				inv.setItem(2, new ItemStack(Material.AIR));
		}
	}
	
	public void tick() {
		if(status == ChargeStatus.CHARGING) {
			if(chargeTimer.isDone()) {
				status = ChargeStatus.CHARGED;
				user.sendActionbar("cloaker_charged");
				chargeDuration.start(assaultActiveDuration);
				updateItems();
			}else {
				user.sendActionbar("cloaker_charging", ""+chargeTimer.secondsLeft());
			}
		}
		else if(status == ChargeStatus.CHARGED) {
			if(chargeDuration.isDone()) {
				status = ChargeStatus.NOT_CHARGED;
				user.sendActionbar("cloaker_uncharged_failed");
				updateItems();
			}
		}
		else if(status == ChargeStatus.PROCESS) {
			if(inCharge.isDone()) {
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (assaultPenaltyDuration * 20), 200));
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (assaultPenaltyDuration * 20), 250));
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (assaultPenaltyDuration * 20), 200));
				user.getPlayer().setSprinting(false);
				//user.glowToSurvivors(5000);
				user.sendActionbar("cloaker_failed_knockdown");
				status = ChargeStatus.NOT_CHARGED;
				updateItems();
			}
		}
		int i = -1;
		for(Location ambushSpot : this.ambushSpots) {
			i++;
			if(ambushSpot == null) {
				continue;
			}
			double x = ambushSpot.getX();
			double y = ambushSpot.getY();
			double z = ambushSpot.getZ();
			BlockData data = Material.DIRT.createBlockData();
			user.getPlayer().spawnParticle(Particle.BLOCK_CRACK, x, y, z, 3, 0, 0, 0, data);
			if(ambushTimer.isDone()) {
				for(DLUser surv : user.getLobby().getSurvivors()) {
					if(surv.getLocation().distance(ambushSpot) < ambushRange) {
						ambushTriggered = i;
						ambushTimer.start(ambushTeleportDuration);
						user.sendActionbar("cloaker_ambush_alarm");
						user.playSound(Sound.BLOCK_NOTE_BLOCK_BELL, 0);
					}
				}
			}
		}
	}
	
	// Hide terror radius if invisible
	@Override
	public int getTerrorRadius() {
		return 16;
	}
	
	public boolean canAttack() {
		return status != ChargeStatus.PROCESS;
	}
}
