package net.blixate.deadlight.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;

public class TimeLimit {
	public static class BossbarHandler implements Runnable {
		BossBar bar;
		double duration;
		TimeLimit parent;
		
		public BossbarHandler(BossBar bar, double duration, TimeLimit egc) {
			this.bar = bar;
			this.duration = duration;
			this.parent = egc;
		}
		
		@Override
		public void run() {
			double progress = bar.getProgress();
			progress -= (1 / duration);
			if(getTimeLeft() < 20d) {
				DLUser[] players = parent.lobby.getPlayers();
				for(DLUser player : players) {
					player.playSound(Sound.BLOCK_NOTE_BLOCK_GUITAR, 0.2);
				}
			}
			if(progress > 0) {
				bar.setProgress(progress);
				return;
			}
			parent.end();
		}
		
		public double getTimeLeft() {
			return bar.getProgress() * duration;
		}
	}
	
	String title;
	
	Lobby lobby;
	BossBar bossbar;
	BukkitTask task;
	BossbarHandler handler;
	
	public TimeLimit(Lobby lobby) {
		this(lobby, "&aTime Limit");
	}
	
	public TimeLimit(Lobby lobby, String title) {
		this(lobby, title, BarColor.RED);
	}
	
	public TimeLimit(Lobby lobby, String title, BarColor color) {
		this.lobby = lobby;
		this.title = title;
		bossbar = Bukkit.createBossBar(FormatUtil.color(title), BarColor.RED, BarStyle.SEGMENTED_20);
		bossbar.setVisible(false);
		for(DLUser user : lobby.getPlayers()) {
			bossbar.addPlayer(user.getPlayer());
		}
		
		for(DLUser user : lobby.externalSpectators) {
			bossbar.addPlayer(user.getPlayer());
		}
	}
	
	public double getTimeLeft() {
		if(handler == null) {
			return -1.0;
		}
		return handler.getTimeLeft();
	}
	
	public void start(double duration) {
		Deadlight.debug("Starting time limit " + this.title);
		bossbar.setVisible(true);
		handler = new BossbarHandler(bossbar, duration, this);
		task = Bukkit.getScheduler().runTaskTimer(Deadlight.inst, handler, 1L, 20L);
	}
	
	public void end() {
		Deadlight.debug("Stopping time limit " + this.title);
		if(task != null)
			task.cancel();
		lobby.sendGlobal("match_end_game_collapsed");
		for(int i = 0; i < lobby.getSurvivorCount(); i++) {
			lobby.getKiller().addScoreEvent(new ScoreEvent("kill_collapse", ScoreType.BLOOD));
		}
		DLUser[] users = lobby.getPlayers();
		for(DLUser user : users) {
			user.quitLobby();
		}
		handler = null;
		lobby.end();
		
		for(DLUser user : users) {
			user.playSound(Sound.BLOCK_ENDER_CHEST_OPEN, .5);
		}
	}
}
