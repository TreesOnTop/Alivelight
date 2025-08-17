package net.blixate.deadlight.player;

import java.math.BigInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.TimeKeeper;
import net.blixate.deadlight.TimeKeeper.MonthlyEvents;
import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.lobby.GameState;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.effects.Effect;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.player.effects.KnockedEffect;
import net.blixate.deadlight.player.effects.ParanoidEffect;
import net.blixate.deadlight.player.effects.UndetectableEffect;
import net.blixate.deadlight.player.stats.DeadlightStat;
import net.blixate.deadlight.scoreboard.SidebarBoard;
import net.blixate.deadlight.scoreboard.SidebarBuilder;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.nms.NMSHandler;
import net.blixate.deadlight.util.time.TimeParser;
import net.md_5.bungee.api.ChatColor;

/**
 * For every player online, this defines what gets run every tick.
 * Handle scoreboards, timers, etc.
 */
public class PlayerLoop implements Runnable {
	// scoreboard
	SidebarBuilder sb;
	SidebarBoard board;
	// user
	DLUser user;
	// internal
	BukkitTask task;
	BukkitTask particleTask;
	// settings & properties
	public int bloodSize = 2;
	public boolean[] bloodFlags = {
		true, // Only while injured
		true // Only while sprinting
	};
	public boolean doBlood = true;
	private int bleedingChance;
	
	String lastHeader;
	String lastFooter;
	
	boolean wasInTerrorRadius;
	boolean inTerrorRadius;
	
	public int boldnessTicks = 0;
	public boolean boldnessEventTriggered = false;
	
	public int afkTicks = 0;
	public Location lastLocation = null;

	public PlayerLoop(DLUser p) {
		this.user = p;
		board = new SidebarBoard(FormatUtil.color("&9Deadlight &av" + Deadlight.getVersion()));
		// ensure this board is displaying information to our player.
		board.addPlayer(p.getPlayer());
		sb = new SidebarBuilder();
		task = Bukkit.getScheduler().runTaskTimer(Deadlight.inst, this, 0, 20L);
		// Task runs more often
		particleTask = Bukkit.getScheduler().runTaskTimer(Deadlight.inst, () -> particleTick(), 0, 5);
	}
	
	public void unregister() {
		task.cancel();
		Bukkit.getScheduler().cancelTask(task.getTaskId());
		particleTask.cancel();
		Bukkit.getScheduler().cancelTask(particleTask.getTaskId());
	}
	
	/** Run every second this player is online. */
	public void run() {
		try {
			Player player = user.getPlayer();
			
			/** Music stuff */
			if(user.trackActive != null) {
				if(user.trackLoop) {
					long end = user.trackStarted + user.trackActive.getLength();
					long now = System.currentTimeMillis();
					if(now > end) {
						user.playTrack();
					}
				}
			}
			
			if(!player.getPlayerListName().equals(getTablistName())) {
				player.setPlayerListName(getTablistName());
			}
			if(user.getLobby() != null) {
				if(user.isSpectating()) {
					user.spectatingUpdate();
				}else {
					for(Perk p : user.perks)
						p.tick();
					DLUser killer = user.getLobby().getKiller();
					if(!user.isKiller()) {
						if(!killer.hasEffect(UndetectableEffect.class)) {
							Location loc = killer.getLocation();
							float terrorRadius = (float)killer.getKillerType().getTerrorRadius();
							inTerrorRadius = (!killer.equals(user) && loc.distance(player.getLocation()) < terrorRadius);
							// Play terror radius effect
							if (inTerrorRadius) {
								if(!wasInTerrorRadius) {
									onTerrorRadiusEnter();
									wasInTerrorRadius = true;
								}
								terror(loc, killer.getKillerType().getTerrorSound(),killer.getKillerType().getTerrorPitch(), terrorRadius);
							}
							// Were we previously in the terror radius, but we are no longer in the terror radius?
							if(wasInTerrorRadius && !inTerrorRadius) {
								onTerrorRadiusExit();
								wasInTerrorRadius = false;
							}
						}
						if(user.hasEffect(ParanoidEffect.class)) {
							killer.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 4, 2, user.getLocation());
						}
						
						if(lastLocation == null) {
							lastLocation = user.getLocation();
						}else {
							if(lastLocation.distance(user.getLocation()) < 0.5) {
								afkTicks ++;
							}else {
								afkTicks = 0;
								lastLocation = user.getLocation();
							}
						}
						if(user.hasEffect(KnockedEffect.class)) {
							afkTicks = 0;
							user.sendActionbar("knocked_actionbar");
						}
						else if(afkTicks > 30) {
							if(user.getLobby().getState() == GameState.COMPLETE_FRAGMENTS) {
								user.sendActionbar("actionbar_afk");
							}else if(user.getLobby().getState() == GameState.END_GAME_COLLAPSE) {
								user.sendActionbar("actionbar_afk_endgame");
							}
						}
					}
				}
			} else {
				if(user.isQueued()) {
					user.sendActionbar("actionbar_queued");
				}
			}
			
			updateScoreboard();
			sendTablistHeaderFooter();
			updateLevel();
			
			user.getPlayer().setGlowing(user.canGlow());
			
			if(user.getTimeUntilDailyReset() <= 0) {
				user.souls = user.souls.add(BigInteger.valueOf(user.loginReward));
				user.send("login_reward", ""+user.loginReward);
				user.lastLoginReward = System.currentTimeMillis();
				user.loginReward++;
				user.rollNewChallenges();
				user.rerolls = 0;
				user.send("challenges_new_day");
			}
		}
		catch(Exception t) {
			t.printStackTrace();
		}
	}
	
	public void sendTablistHeaderFooter() {
		// set players tablist
		String header = formatPlayerData("header_text");
		String footer = formatPlayerData("footer_text");
		if(lastHeader == null) { lastHeader = ""; }
		if(lastFooter == null) { lastFooter = ""; }
		if(!header.equals(lastHeader)) {
			user.getPlayer().setPlayerListHeader(header);
			lastHeader = header;
			
		}
		if(!footer.equals(lastFooter)) {
			user.getPlayer().setPlayerListFooter(footer);
			lastFooter = footer;
		}
	}
	
	public void particleTick() {
		try {
			Player player = user.getPlayer();
			if(user.getLobby() != null) {
				if(!user.isSpectating()) {
					DLUser killer = user.getLobby().getKiller();
					if(killer.hasEffect(UndetectableEffect.class)) {
						if(user.isKiller()) {
							Location loc = player.getLocation();
							ParticlesUtil.spawnParticle(Particle.SMOKE_NORMAL, loc.add(0, 1, 0), 5, 0, 0.5, 0.5, 0.5);
						}
					}
					if(user.isHoldingSoul()) {
						spawnSoulParticles(user.getLobby(), player);
					}
					if(user.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
						return;
					}
					if(user.hasEffect(InfectedEffect.class)) {
						Location loc = player.getLocation();
						Particle.DustOptions options = new Particle.DustOptions(Color.GREEN, 2);
						loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY()+1, loc.getZ(), 2, 0, .1, .4, .1, options);
					}
					
					if(bloodCheck()) {
						spawnBlood(user.getLobby(), player);
					}
				}
			}
		}catch(Exception t) {
		}
	}
	
	public void terror(Location killerLocation, Sound heartbeatSound, float pitch, float terrorRadius) {
		if(boldnessTicks < 100) {
			if(user.getLocation().distance(killerLocation) < terrorRadius/2) {
				boldnessTicks += 2;
			}else {
				boldnessTicks += 1;
			}
		}
		user.getStatTracker().incrementStat(DeadlightStat.KILLER_RADIUS_TIME);
		if(user.getLobby().getKiller().hasKillerPerk("Omen")) {
			user.getPlayer().playSound(user.getLocation(), heartbeatSound, SoundCategory.MASTER, terrorRadius / 16, pitch);
			Bukkit.getScheduler().runTaskLater(Deadlight.inst, () -> user.getPlayer().playSound(user.getLocation(), heartbeatSound, SoundCategory.MASTER, terrorRadius / 16, pitch), 5);
		}else {
			user.getPlayer().playSound(killerLocation, heartbeatSound, SoundCategory.MASTER, terrorRadius / 16, pitch);
			Bukkit.getScheduler().runTaskLater(Deadlight.inst, () -> user.getPlayer().playSound(killerLocation, heartbeatSound, SoundCategory.MASTER, terrorRadius / 16, pitch), 5);
		}
	}
	
	public void onTerrorRadiusEnter() {
		Deadlight.debug(user.getName() + " entered terror radius");
		boldnessEventTriggered = false;
		boldnessTicks = 0;
	}
	
	public void onTerrorRadiusExit() {
		Deadlight.debug(user.getName() + " exited terror radius");
		if(boldnessTicks > 20 && !boldnessEventTriggered) {
			ScoreEvent score = new ScoreEvent("boldness", ScoreType.BLOOD);
			score.addMultiplier(boldnessTicks);
			boldnessEventTriggered = true;
			boldnessTicks = 0;
			user.addScoreEvent(score);
		}
	}
	
	/** Checks if the player should bleed */
	private boolean bloodCheck() {
		double health = user.getPlayer().getHealth();
		if(doBlood) {
			if(Deadlight.RNG.nextInt(100) < bleedingChance) {
				final boolean bleedWhileInjured = bloodFlags[0];
				final boolean bleedWhileSprinting = bloodFlags[1];
				
				if(bleedWhileInjured && !bleedWhileSprinting) {
					return health < user.getMaxHealth();
				}
				else if(bleedWhileInjured && bleedWhileSprinting) {
					return health < user.getMaxHealth() && user.getPlayer().isSprinting();
				}
				else if(!bleedWhileInjured && !bleedWhileSprinting) {
					return true;
				}else if (!bleedWhileInjured && bleedWhileSprinting) {
					return user.getPlayer().isSprinting();
				}
			}
		}
		return false;
	}
	
	private void spawnSoulParticles(Lobby lobby, Player player) {
		Location loc = player.getLocation().add(0,1,0);
		for(DLUser p : lobby.getPlayers()) {
			p.getPlayer().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0.2, 0.2, 0.2, 0);
		}
	}
	
	public void spawnBlood(Lobby lobby, Player player) {
		Location loc = player.getLocation();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		Particle.DustOptions options = new Particle.DustOptions(user.bloodColor.getColor(), bloodSize);
		for(DLUser p : lobby.getPlayers()) {
			p.getPlayer().spawnParticle(Particle.REDSTONE, x, y+0.1, z, 1, options);
		}
	}
	
	public void updateLevel() {
		if(user.getPlayer().getLevel() != user.getLevel())
			user.getPlayer().setLevel(user.getLevel());
		if(user.getPlayer().getExp() != user.getLevelProgress())
			user.getPlayer().setExp(user.getLevelProgress());
	}
	
	private void updateScoreboard() {
		if(!board.isEnabled()) return;
		
		sb = new SidebarBuilder();
		if(!user.hasReadTutorial) {
			sb.writeBlank();
			sb.write("&7Welcome to Deadlight!");
			sb.writeBlank();
			sb.write("&7Read &e/tutorial&7 to gain access");
			sb.write("&7to the rest of the server.");
			sb.writeBlank();
			sb.write("&7View our rules");
			sb.write("&7with &e/rules");
		}else {
			if(user.prestige < 10) {
				sb.writeIf(user.prestige > 0, "&dPrestige " + user.prestige);
			}else {
				sb.write("&dMaster");
			}
			int percent = Math.round(user.getLevelProgress()*100);
			sb.write("&dLevel "+user.level + " &e" + percent + "%");
			sb.writeBlank();
			
			sb.write(user.getQueueString());
			sb.writeBlank();
			if(user.getBlood() >= user.getBloodCap()) {
				sb.write("&7Blood &8» &c" + FormatUtil.formatCompact(user.blood));
			} else {
				sb.write("&7Blood &8» &e" + FormatUtil.formatCompact(user.blood));
			}
			sb.write("&7Souls &8» &e" + FormatUtil.formatCurrency(user.souls));
			sb.writeBlank();
			sb.write("&7Daily Reset &8» &e" + TimeParser.toClockTime(user.getTimeUntilDailyReset()));
		}
		sb.writeBlank();
		sb.write("&7Deadlight.minehut.gg");
		sb.build(board);
	}
	
	private String getTablistName() {
		return ChatColor.GRAY + user.getFormattedName();
	}
	
	private String formatPlayerData(String text) {
		String msg = Deadlight.msg(user, text);
		msg = msg
			.replace("$blood", ""+user.blood).replace("$souls", ""+user.souls)
			.replace("$ping_str", user.getPingString()).replace("$ping", ""+user.getPing())
			.replace("$queue_state", user.getQueueString())
			.replace("$tps_colored", getColoredTps()).replace("$tps", NMSHandler.getRoundedTPS())
			.replace("$dl_version", Deadlight.getVersion())
			.replace("$online_player_count", Bukkit.getOnlinePlayers().size() + "")
			.replace("$max_player_count", Bukkit.getMaxPlayers() + "")
			.replace("$effect_list", getEffectList())
			.replace("$event", getEvent());
		return msg;
	}
	
	public String getEvent() {
		String message = "event_none";
		
		// first we check monthly events
		if(TimeKeeper.isMonthlyEventActive(MonthlyEvents.HALLOWEEN)) {
			message = "event_halloween";
		}else if(TimeKeeper.isMonthlyEventActive(MonthlyEvents.CHRISTMAS)) {
			message = "event_christmas";
		}else if(TimeKeeper.isMonthlyEventActive(MonthlyEvents.VALENTINE)) {
			message = "event_valentine";
		}else {
			// check smaller events
			if(TimeKeeper.isBloodBathEvent()) {
				message = "event_bloodbath";
			} else if(TimeKeeper.isFishingFrenzyEvent()) {
				message = "event_fishing";
			}
		}
		return Deadlight.msg(message);
	}
	
	public String getEffectList() {
		String[] effects = new String[user.activeEffects.size()];
		int i = 0;
		for(Effect effect : user.activeEffects) {
			effects[i++] = ChatColor.GOLD + effect.getName() + ChatColor.GRAY + " (" + effect.getTimeLeft() + ")";
		}
		return String.join("\n", effects);
	}
	
	public String getColoredTps() {
		double tps = NMSHandler.getTPS()[0];
		ChatColor color = ChatColor.GREEN;
		if(tps < 18 && tps > 15) { // Oh no...
			color = ChatColor.YELLOW;
		}else if(tps < 15 && tps > 10) { // BAD!
			color = ChatColor.RED;
		}else if(tps < 10) { // HORRIBLE!
			color = ChatColor.DARK_RED;
		}
		return color + NMSHandler.roundTps(tps);
	}
	
	public void setDoBleed(boolean b) {
		doBlood = b;
		if(b) {
			user.playSound(Sound.BLOCK_NOTE_BLOCK_CHIME, 1.5);
			user.send("blood_unhidden");
		}else{
			user.playSound(Sound.BLOCK_NOTE_BLOCK_CHIME, 2);
			user.send("blood_hidden");
		}
	}
	
	public void setBloodMode(boolean whileInjured, boolean whileSprinting) {
		bloodFlags = new boolean[] { whileInjured, whileSprinting };
	}
	
	/** Set the chance of this player spawning a blood particle */
	public void setBleedingChance(int percent) {
		bleedingChance = percent;
	}

	public void setScoreboardEnabled(boolean b) {
		if(b) {
			board.addPlayer(user.getPlayer());
		}
	}
}
