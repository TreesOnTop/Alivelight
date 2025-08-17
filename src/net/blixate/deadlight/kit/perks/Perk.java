package net.blixate.deadlight.kit.perks;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import net.blixate.config.ConfigProperty;
import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.MatchPlayerData;
import net.blixate.deadlight.util.time.Cooldown;
import net.blixate.deadlight.util.time.Timer;

public abstract class Perk {
	
	PerkRegistry registry;
	
	protected DLUser user;
	protected ArrayList<BukkitTask> tasks = new ArrayList<>();
	protected Cooldown cooldown = new Cooldown();
	protected ArrayList<Timer> timers = new ArrayList<>();
	
	int tier;
	
	public Perk() {
		registry = (PerkRegistry) Deadlight.getPerkManager().getRegistry(this.getClass().getSimpleName());
	}
	
	public void setup(DLUser user) {
		this.user = user;
		tier = user.getPerkTier(getRegistry().id);
		setup();
	}
	
	public void destroy() {
		for(Timer timer : timers) {
			timer.stop();
		}
		timers.clear();
	}
	
	/** Runs on the registered {@link PerkEventType}
	 */
	public abstract void activate(PerkEvent event);
	
	/* Run every tick to update */
	public void tick() {}
	
	/* Run when this perk shows up on the scoreboard
	 * This is expected to just return the name of the perk, which is provided in {@code line} */
	public String scoreboardTick(String line) {
		return line;
	}
	
	/* Called after setup(DLUser) is completed */
	public void setup() {}
	
	/** Runs on any event
	 * @param e The {@link PerkEventType} that triggered this call. */
	public void event(PerkEvent e) {}
	
	/** <b>Do not override!</b><br>
	 * This calls the registered event for {@link Perk#activate()} and executes before {@link Perk#event(PerkEventType)}*/
	void callEvent(PerkEventType e, Object[] params) {
		if(user.isSpectating()) {
			return;
		}
		PerkEvent event = new PerkEvent(e, params);
		try {
			if(e.equals(getEvent())) {
				activate(event);
			}
			event(event);
		}catch(Throwable t) {
			System.err.println(getRegistry().id + " failed to activate!");
			t.printStackTrace();
			return;
		}
	}
	
	public ConfigProperty getTierProperty() {
		return getRegistry().getTier(tier);
	}
	
	protected DLUser getUser() {
		return this.user;
	}
	
	protected MatchPlayerData getData() {
		return this.user.getMatchData();
	}
	
	protected Player getPlayer() {
		return this.user.getPlayer();
	}
	
	public PerkRegistry getRegistry() {
		return registry;
	}
	
	public PerkEventType getEvent() {
		return getRegistry().event;
	}
	
	public void delay(Runnable r, long delay) {
		Bukkit.getScheduler().runTaskLater(Deadlight.inst, r, delay);
	}
	
	protected void addTimer(Timer timer) {
		timers.add(timer);
	}

	public int getTier() {
		return tier;
	}
}
