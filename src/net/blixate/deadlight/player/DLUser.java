package net.blixate.deadlight.player;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

import io.netty.channel.ChannelHandlerContext;
import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.TimeKeeper;
import net.blixate.deadlight.advertising.MinehutRank;
import net.blixate.deadlight.fishing.FishingRods;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.kit.items.Items;
import net.blixate.deadlight.kit.offerings.OfferingManager;
import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.kit.perks.PerkRegistry;
import net.blixate.deadlight.kit.type.KillerType;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.MatchSettings;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.maps.environment.PlayerSoul;
import net.blixate.deadlight.music.DLTrack;
import net.blixate.deadlight.player.effects.BrokenEffect;
import net.blixate.deadlight.player.effects.Effect;
import net.blixate.deadlight.player.effects.ExposedEffect;
import net.blixate.deadlight.player.items.SpawnItems;
import net.blixate.deadlight.player.stats.DeadlightStat;
import net.blixate.deadlight.player.stats.StatTracker;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.file.SongManager;
import net.blixate.deadlight.util.random.DeadlightLootTable.DropResult;
import net.blixate.deadlight.util.random.DeadlightLootTable.FungibleDropResult;
import net.blixate.deadlight.util.random.DeadlightLootTable.ItemDropResult;
import net.blixate.deadlight.util.random.DeadlightLootTable.OfferingDropResult;
import net.blixate.deadlight.util.time.Cooldown;
import net.blixate.deadlight.util.time.TimeParser;
import net.blixate.deadlight.util.time.Timer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * <h1>Wrapper for Bukkit's Player class, used for this plugin</h1>
 * 
 * <p>This handles storage, settings, and cooldowns. Any attributes that must be bound to a player
 * are included as fields.</p>
 * 
 * @see Deadlight
 * @see DLData
 */
public class DLUser extends DLData {
	public static final int MAX_LEVEL = 100;
	public static final int MAX_PRESTIGE = 10;
	public static final int PERK_CAP = 4;
	
	public GuiInventory gui;
	Player bukkitPlayer;
	Lobby lobby;
	
	/* Cooldowns! */
	public Cooldown guiCooldown = new Cooldown();
	public Cooldown launchCooldown = new Cooldown();
	public Cooldown genCooldown = new Cooldown();
	public Cooldown chatCooldown = new Cooldown();
	public Cooldown hitCooldown = new Cooldown();
	public Cooldown npcCooldown = new Cooldown();
	public Cooldown protectionHitCooldown = new Cooldown();
	public Cooldown reportCooldown = new Cooldown();
	
	public MatchPlayerData mpd;
	
	public boolean autoQueue = false;
	/** Handles scoreboards and anything else that must run repeatedly. */
	PlayerLoop loop;
	
	public ArrayList<Effect> activeEffects = new ArrayList<>();
	public List<Perk> perks; // all active perks
	
	public long bloodBuffer;
	public boolean bloodBufferHasCleared;
	
	// Killer Types
	public KillerType killerTypeInstance = null;
	
	public long joinTime;
	public Alignment preferredRole;
	
	public String minehutRankName;
	
	public DLTrack trackActive;
	public long trackStarted;
	public boolean trackLoop;
	
	public DLUser(Player p) {
		super(p); // This loads our data!
		// must be done before anything.
		this.bukkitPlayer = p;
		loop = new PlayerLoop(this);
		lastLogin = System.currentTimeMillis();
		statTracker = new StatTracker(this);
		// Inject a packet listener
		mpd = new MatchPlayerData();
		joinTime = System.currentTimeMillis();
		loop.sendTablistHeaderFooter();
	}
	
	public void delete() {
		if(Deadlight.queue.players.contains(this)) {
			Deadlight.queue.removeDLUser(this);
		}
		playtime += (System.currentTimeMillis() - joinTime);
		
		if(this.getLobby() != null) {
			this.getLobby().removePlayer(this);
		}
		this.savePlayerData();
		this.getPlayerLoop().unregister();
		PlayerManager.removeUser(bukkitPlayer); // GC will clean this up.
	}
	
	public String getFormattedName() {
		return Deadlight.inst.getFormattedPlayerName(this);
	}
	
	public Player getPlayer() {
		return this.bukkitPlayer;
	}
	
	/** Returns null if there is no lobby */
	public Lobby getLobby() {
		return this.lobby;
	}
	
	public MatchPlayerData getMatchData() {
		return mpd;
	}
	
	public void sendTitle(String title, String subtitle) {
		final int duration = 5;
		sendTitle(title, subtitle, duration);
		//this.bukkitPlayer.sendTitle(Deadlight.msg(this, title), Deadlight.msg(this, subtitle), 20, 20 * duration, 20);
	}
	
	public void sendTitle(String title, String subtitle, int duration) {
		this.bukkitPlayer.sendTitle(Deadlight.msg(this, title), Deadlight.msg(this, subtitle), 20, 20 * duration, 20);
	}
	
	public void clearTitle() {
		this.bukkitPlayer.sendTitle("", "", 0, 1, 0);
	}
	
	public void send(String msg, String...strings) {
		this.bukkitPlayer.sendMessage(Deadlight.msg(this, msg, strings));
	}

	public void setGameLobby(Lobby lobby) {
		if(Deadlight.queue.players.contains(this)) {
			Deadlight.queue.removeDLUser(this);
		}
		this.lobby = lobby;
	}
	
	public void openInventory(GuiInventory gui) {
		gui.openInventory(this);
		this.gui = gui;
	}

	public void showQueueInventory() {
		openInventory(Deadlight.queue.inventory);
	}
	
	public PlayerInventory getInventory() {
		return bukkitPlayer.getInventory();
	}

	public ItemStack getHeldItem() {
		int slot = getInventory().getHeldItemSlot();
		return getInventory().getItem(slot);
	}
	
	public boolean isKiller() {
		if(lobby == null) return false;
		return lobby.isKiller(this);
	}
	
	public boolean isObsession() {
		if(lobby == null) return false;
		return lobby.isObsession(this);
	}
	
	public boolean isSpectating() {
		if(lobby == null) return false;
		return lobby.isSpectating(this);
	}
	
	public boolean isHoldingSoul() {
		ItemStack heldItem = getInventory().getItemInMainHand();
		if(heldItem.getType().equals(Material.PLAYER_HEAD)) {
			String uuidString = PlayerSoul.getOwnerUUID(heldItem);
			if(uuidString != null) {
				return true;
			}
		}
		return false;
	}
	
	public void showBook(String...pages) {
		ItemStack book = makeBook();
		BookMeta meta = (BookMeta)book.getItemMeta();
		for(int i = 0; i < pages.length; i++) {
			pages[i] = FormatUtil.color(pages[i]);
		}
		meta.setPages(pages);
		book.setItemMeta(meta);
		bukkitPlayer.openBook(book);
	}
	
	public void showBook(BaseComponent...pages) {
		ItemStack book = makeBook();
		BookMeta meta = (BookMeta)book.getItemMeta();
		meta.spigot().setPages(pages);
		book.setItemMeta(meta);
		bukkitPlayer.openBook(book);
	}
	
	private ItemStack makeBook() {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta = (BookMeta)book.getItemMeta();
		meta.setTitle(ChatColor.BLACK + "");
		meta.setAuthor("Deadlight");
		book.setItemMeta(meta);
		return book;
	}
	
	public void forEachAttribute(Consumer<AttributeInstance> consumer) {
		for(Attribute attr : Attribute.values()) {
			AttributeInstance instance = bukkitPlayer.getAttribute(attr);
			if(instance == null || instance.getModifiers() == null) {
				continue;
			}
			consumer.accept(bukkitPlayer.getAttribute(attr));
		}
	}
	
	public void removeAllAttributes() {
		forEachAttribute((instance) -> {
			for(AttributeModifier modifier : instance.getModifiers()) {
				instance.removeModifier(modifier);
			}
		});
	}
	
	public DLInventory getItemInventory() {
		return this.inventory;
	}
	
	public int getPing() {
		return bukkitPlayer.getPing();
		//return NMSHandler.getPing(bukkitPlayer);
    }
	
	
	public String toString() {
		return this.bukkitPlayer.getName();
	}
	
	/** The GUI this player is currently looking at.
	 * @return {@code null} if there is no gui open or the player's currently viewed inventory isn't a {@link GuiInventory}. */
	public GuiInventory getGui() {
		return this.gui;
	}
	
	public void clearBloodBuffer() {
		bloodBufferHasCleared = true;
		if(bloodBuffer > 0) {
			if(TimeKeeper.isBloodBathEvent() || TimeKeeper.isAnyMonthlyEventActive()) {
				bloodBuffer *= 2;
				send("blood_buffer_cleared_bloodbath", FormatUtil.formatLong(bloodBuffer));
			}else {
				send("blood_buffer_cleared", FormatUtil.formatLong(bloodBuffer));
			}
			BigInteger newBlood = blood.add(BigInteger.valueOf(bloodBuffer));
			setBloodWithCap(newBlood);
			
			bloodBuffer = 0;
			// random reward, only allowed if they have some blood post match.
			if(Math.random() * 100 < mpd.itemDropChance) {
				DropResult result = Deadlight.postMatchDrops.getTable("global").pick();
				this.addDropReward(result);
				send("match_result", result.toString());
			}
		}
		
	}
	
	public void setBloodWithCap(BigInteger newBlood) {
		if(newBlood.compareTo(BigInteger.valueOf(this.getBloodCap())) > 0 && blood.compareTo(BigInteger.valueOf(this.getBloodCap())) < 0) {
			blood = BigInteger.valueOf(this.getBloodCap());
		}else if(newBlood.compareTo(BigInteger.valueOf(this.getBloodCap())) < 0) {
			blood = newBlood;
		}// If neither are met, they are already over cap and won't be awarded blood.
	}
	
	public void addScoreEvent(ScoreEvent event) {
		if(isSpectating() && lobby.isPlayerInIndex(-1, this)) {
			return;
		}
		long amount = event.getAmount();
		if(isInMatch()) {
			Deadlight.getPerkManager().callEvent(this, PerkEventType.SCORE_EVENT, event);
		}
		if(event.getType() == ScoreType.BLOOD) {
			addBloodSilent(amount);
			if(!event.isSilent()) {
				String msg = "&a+&e" + FormatUtil.formatLong(amount) + "&a " + event.getName();
				if(getBlood() > getBloodCap()) {
					msg += " &d(MAX)";
				}
				bukkitPlayer.sendMessage(FormatUtil.color(msg));
			}
		}else if(event.getType() == ScoreType.SOULS) {
			souls = souls.add(BigInteger.valueOf(amount));
			if(!event.isSilent()) {
				bukkitPlayer.sendMessage(FormatUtil.color("&b+&e" + FormatUtil.formatLong(amount) + "&b " + event.getName()));
			}
		}
	}
	
	public void addBloodSilent(long amount) {
		amount *= mpd.bloodMultiplier + (0.1 * prestige) * OfferingManager.getBloodBoost();
		if(lobby != null) {
			if(isSpectating() && lobby.isPlayerInIndex(-1, this)) {
				return;
			}
			bloodBuffer += amount;
			bloodBuffer = Math.min(bloodBuffer, 32000);
			Deadlight.getPerkManager().callEvent(this, PerkEventType.BLOOD_GAIN);
		}
		else {
			blood = blood.add(BigInteger.valueOf(amount));
		}
	}
	
	/* Do not remove from lobby, just set to null. */
	public void quitLobby() {
		this.setGameLobby(null);
	}
	
	public boolean isQueued() {
		return Deadlight.queue.players.contains(this);
	}
	
	public boolean isQueueBanned() {
		return queueBan != -1 && System.currentTimeMillis() < queueBan;
	}
	
	public void queueBan(long time) {
		queueBan = System.currentTimeMillis() + time;
		send("user_now_queue_banned", TimeParser.toFancyTime(time));
	}
	
	public boolean equals(Object other) {
		if(other == this) return true;
		if(!(other instanceof DLUser)) return false;
		return ((DLUser)other).getUUIDString().equals(this.getUUIDString());
	}
	
	/** @see PlayerLoop */
	public PlayerLoop getPlayerLoop() {
		return this.loop;
	}

	public void sendActionbar(String addr, String...args) {
		bukkitPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Deadlight.msg(this, addr, args)));
	}
	
	public double getMaxHealth() {
		return bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
	}
	
	public void heal() {
		bukkitPlayer.setHealth(getMaxHealth());
		bukkitPlayer.setFoodLevel(20);
	}
	
	public void addExp(int amount) {
		if(TimeKeeper.isAnyMonthlyEventActive()) {
			amount *= 2;
		}
		if(this.isInMatch() && this.isKiller()) {
			boolean killerLevelUp = getKillerData().addExp(amount);
			if(killerLevelUp) {
				send("killer_level_up", killerType.getName(), ""+killerData.get(killerType.name()).level);
			}
		}
		amount *= OfferingManager.getXPBoost();
		exp += amount;
		boolean hasLevelChanged = false;
		while(exp > getLevelUpExp()) {
			if(prestige < MAX_PRESTIGE) {
				if(level >= MAX_LEVEL) {
					level = MAX_LEVEL;
					exp = getLevelUpExp();
					return;
				}
			}
			exp -= getLevelUpExp();
			level ++;
			blood = blood.add(BigInteger.valueOf(100));
			if(!hasLevelChanged) hasLevelChanged = true;
		}
		if(hasLevelChanged) {
			send("level_up", ""+level);
			if(level >= 100 && prestige < 10) {
				send("prestige_available");
			}
			playSound(Sound.ENTITY_PLAYER_LEVELUP, 2);
		}
	}
	
	@SuppressWarnings("deprecation")
	public boolean isOnGround() {
		return bukkitPlayer.isOnGround();
	}
	
	public void prestige() {
		if(level == MAX_LEVEL) {
			level = 0;
			exp = 0;
			prestige++;
			send("level_prestige", "Prestige "+prestige);
			Location location = getLocation();
			location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location.add(new Vector(0, 1, 0)), 50, 1, 1, 1, 0);
			purchasedPerks = new HashMap<>();
			killerPerks = new ArrayList<>();
			survivorPerks = new ArrayList<>();
			equippedItem = null;
			ScoreEvent prestigeBlood = new ScoreEvent("prestige", ScoreType.BLOOD);
			blood = blood.add(BigInteger.valueOf(prestigeBlood.getAmount()));
			addScoreEvent(new ScoreEvent("prestige", ScoreType.SOULS));
			for(DLUser user : PlayerManager.getPlayers()) {
				user.send("global_level_prestige", getName(), "Prestige " + prestige);
			}
			savePlayerData();
			SongManager.playSong(SongManager.getSong("cheers"), Bukkit.getOnlinePlayers());
		}
	}
	
	String getQueueString() {
		if(this.lobby != null && !Deadlight.queue.players.contains(this)) {
			return Deadlight.msg(this, "state_in_match");
		}
		if(Deadlight.queue.players.contains(this)) {
			return Deadlight.msg(this, "state_in_queue", new String[] {""+Deadlight.queue.players.size()});
		}
		else if(this.isQueueBanned()) {
			return Deadlight.msg(this, "state_queue_banned", new String[] {""+this.getQueueBanTime() });
		}
		else {
			return Deadlight.msg(this, "state_not_in_queue");
		}
	}
	
	String getPingString() {
		ChatColor color;
		int ping = getPing();
		if(ping < 150) {
			color = ChatColor.GREEN;
		}else if(ping < 300) {
			color = ChatColor.YELLOW;
		}else if(ping < 500) {
			color = ChatColor.RED;
		}else{
			color = ChatColor.DARK_RED;
		}
		return color + "" + getPing() + "ms";
	}
	
	public void queue() {
		if(Deadlight.locked) {
			send("user_queue_locked");
			return;
		}
		if(!hasReadTutorial) {
			send("user_read_tutorial");
			return;
		}
		if(isQueueBanned()) {
			send("user_queue_banned", TimeParser.toLongFormTime( this.queueBan - System.currentTimeMillis()));
			return;
		}
		if(getLobby() != null) {
			send("match_disallow_command");
			return;
		}
		
		// join a queue
		if(!Deadlight.queue.players.contains(this)) {
			Deadlight.queue.addDLUser(this);
			send("user_join_queue");
		}
		else{
			showQueueInventory();
		}
	}
	
	public double getUncursingSpeed() {
		return mpd.uncursingSpeed;
	}
	
	public double getPoweringSpeed() {
		return mpd.poweringSpeed;
	}
	
	public double getHealingSpeed() {
		MatchSettings settings = getLobby().settings;
		return mpd.healingSpeed * settings.healingSpeed;
	}
	
	public void sendProgress(double speed, double progress) {
		String progressString = "";
		if(speed > 1) {
			progressString = "actionbar_progress_fast";
		}else if(speed < 1){
			progressString = "actionbar_progress_slow";
		}else {
			progressString = "actionbar_progress";
		}
		bukkitPlayer.sendTitle(" ", Deadlight.msg(progressString, new String[] {"" + (progress >= 100 ? "100" : (int)progress)}), 1, 15, 1);
	}
	
	public boolean hasPerk(PerkRegistry perk) {
		if(perk.alignment == Alignment.KILLER) {
			return hasKillerPerk(perk.id);
		}else if(perk.alignment == Alignment.SURVIVOR) {
			return hasSurvivorPerk(perk.id);
		}
		return false;
	}
	
	public void addPerk(PerkRegistry perk) {
		if(perk.alignment == Alignment.KILLER) {
			if(killerPerks.size() >= PERK_CAP) {
				send("perk_too_many", killerPerks.size() + "");
				return;
			}
			killerPerks.add(perk.id);
		}else if(perk.alignment == Alignment.SURVIVOR) {
			if(survivorPerks.size() >= PERK_CAP) {
				send("perk_too_many", survivorPerks.size() + "");
				return;
			}
			survivorPerks.add(perk.id);
		}
		send("perk_equip", perk.name);
	}
	
	public void removePerk(PerkRegistry perk) {
		removePerk(perk.id, perk.alignment);
		send("perk_unequip", perk.name);
	}
	
	public String getName() {
		return bukkitPlayer.getName();
	}
	
	public Vector velocity() {
		return bukkitPlayer.getVelocity();
	}
	
	public Vector getDirection() {
		return getLocation().getDirection();
	}
	
	public Location getLocation() {
		return bukkitPlayer.getLocation();
	}
	
	public void teleportToSpawn() {
		teleportToSpawn(true);
	}
	
	public void teleportToSpawn(boolean startMusic) {
		Player p = bukkitPlayer;
		
		loop.setScoreboardEnabled(true);
		
		/* Remove all potion effects */
		for(PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
		
		removeAllEffects();
		removeAllAttributes();
		
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
		
		p.setWalkSpeed(0.2f);
		
		p.setGameMode(GameMode.ADVENTURE);
		p.setAllowFlight(checkPerm("deadlight.fly"));
		
		p.getEquipment().clear();
		p.getInventory().clear();
		if(isQueued()) {
			p.getInventory().setItem(2, SpawnItems.leaveQueue.get());
		}else {
			p.getInventory().setItem(2, SpawnItems.joinQueue.get());
		}
		
		p.getInventory().setItem(6, SpawnItems.kit.get());
		
		if(this.getFishingRod() != null && fishingRodUsesLeft > 0) {
			p.getInventory().setItem(4, fishingRodType.get());
		}
		
		getPlayerLoop().setBleedingChance(100);
		getPlayerLoop().bloodSize = 2;
		getPlayerLoop().setBloodMode(true, true);
		mpd.reset();
		getPlayerLoop().boldnessTicks = 0;
		// disable glowing
		p.setGlowing(false);
		
		Bukkit.getOnlinePlayers().forEach(other -> p.showPlayer(Deadlight.inst, other));
		
		heal();
		p.teleport(Deadlight.spawn);
		
		if(startMusic) {
			this.startTrack(DLTrack.DOOMED);
		}
	}
	
	public void useFishingRod() {
		fishingRodUsesLeft--;
		if(fishingRodUsesLeft <= 0) {
			getInventory().setItem(4, null);
			fishingRodUsesLeft = -1;
			fishingRodType = null;
			this.send("rod_expired");
		}
	}
	
	public FishingRods getFishingRod() {
		return fishingRodType;
	}
	
	public long getFishingRodUsesLeft() {
		return fishingRodUsesLeft;
	}
	
	public void applyModifier(Attribute attr, AttributeModifier modifier) {
		bukkitPlayer.getAttribute(attr).addModifier(modifier);
	}
	
	public void removeModifier(Attribute attr, AttributeModifier modifier) {
		bukkitPlayer.getAttribute(attr).removeModifier(modifier);
	}
	
	public void push(Vector normalized, double speed) {
		addVelocity(normalized.multiply(speed));
	}
	
	public void addVelocity(Vector d) {
		bukkitPlayer.setVelocity(velocity().add(d));
	}
	
	public void setSpeed(float speed) {
		bukkitPlayer.setWalkSpeed(speed);
	}
	
	public float getSpeed() {
		return bukkitPlayer.getWalkSpeed();
	}
	
	@Override
	public UUID getUUID() {
		return bukkitPlayer.getUniqueId();
	}
	
	public boolean checkPerm(String permission) {
		return bukkitPlayer.hasPermission(permission);
	}
	
	public boolean isHoldingItem(Items item) {
		if(item == null) return false;
		ItemStack heldItem = getInventory().getItemInMainHand();
		if(heldItem == null) return false;
		return item.is(heldItem);
	}

	public void useItem() {
		PlayerInventory inv = bukkitPlayer.getInventory();
		ItemStack heldItem = inv.getItemInMainHand();
		int amount = heldItem.getAmount();
		if(amount == 1) {
			inv.setItemInMainHand(new ItemStack(Material.AIR));
			getItemInventory().removeItem(equippedItem);
			if(getItemInventory().getItem(equippedItem) <= 0) {
				equippedItem = null;
			}
			addExp(150);
		}else {
			heldItem.setAmount(heldItem.getAmount()-1);
			addExp(50);
		}
		Deadlight.getPerkManager().callEvent(this, PerkEventType.USE_ITEM);
		statTracker.incrementStat(DeadlightStat.ITEMS_USED);
	}
	
	public Scoreboard getScoreboard() {
		return loop.board.getBoard();
	}
	
	public int getHeathPercent() {
		return (int)((double)(bukkitPlayer.getHealth() / getMaxHealth()) * 100);
	}
	
	/** Plays a semi-omni-directional sound */
	public void playSound(Sound sound, double pitch) {
		bukkitPlayer.playSound(getLocation(), sound, SoundCategory.MASTER, 10f, (float)pitch);
	}
	
	public void playSound(Sound sound, float volume, double pitch, Location location) {
		bukkitPlayer.playSound(location, sound, SoundCategory.MASTER, volume, (float)pitch);
	}
	
	public void playSound(Sound sound, double pitch, Location location) {
		bukkitPlayer.playSound(location, sound, SoundCategory.MASTER, 10f, (float)pitch);
	}
	
	public void addHealth(double amount) {
		// Don't allow healing with dark heart!
		if(hasEffect(BrokenEffect.class)) {
			return;
		}
		double i = bukkitPlayer.getHealth()+amount;
		if(i > getMaxHealth()) {
			heal();
			return;
		}
		bukkitPlayer.setHealth(i);
		if(lobby != null) {
			Deadlight.getPerkManager().callEvent(this, PerkEventType.SURVIVOR_HEALTH_CHANGED);
		}
	}

	public boolean isMaxHealth() {
		return bukkitPlayer.getHealth() >= getMaxHealth();
	}
	
	public boolean isInMatch() {
		return getLobby() != null;
	}
	
	public void stun(double time) {
		bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (20 * time), 1, false, false, false));
		bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (20 * time), 0, false, false, false));
		bukkitPlayer.teleport(bukkitPlayer.getLocation());
	}
	
	public void stun(DLUser stunner, double time) {
		stun(time);
		stunner.addScoreEvent(new ScoreEvent("stunned", ScoreType.BLOOD));
	}
	
	// Status Effect API
	
	public void applyEffect(Effect effect) {
		effect.apply(this);
		if(!effect.isInfiniteDuration()) {
			effect.startTimer(this);
		}
		activeEffects.add(effect);
		if(effect instanceof ExposedEffect) {
			this.send("exposed");
			this.playSound(Sound.ENTITY_WITHER_DEATH, 2);
		}
	}

	public void removeEffect(Class<? extends Effect> effectClass) {
		ArrayList<Effect> toRemove = new ArrayList<>();
		for(Effect effect : activeEffects) {
			if(effect.getClass().equals(effectClass)) {
				Timer effectTimer = effect.getTimer();
				if(effectTimer == null) {
					effect.remove(this);
				}else {
					effectTimer.stop();
				}
				toRemove.add(effect);
			}
		}
		for(Effect effect : toRemove) {
			activeEffects.remove(effect);
		}
	}
	
	public void removeEffectWithId(long id) {
		ArrayList<Effect> toRemove = new ArrayList<>();
		for(Effect effect : activeEffects) {
			if(effect.getId() == id) {
				Timer effectTimer = effect.getTimer();
				if(effectTimer == null) {
					effect.remove(this);
				}else {
					effectTimer.stop();
				}
				toRemove.add(effect);
			}
		}
		for(Effect effect : toRemove) {
			activeEffects.remove(effect);
		}
	}
	
	public boolean hasEffect(Class<? extends Effect> class1) {
		for(Effect effect : activeEffects) {
			if(effect.getClass().equals(class1)) {
				return true;
			}
		}
		return false;
	}
	
	public void removeAllEffects() {
		for(Effect effect : activeEffects) {
			Timer effectTimer = effect.getTimer();
			if(effectTimer == null) {
				effect.remove(this);
			}else {
				effectTimer.stop();
			}
		}
		activeEffects.clear();
	}
	
	// Glowing API
	
	public void glowToSurvivors(long duration) {
		mpd.survivorGlow = System.currentTimeMillis() + duration;
		bukkitPlayer.setGlowing(true); // Update Metadata
	}
	
	public void glowToKiller(long duration) {
		mpd.killerGlow = System.currentTimeMillis() + duration;
		bukkitPlayer.setGlowing(true); // Update Metadata
	}
	
	public void glow(long duration) {
		mpd.globalGlow  = System.currentTimeMillis() + duration;
		bukkitPlayer.setGlowing(true); // Update Metadata
	}
	
	public void glowToEntity(long duration, Entity entity) {
		mpd.glowingToEntity.put(entity.getUniqueId(), System.currentTimeMillis() + duration);
		entity.setGlowing(true); // Update Metadata
	}
	
	public boolean canGlow() {
		return shouldGlowToKiller() || shouldGlowToSurvivors() || shouldGlowToAll() || !mpd.glowingToEntity.isEmpty();
	}
	
	public boolean shouldGlowToKiller() {
		return mpd.killerGlow > System.currentTimeMillis();
	}
	
	public boolean shouldGlowToSurvivors() {
		return mpd.survivorGlow > System.currentTimeMillis();
	}
	
	public boolean shouldGlowToAll() {
		return mpd.globalGlow > System.currentTimeMillis();
	}
	
	public boolean shouldGlowToEntity(UUID uuid) {
		if(uuid == null) return false;
		if(!mpd.glowingToEntity.containsKey(uuid)) return false;
		
		boolean glow = mpd.glowingToEntity.get(uuid).longValue() > System.currentTimeMillis();
		
		if(!glow) {
			mpd.glowingToEntity.remove(uuid);
		}
		
		return glow;
	}
	
	/** Handling packets */
	
	// Return true if the provided entity should glow to this player.
	// Also passes the current status of glowing
	public boolean glowingUpdate(boolean glowing, Entity entity) {
		if(entity instanceof FishHook) {
			ProjectileSource source = ((FishHook) entity).getShooter();
			if(source instanceof Entity) {
				Entity shooter = (Entity)source;
				return shooter.getUniqueId().equals(getUUID());
			}
		}
		
		if(isInMatch()) {
			if(entity instanceof Shulker) {
				return isKiller() || isSpectating();
			}
			if(entity instanceof ItemFrame) {
				return isKiller();
			}
			if(entity instanceof ArmorStand) {
				if(entity.getScoreboardTags().contains("revivingHead")) {
					return !isKiller();
				}
			}
			if(entity instanceof Player) {
				DLUser glower = PlayerManager.getUser(entity.getUniqueId());
				if(glower == null) return glowing;
				// This entity is glowing to the killer, and this is the killer
				boolean glowToKiller = (glower.shouldGlowToKiller() && this.isKiller());
				// This entity is glowing to survivors, and this is a survivor
				boolean glowToSurvivors = (!this.isKiller() && glower.shouldGlowToSurvivors());
				// This entity is glowing to everybody
				boolean glowToAll = glower.shouldGlowToAll();
				// This entity is glowing to a specific entity, and this is the entity it's glowing to.
				boolean glowDirect = glower.shouldGlowToEntity(this.getUUID());
				return (
						glowToKiller || // Make this player glow to killer
						glowToSurvivors || // Make this player glow to survivors
						glowToAll || // Make this player glow to everybody
						glowDirect // Make this player glow to this specific player
						);
			}
		}
		return glowing;
	}
	
	/*@SuppressWarnings("unchecked")
	public boolean onPacketWrite(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws IllegalArgumentException, IllegalAccessException {
		Class<?> cls = packet.getClass();
		String clsName = cls.getSimpleName();
		if(clsName.equals("PacketPlayOutEntityMetadata")) {
			int entityId = NMSHandler.getInt(packet, "a");
			Entity entity = NMSHandler.getEntityById(entityId);
			if(entity == null) {
				Deadlight.debug("No entity exists with id " + entityId);
				return true;
			}
			Field f = NMSHandler.getField(packet, "b");
			f.setAccessible(true);
			List<DataWatcher.Item<?>> dataList = (List<Item<?>>) f.get(packet);
			Item<Object> data = (Item<Object>) dataList.get(0);
			Object metadataStatus = data.b();
			
			if((metadataStatus instanceof Byte) && data.a().a() == 0) {
				Byte b = (Byte) metadataStatus;
				boolean glowing = (b & 0x40) != 0; // is the seventh bit set?
				boolean shouldGlow = glowingUpdate(glowing, entity);
				
				if(!shouldGlow && glowing) {
					b = Byte.valueOf((byte) (b & ~(b & 0x40))); // unset the 7th bit to stop glowing
				}
				if(shouldGlow && !glowing) {
					b = Byte.valueOf((byte) (b | 0x40)); // set the 7th bit to start glowing
				}
				data.a(b);
				dataList.set(0, data);
				f.set(packet, dataList);
			}
			f.setAccessible(false);
		}
		return true;
	}*/
	
	public boolean onPacketRead(ChannelHandlerContext context, Object packet) {
		//Deadlight.debug(getName() + ": " + packet.getClass().getSimpleName());
		return true;
	}

	public void spectate() {
		Player p = bukkitPlayer;
		
		/* Remove all potion effects */
		for(PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
		
		removeAllEffects();
		removeAllAttributes();
		
		p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
		
		p.setWalkSpeed(0.2f);
		
		p.setGameMode(GameMode.ADVENTURE);
		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, false));
		p.setAllowFlight(true);
		
		p.getInventory().clear();
		p.getInventory().setItem(8, SpawnItems.leaveMatch.get());
		heal();
		spectatingUpdate();
	}
	
	/*
	 * Check if
	 * -> The player's soul is on the ground
	 * -> Someone is carrying the player's soul
	 * -> The player's soul is being revived
	 * -> The player can be revived
	 * -> The player is not an ambient spectator
	 */
	public boolean isRevivePossible() {
		if(mpd.revived) return false;
		if(!mpd.allowRevive) return false;
		if(lobby.isPlayerInIndex(-1, this)) return false;
		if(!getLobby().isBeingRevived(this))
			if(getLobby().getSoulCarrier(this) == null)
				if(getLobby().getPlayerSoul(this.getUUID()) == null)
					return false;
		return true;
	}
	
	public void spectatingUpdate() {
		
		for(DLUser user : lobby.getPlayers()) {
			if(!user.isSpectating()) {
				user.getPlayer().hidePlayer(Deadlight.inst, bukkitPlayer);
			}else {
				user.getPlayer().showPlayer(Deadlight.inst, bukkitPlayer);
			}
		}
		for(DLUser user : lobby.externalSpectators) {
			user.getPlayer().showPlayer(Deadlight.inst, bukkitPlayer);
		}
		if(isRevivePossible() && !bloodBufferHasCleared) {
			sendActionbar("player_soul_on_ground");
		}
	}

	public void addDropReward(DropResult result) {
		switch(result.category) {
		case SOULS:{
			long amount = ((FungibleDropResult)result).drop;
			souls = souls.add(BigInteger.valueOf(amount));
		} break;
		case BLOOD:
		{
			long amount = ((FungibleDropResult)result).drop;
			setBloodWithCap(blood.add(BigInteger.valueOf(amount)));
		} break;
		case ITEMS: {
			ItemDropResult drop = (ItemDropResult) result;
			String itemName = drop.drop.name;
			long itemCount = drop.drop.count.getAmount();
			inventory.items.put(itemName, (int) (inventory.items.getOrDefault(itemName, 0)+itemCount));
		} break;
		case OFFERING: {
			OfferingDropResult drop = (OfferingDropResult) result;
			String offeringName = drop.drop.name;
			long offeringCount = drop.drop.count.getAmount();
			offerings.put(offeringName, (int) (offerings.getOrDefault(offeringName, 0)+offeringCount));
		} break;
		default:
			break;
		}
	}
	
	public String getFormattedNickName() {
		if(checkPerm("deadlight.nick.format")) {
			return FormatUtil.color(nickname);
		}
		else if(checkPerm("deadlight.nick.colored")) {
			return FormatUtil.colorOnly(nickname);
		}else {
			return nickname;
		}
	}

	public KillerType getKillerType() {
		return killerTypeInstance;
	}
	
	public KillerData getKillerData() {
		if(!killerData.containsKey(killerType.name())) {
			killerData.put(killerType.name(), new KillerData(new JSONObject()));
		}
		return killerData.get(killerType.name());
	}

	public Location getTargetLocation(double range) {
		Player player = getPlayer();
		RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), getDirection(), range);
		if(result != null && result.getHitBlock() != null) {
			Block tpBlock = result.getHitBlock().getRelative(result.getHitBlockFace());
			if(!tpBlock.getType().isSolid())
				return tpBlock.getLocation().add(0.5, 0.5, 0.5);
		}
		return null;
	}
	
	public int getKillerWeight() {
		return killerWeight;
	}
	
	public int getObsessionWeight() {
		return obsessionWeight + (this.hasSurvivorPerk("HealthyObsession") ? 3 : 0);
	}
	
	@Override
	public long getPlaytime() {
		return playtime + (System.currentTimeMillis() - joinTime);
	}
	
	public StatTracker getStatTracker() {
		return statTracker;
	}

	public MinehutRank getMinehutRank() {
		return Deadlight.getMinehutRankManager().getRank(this.minehutRankName);
	}
	
	public void startTrack(DLTrack track) {
		stopTrack();
		
		this.trackActive = track;
		this.trackStarted = System.currentTimeMillis();
		this.trackLoop = true;
		if(track != null && musicEnabled) {
			this.playTrack();
		}
	}
	
	public void startTrack(DLTrack track, boolean loop) {
		stopTrack();
		this.trackActive = track;
		this.trackLoop = loop;
		if(track != null && musicEnabled) {
			this.playTrack();
		}
	}
	
	public void stopTrack() {
		if(trackActive == null) return;
		bukkitPlayer.stopSound(trackActive.getResourcePackId());
	}

	public void playTrack() {
		if(!musicEnabled) return;
		bukkitPlayer.playSound(getLocation(), trackActive.getResourcePackId(), trackActive.getVolume(), 1);
		this.trackStarted = System.currentTimeMillis();
	}
	
	public void setMusicEnabled(boolean toggle) {
		if(!musicEnabled && toggle) {
			musicEnabled = true;
			startTrack(trackActive);
			return;
		}else if(!toggle) {
			stopTrack();
		}
		musicEnabled = toggle;
	}
}
