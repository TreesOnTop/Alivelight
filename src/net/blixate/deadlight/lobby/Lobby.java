package net.blixate.deadlight.lobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.kit.items.Items;
import net.blixate.deadlight.kit.items.flowers.FlowerTotem;
import net.blixate.deadlight.kit.items.flowers.HealingFlower;
import net.blixate.deadlight.kit.items.flowers.KnowledgeFlower;
import net.blixate.deadlight.kit.items.flowers.SpeedFlower;
import net.blixate.deadlight.kit.items.flowers.UndyingFlower;
import net.blixate.deadlight.kit.perks.Perk;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.kit.perks.PerkRegistry;
import net.blixate.deadlight.kit.type.KillerType;
import net.blixate.deadlight.kit.type.addons.AddonManager;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.maps.GameMap;
import net.blixate.deadlight.maps.MapClearer;
import net.blixate.deadlight.maps.MapLoader;
import net.blixate.deadlight.maps.environment.ExitPortal;
import net.blixate.deadlight.maps.environment.MapObject;
import net.blixate.deadlight.maps.environment.MapPosition;
import net.blixate.deadlight.maps.environment.PlayerSoul;
import net.blixate.deadlight.maps.environment.PortalFragment;
import net.blixate.deadlight.maps.environment.SurvivorObjective;
import net.blixate.deadlight.maps.environment.states.BlockedState;
import net.blixate.deadlight.music.DLTrack;
import net.blixate.deadlight.player.Alignment;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.KillerData;
import net.blixate.deadlight.player.MatchPlayerData;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.player.effects.BrokenEffect;
import net.blixate.deadlight.player.effects.DeterminedEffect;
import net.blixate.deadlight.player.effects.ExhaustedEffect;
import net.blixate.deadlight.player.effects.ExposedEffect;
import net.blixate.deadlight.player.effects.HemorrhageEffect;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.player.effects.KnockedEffect;
import net.blixate.deadlight.player.effects.MangledEffect;
import net.blixate.deadlight.player.stats.DeadlightStat;
import net.blixate.deadlight.scoreboard.SidebarBoard;
import net.blixate.deadlight.scoreboard.SidebarBuilder;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.random.WeightedDropTable;

/**
 * <h1>Represents a game match</h1>
 */
public class Lobby {
	
	public static enum StartingState {
		SCOREBOARD_SETUP, SELECTING_ROLES, LOADING_MAP, PLACING_OBJECTIVES, SETTING_UP_PLAYERS, COMPLETED;
	}
	
	public static NamespacedKey ENTITY_IDENTIFIER = new NamespacedKey(Deadlight.inst, "lobbyEntity");
	
	public MatchSettings settings;
	
	public GameMap map;
	public int killer;
	public int obsession;
	
	private GameState state;
	
	private HashMap<Integer, DLUser> players;
	public HashSet<DLUser> externalSpectators;
	private int index = 0;
	private TimeLimit timeLimit;
	private PortalFragment[] gens;
	private ExitPortal[] portals;
	
	private ArrayList<PlayerSoul> playerSouls;
	private ArrayList<FlowerTotem> flowers;
	
	public int gensDone = 0;
	public int requiredGens = 0;
	public int portalsOpened = 0;
	public int escapes = 0;
	public int revives = 0;
	
	public int whispers = -1;
	
	public KillerType killerType;
	
	// Scoreboard stuff, about to get more complicated than this believe it or not...
	SidebarBoard board;
	public Scoreboard score;
	public Team killerTeam;
	public Team survivorTeam;
	public Team spectatorTeam;
	public Team fragmentTeam;
	
	Location offset;
	
	public boolean canTrackChallenges;
	
	// A random number to identify entities attached to this lobby.
	public int randomEntityLobbyId;
	
	private StartingState startingState;
	
	public void setStartingState(StartingState state) {
		this.startingState = state;
	}
	
	public Lobby(int index, DLUser[] users) {
		randomEntityLobbyId = Deadlight.RNG.nextInt(0, Integer.MAX_VALUE);
		settings = new MatchSettings();
		
		setStartingState(StartingState.SCOREBOARD_SETUP);
		
		score = Bukkit.getScoreboardManager().getNewScoreboard();
		externalSpectators = new HashSet<>();
		board = new SidebarBoard(FormatUtil.color("&9&lDEADLIGHT"), score);
		killerTeam = score.registerNewTeam("killers");
		killerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
		killerTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
		killerTeam.setColor(ChatColor.DARK_RED);
		
		survivorTeam = score.registerNewTeam("survivors");
		if(settings.survivorsShowNametags) {
			survivorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
		}else {
			survivorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
		}
		survivorTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);
		survivorTeam.setCanSeeFriendlyInvisibles(true);
		survivorTeam.setColor(ChatColor.GREEN);
		
		spectatorTeam = score.registerNewTeam("spectators");
		spectatorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
		spectatorTeam.setCanSeeFriendlyInvisibles(true);
		spectatorTeam.setColor(ChatColor.GRAY);
		
		fragmentTeam = score.registerNewTeam("fragments");
		fragmentTeam.setColor(ChatColor.GOLD);
		
		// player stuff
		if(!Deadlight.debug) {
			this.canTrackChallenges = users.length >= 4;
		}else {
			this.canTrackChallenges = true;
		}
		
		
		setStartingState(StartingState.SELECTING_ROLES);
		this.players = new HashMap<Integer, DLUser>();
		for(int i = 0; i < users.length; i ++) {
			DLUser user = users[i];
			user.getPlayerLoop().setScoreboardEnabled(false);
			board.addPlayer(user.getPlayer());
			players.put(i, user);
		}
		WeightedDropTable<Integer> killerTable = new WeightedDropTable<>();
		for(int i = 0; i < users.length; i ++) {
			killerTable.addElement(i, Math.max(users[i].getKillerWeight(), 1));
		}
		killer = killerTable.pick();
		WeightedDropTable<Integer> obsessionTable = new WeightedDropTable<>();
		for(int i = 0; i < users.length; i ++) {
			obsessionTable.addElement(i, Math.max(users[i].getObsessionWeight(), 1));
		}
		obsession = obsessionTable.pick();
		if(obsession == killer) {
			if(obsession > 0)
				obsession--;
			else
				obsession++;
		}
		
		this.playerSouls = new ArrayList<>();
		this.flowers = new ArrayList<>();
		this.index = index;
	}
	
	public void updateGlowing() {
		for(String uuidString : fragmentTeam.getEntries()) {
			UUID uuid = UUID.fromString(uuidString);
			Entity entity = Bukkit.getEntity(uuid);
			if(entity != null) {
				boolean glowing = entity.isGlowing();
				entity.setGlowing(glowing);
			}
		}
	}
	
	public void addSpectator(DLUser user) {
		externalSpectators.add(user);
		if(user.getLobby() == null || !user.getLobby().equals(this)) {
			user.setGameLobby(this);
			user.getPlayerLoop().setScoreboardEnabled(false);
			board.addPlayer(user.getPlayer());
			timeLimit.bossbar.addPlayer(user.getPlayer());
		}
		spectatorTeam.addEntry(user.getPlayer().getName());
		updateGlowing();
		user.startTrack(DLTrack.COLD_SPIRITS);
	}
	
	public void removeSpectator(DLUser user) {
		user.setGameLobby(null);
		user.getPlayerLoop().setScoreboardEnabled(true);
		spectatorTeam.removeEntry(user.getPlayer().getName());
		timeLimit.bossbar.removePlayer(user.getPlayer());
		user.teleportToSpawn();
		updateGlowing();
	}
	
	public void start() throws Exception {
		if(players.size() == 2) {
			requiredGens = 1;
		}else {
			requiredGens = players.size();
		}
		
		setState(GameState.COMPLETE_FRAGMENTS);
		
		/* Setup map */
		setStartingState(StartingState.LOADING_MAP);
		map = MapLoader.getRandomMap();
		offset = MapLoader.loadMap(getMap().getName(), this.index);
		setStartingState(StartingState.PLACING_OBJECTIVES);
		ArrayList<SurvivorObjective> objs = new ArrayList<>();
		for(MapPosition genLoc : getMap().getGenerators()) {
			Deadlight.debug("Added generator " + genLoc);
			objs.add(new PortalFragment(this, genLoc));
		}
		gens = new PortalFragment[objs.size()];
		gens = objs.toArray(gens);
		objs = new ArrayList<>();
		for(MapPosition genLoc : getMap().getPortals()) {
			Deadlight.debug("Added portal " + genLoc);
			objs.add(new ExitPortal(this, genLoc));
		}
		portals = new ExitPortal[objs.size()];
		portals = objs.toArray(portals);
		for(PortalFragment gen : gens) {
			gen.generate();
			gen.createHighlightShulker(this);
		}
		for(ExitPortal portal : portals) {
			portal.generate();
		}
		// Spawn points
		Location[] spawns = getMap().createSpawns(offset);
		// Swap spawn locations
		if(Deadlight.RNG.nextBoolean()) {
			Location loc1 = spawns[0];
			spawns[0] = spawns[1];
			spawns[1] = loc1;
		}
		/**
		 * Player Setup
		 * 
		 * Set up killers and survivors with the required stuff.
		 */
		setStartingState(StartingState.SETTING_UP_PLAYERS);
		for(DLUser player : players.values()) {
			player.setGameLobby(this);
			if(Deadlight.queue.players.contains(player)) {
				Deadlight.queue.removeDLUser(player);
			}
			
			setupPlayer(player);
			// set poof effect at spawn
			ParticlesUtil.spawnParticle(Particle.CLOUD, offset, killer, whispers, index, gensDone, escapes);
			if(isKiller(player)) {
				setupKiller(player, spawns[0]);
			}else{
				setupSurvivor(player, spawns[1]);
			}
			player.heal();
			if(!player.perks.isEmpty()) {
				Player p = player.getPlayer();
				p.sendMessage(ChatColor.YELLOW + "Your Perks:");
				for(Perk perk : player.perks) {
					p.sendMessage(ChatColor.YELLOW + "- " + perk.getRegistry().name + ChatColor.GOLD + ChatColor.BOLD + " " + "I".repeat(perk.getTier()));
				}
			}else {
				player.getPlayer().sendMessage(ChatColor.RED + "You don't have any perks!");
			}
			if(player.isKiller()) {
				KillerData data = player.getKillerData();
				if(!data.selectedAugments.isEmpty()) {
					player.getPlayer().sendMessage(ChatColor.YELLOW + "Your Augments:");
					for(String addon : data.selectedAugments) {
						String addonName = AddonManager.getNameById(player.killerType, addon);
						player.getPlayer().sendMessage(ChatColor.YELLOW + "- " + addonName);
					}
				}else {
					player.getPlayer().sendMessage(ChatColor.RED + "You don't have any augments!");
				}
			}
		}
		for(DLUser player : players.values()) {
			Deadlight.getPerkManager().callEvent(player, PerkEventType.LOBBY_START);
		}
		
		setStartingState(StartingState.COMPLETED);
	}
	
	/*public void addGlow(Entity entity, Player player) {
		ProtocolManager pm = ProtocolLibrary.getProtocolManager();
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, entity.getEntityId()); //Set packet's entity id
		WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
		Serializer serializer = Registry.get(Byte.class); //Found this through google, needed for some stupid reason
		watcher.setEntity(entity); //Set the new data watcher's target
		watcher.setObject(0, serializer, (byte) (0x40)); //Set status to glowing, found on protocol page
		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects()); //Make the packet's datawatcher the one we created
		pm.sendServerPacket(player, packet);
	}
	
	public void removeGlow(Entity entity, Player player) {
		ProtocolManager pm = ProtocolLibrary.getProtocolManager();
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, entity.getEntityId()); //Set packet's entity id
		WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
		Serializer serializer = Registry.get(Byte.class); //Found this through google, needed for some stupid reason
		watcher.setEntity(entity); //Set the new data watcher's target
		watcher.setObject(0, serializer, 0); //Set status to glowing, found on protocol page
		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects()); //Make the packet's datawatcher the one we created
		pm.sendServerPacket(player, packet);
	}*/
	
	/** Run every second this lobby exists */
	public void tick() {
		try {
			for(MapObject gen : gens) {
				if(gen == null) continue;
				SurvivorObjective obj = (SurvivorObjective)gen;
				obj.tickStates(this);
				obj.tick(this);
				/*if(obj.getHighlighter() != null) {
					for(DLUser user : this.getPlayers()) {
						if(user.isKiller()) {
							addGlow(obj.getHighlighter(), user.getPlayer());
						}else {
							removeGlow(obj.getHighlighter(), user.getPlayer());
						}
					}
				}*/
				
			}
			for(MapObject gen : portals) {
				if(gen == null) continue;
				SurvivorObjective obj = (SurvivorObjective)gen;
				obj.tickStates(this);
				obj.tick(this);
				/*if(obj.getHighlighter() != null) {
					for(DLUser user : this.getPlayers()) {
						if(user.isKiller()) {
							addGlow(obj.getHighlighter(), user.getPlayer());
						}else {
							removeGlow(obj.getHighlighter(), user.getPlayer());
						}
					}
				}*/
			}
			if(flowers != null && !flowers.isEmpty()) {
				for(FlowerTotem totem : flowers) {
					totem.tick(this);
					ParticlesUtil.spawnParticle(Particle.SOUL_FIRE_FLAME, totem.getLocation().clone().add(.5,.5,.5), 5, 0, 0.1, 0.1, 0.1);
				}
			}
		}catch(Throwable t) {
			Deadlight.error(t);
		}
		/* Scoreboard */
		SidebarBuilder sb = new SidebarBuilder();
		sb.write("&7Map: &e" + map.getFancyName());
		sb.write("&7" + getStateString());
		sb.writeBlank();
		String pres = Deadlight.prestigeColors[getKiller().getPrestige()];
		String level = pres + getKiller().getLevel();
		if(getKiller().getPing() > 500) {
			level += " &c⌚";
		}
		sb.write(pres + level + " &4☠ " + getKiller().getName());
		if(getPlayers().length == 1) {
			sb.write("&cThere are no survivors.");
		}else {
			for(DLUser player : getSurvivors()) {
				String name = player.getName();
				if(whispers != -1) {
					if(player.getLocation().distance(getKiller().getLocation()) < whispers) {
						name = "&e" + name;
					}
				}
				int health = (int)(player.getPlayer().getHealth() / 2d);
				int maxHealth = (int)(player.getMaxHealth() / 2d);
				String icon = "✚";
				if(player.hasEffect(KnockedEffect.class)) {
					icon = "&4❌";
				}
				else if(player.hasEffect(BrokenEffect.class)) {
					icon = "&8" + icon;
				}
				else if(player.hasEffect(MangledEffect.class)) {
					icon = "&6" + icon;
				}
				if(player.hasEffect(DeterminedEffect.class)) {
					icon += "&6🛡";
				}
				if(player.hasEffect(ExposedEffect.class)) {
					icon += "&4☠";
				}
				if(player.hasEffect(HemorrhageEffect.class)) {
					icon += "&7❣";
				}
				if(player.hasEffect(ExhaustedEffect.class)) {
					icon += "&7⚡";
				}
				if(player.hasEffect(InfectedEffect.class)) {
					icon += "&7☣";
				}
				
				pres = Deadlight.prestigeColors[player.getPrestige()];
				level = pres + player.getLevel() + " ";
				if(player.isObsession()) level += "&4☽ ";
				if(player.getPing() > 500) {
					level += "&c⌚ ";
				}
				sb.write(level + "&f" + name + " &c" + health + "/" + maxHealth + icon);
			}
		}
		
		int spectators = 0;
		for(DLUser player : getPlayers()) {
			if(player == null) continue;
			if(player.isSpectating()) {
				if(player.isRevivePossible()) {
					sb.write("&8&m" + player.getName() + "&b ☠");
				}else {
					spectators ++;
				}
			}
		}
		spectators += externalSpectators.size();
		sb.writeIf(spectators > 0, "&7" + spectators + " Spectators");
		sb.writeBlank();
		sb.write("&7Deadlight.minehut.gg");
		sb.build(board);
		this.updateGlowing();
	}
	
	private void setupPlayer(DLUser player) {
		Player p = player.getPlayer();
		p.getInventory().clear();
		p.setFlying(false);
		p.setAllowFlight(false);
		p.setGameMode(GameMode.SURVIVAL);
		player.getPlayerLoop().setBloodMode(true, true);
		player.mpd = new MatchPlayerData();
		player.getMatchData().reset();
		player.perks = new ArrayList<>();
		for(DLUser user : getPlayers()) {
			user.getPlayer().showPlayer(Deadlight.inst, p);
		}
		List<String> perks = isKiller(player) ? player.getKillerPerks() : player.getSurvivorPerks();
		List<PerkRegistry> removePerks = new ArrayList<>();
		for(String perkId : perks) {
			PerkRegistry registry = Deadlight.getPerkManager().getRegistry(perkId);
			if(registry == null) {
				player.removePerk(perkId, isKiller(player) ? Alignment.KILLER : Alignment.SURVIVOR);
				continue;
			}
			Perk perk = registry.createInstance();
			if(perk == null) {
				removePerks.add(registry);
				continue;
			}
			perk.setup(player);
			player.perks.add(perk);
		}
		if(removePerks != null || !removePerks.isEmpty()) {
			for(PerkRegistry reg : removePerks) {
				player.removePerk(reg);
			}
		}
		if(killerTeam.hasEntry(player.getName())) {
			killerTeam.removeEntry(player.getName());
		}
		if(survivorTeam.hasEntry(player.getName())) {
			survivorTeam.removeEntry(player.getName());
		}
		if(spectatorTeam.hasEntry(player.getName())) {
			spectatorTeam.removeEntry(player.getName());
		}
	}
	
	private void setupSurvivor(DLUser player, Location loc) {
		Player p = player.getPlayer();
		p.teleport(loc);
		p.setWalkSpeed((float)settings.survivorSpeed);
		
		player.sendTitle("survivor_spawn_title", "survivor_spawn_subtitle");
		player.send("role_survivor");
		
		if(player.isObsession()) {
			player.send("surv_become_obsession");
		}
		player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(settings.survivorHealth);
		equipSurvivor(player);
		player.playSound(Sound.BLOCK_BELL_RESONATE, 1.5);
		survivorTeam.addEntry(player.getName());
		player.killerWeight ++;
		Deadlight.getPerkManager().callEvent(player, PerkEventType.SURVIVOR_SPAWN);
		
		player.startTrack(DLTrack.LOST);
	}
	
	private void setupKiller(DLUser player, Location loc) {
		Player p = player.getPlayer();
		PlayerInventory inv = p.getInventory();
		p.teleport(loc);
		
		player.sendTitle("killer_spawn_title", "killer_spawn_subtitle");
		player.send("role_killer");
		
		killerType = player.killerType.create();
		
		player.killerTypeInstance = killerType;
		killerType.setup(player);
		killerType.equipKiller(inv);
		p.setWalkSpeed(killerType.getMovementSpeed());
		player.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5);
		killerTeam.addEntry(player.getName());
		player.killerWeight = 1;
		
		player.startTrack(DLTrack.TYRANNY);
	}
	
	/** Give the player their item if they have one. */
	public static void equipSurvivor(DLUser player) {
		/* If the player has an item, assign it to player.equippedItem */
		if(player.getEquippedItem() != null) {
			Items item = Items.valueOf(player.equippedItem);
			player.getInventory().addItem(item.stack());
		}
	}
	
	/** True if all generators required are completed. */
	public boolean isEndGame() {
		return state == GameState.END_GAME_COLLAPSE;
	}
	
	public void setState(GameState newState) {
		if(state == newState) {
			return;
		}
		state = newState;
		if(timeLimit != null) {
			if(!timeLimit.task.isCancelled()) {
				timeLimit.task.cancel();
				for(DLUser player : players.values()) timeLimit.bossbar.removePlayer(player.getPlayer());
				for(DLUser player : externalSpectators) timeLimit.bossbar.removePlayer(player.getPlayer());
			}
		}
		timeLimit = state.createTimer(this);
		timeLimit.start(state.getTimeDuration());
	}
	
	public GameState getState() {
		return state;
	}
	
	public void end() {
		if(timeLimit != null) timeLimit.task.cancel();
		// Delete all entities.
		for(PortalFragment gen : gens) {
			if(gen == null) continue;
			gen.removeHighlightShulker();
			gen.removeDisplayModel();
		}
		for(ExitPortal gen : portals) {
			if(gen == null) continue;
			gen.removeHighlightShulker();
		}
		for(int i = 0; i < playerSouls.size(); i++) {
			removePlayerSoul(0);
		}
		
		try {
			getKiller().killerTypeInstance.initCleanup();
			getKiller().killerTypeInstance = null;
		}catch(Throwable t) {
			t.printStackTrace();
		}
		
		for(DLUser spectator : externalSpectators) {
			spectator.teleportToSpawn();
			removeSpectator(spectator);
		}
		externalSpectators.clear();
		
		for(DLUser player : players.values()){
			player.clearTitle();
			removePlayer2(player);
			player.teleportToSpawn();
			player.clearBloodBuffer();
		}
		Deadlight.getLobbyManager().removeLobby(index);
		if(board != null)
			board.unregister();
		board = null;
		// clear the map
		int[] size = MapLoader.getStructure(map.getName()).getSize();
		new MapClearer(this.index).setSize(size[0], size[1], size[2]).run();
		Deadlight.getLobbyManager().lockLobby(this.index, 5);
	}
	
	public void sendGlobal(String address, String...strings) {
		for(DLUser player : players.values()) {
			player.send(address, strings);
		}
		for(DLUser spectator : externalSpectators) {
			spectator.send(address, strings);
		}
	}
	
	private void removePlayer2(DLUser user) {
		timeLimit.bossbar.removePlayer(user.getPlayer());
		user.quitLobby();
		if(killerTeam.hasEntry(user.getName())) {
			killerTeam.removeEntry(user.getName());
		}
		if(survivorTeam.hasEntry(user.getName())) {
			survivorTeam.removeEntry(user.getName());
		}
		if(spectatorTeam.hasEntry(user.getName())) {
			spectatorTeam.removeEntry(user.getName());
		}
		if(externalSpectators.contains(user)) {
			externalSpectators.remove(user);
			removeSpectator(user);
		}
		if(!user.perks.isEmpty()) {
			for(Perk perk : user.perks) {
				perk.destroy();
			}
		}
		user.getPlayerLoop().setScoreboardEnabled(true);
	}
	
	public void removePlayer(DLUser user) {
		removePlayer2(user);
		players.remove(getPlayerIndex(user));
		if(!user.isSpectating()) {
			checkState();
		}
	}
	
	public boolean isKiller(DLUser u) {
		return isPlayerInIndex(killer, u);
	}
	
	public boolean isObsession(DLUser u) {
		return isPlayerInIndex(obsession, u);
	}
	
	public boolean isSpectating(DLUser user) {
		return spectatorTeam.hasEntry(user.getName()) || externalSpectators.contains(user);
	}
	
	public DLUser getKiller() {
		return players.get(killer);
	}
	
	public DLUser getObsession() {
		return players.get(obsession);
	}
	
	public boolean isPlayerInIndex(int index, DLUser user) {
		return user.equals(players.get(index));
	}
	
	private int getPlayerIndex(DLUser user) {
		for(Integer i : players.keySet()) {
			if(players.get(i).equals(user)) {
				return i;
			}
		}
		return -1;
	}
	
	public ExitPortal getPortal(Location loc) {
		MapPosition location = MapPosition.to(loc, offset);
		for(ExitPortal obj : portals) {
			if(obj.getPosition().equals(location)) {
				return obj;
			}
		}
		return null;
	}
	
	public PortalFragment getGenerator(Location loc) {
		MapPosition location = MapPosition.to(loc, offset);
		for(PortalFragment obj : gens) {
			if(obj.getPosition().equals(location)) {
				return obj;
			}
		}
		return null;
	}
	
	/** Returns the amount of survivors alive. */
	public int getSurvivorCount() {
		return survivorTeam.getEntries().size();
	}
	
	/** Called ONLY when a player disconnects from the game.
	 * There isn't a reason to clear blood buffers here, but if they happen to be cleared that's fine.*/
	public void disconnect(DLUser user) {
		if(externalSpectators.contains(user)) {
			externalSpectators.remove(user);
			removeSpectator(user);
			return;
		}
		
		if(isSpectating(user)) {
			if(getPlayerSoul(user.getUUID()) != null) {
				sendGlobal("player_soul_left", user.getName());
				removePlayerSoul(getPlayerSoul(user.getUUID()));
				getKiller().addScoreEvent(new ScoreEvent("soul_destroyed", ScoreType.BLOOD));
			}
			DLUser carrier = getSoulCarrier(user);
			if(carrier != null) {
				sendGlobal("player_soul_left", user.getName());
				PlayerInventory inv = carrier.getInventory();
				if(inv.contains(Material.PLAYER_HEAD)) {
					// only check their hotbar
					for(int i = 0; i < 8; i++) {
						ItemStack item = inv.getItem(i);
						if(item == null) continue;
						if(item.getType().equals(Material.PLAYER_HEAD)) {
							String uuid = PlayerSoul.getOwnerUUID(item);
							if(uuid != null) {
								inv.setItem(i, null);
								break;
							}
						}
					}
				}
				getKiller().addScoreEvent(new ScoreEvent("soul_destroyed", ScoreType.BLOOD));
			}
			if(isBeingRevived(user)) {
				sendGlobal("player_soul_revive_cancelled", user.getName());
				for(MapObject object : this.getGenerators()) {
					PortalFragment frag = (PortalFragment)object;
					if(frag.isReviving()) {
						if(frag.reviving.equals(user.getUUID().toString())) {
							frag.removeDisplayModel();
							frag.setReviving(null, this);
							break;
						}
					}
				}
			}
			// we allow players who leave while dead to keep their blood
			// this might be added as a config option
			user.clearBloodBuffer();
			removePlayer(user);
			return;
		}
		user.bloodBuffer = 0; // They don't get blood when they disconnect
		if(isKiller(user)) {
			for(DLUser survivor : getSurvivors()) {
				survivor.addScoreEvent(new ScoreEvent("killer_disconnected", ScoreType.BLOOD));
			}
			sendGlobal("match_killer_disconnect");
			removePlayer2(user);
			end();
		}else {
			user.getItemInventory().removeItem(user.getEquippedItem());
			if(user.getItemInventory().getItem(user.equippedItem) <= 0) {
				user.equippedItem = null;
			}
			sendGlobal("match_survivor_disconnect");
			removePlayer(user);
			for(DLUser player : getPlayers()) {
				if(!player.equals(user))
					player.addScoreEvent(new ScoreEvent("survivor_disconnected", ScoreType.BLOOD));
			}
		}
	}
	
	private boolean isFatal(DLUser victim, double damage) {
		return (victim.getPlayer().getHealth()-damage <= 0);
	}
	
	public void lobbyDamage(double damage, DLUser user, DLUser victim, AttackType attack) {
		if(victim.isSpectating()) {
			return;
		}
		if(user == null) {
			user = getKiller();
		}
		if(attack != AttackType.TRAP) {
			user.getStatTracker().incrementStat(DeadlightStat.SURVIVORS_HIT);
			user.hitCooldown.start(user.getLobby().settings.hitCooldown);
			Player killer = user.getPlayer();
			killer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (20 * settings.hitSlowdown), 1, false, false, false));
			killer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (20 * settings.hitBlindness), 0, false, false, false));
			if(attack == AttackType.BASIC && victim.hasEffect(ExposedEffect.class)) {
				damage *= 2;
			}
			// check if this is a fatal hit
			if(isFatal(victim, damage)) {
				Deadlight.getPerkManager().callEvent(victim, PerkEventType.FATAL_HIT, attack);
				Deadlight.getPerkManager().callEvent(user, PerkEventType.FATAL_HIT, attack);
				ArrayList<FlowerTotem> totems = new ArrayList<>();
				for(FlowerTotem totem : flowers) {
					if(totem instanceof UndyingFlower) {
						totems.add(totem);
					}
				}
				for(FlowerTotem totem : totems) {
					((UndyingFlower)totem).fatalHit(victim, this); // I hate doing this but oh well
				}
			}
		}
		if(victim.hasEffect(DeterminedEffect.class)) {
			victim.playSound(Sound.ITEM_TRIDENT_RIPTIDE_3, 2);
			ParticlesUtil.spawnParticle(Particle.HEART, victim.getLocation().add(0, 1, 0), 20, 0, .25, .5, .25);
			victim.glow(1000);
			victim.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, 1, false, false, false));
			victim.removeEffect(DeterminedEffect.class);
			return;
		}
		// Check AGAIN if this hit is fatal
		if(isFatal(victim, damage)) {
			// dead
			if(user.killEffect != null) {
				user.killEffect.play(victim.getLocation());
			}
			user.addExp(200);
			survivorDeath(victim, attack);
		}else {
			// still alive
			if(attack != AttackType.TRAP) {
				user.addExp(100);
				user.addBloodSilent(50);
				if(victim.getMatchData().survivorHealingProgress > 0) {
					user.getStatTracker().incrementStat(DeadlightStat.HEALS_INTERRUPTED);
				}
				victim.getMatchData().survivorHealingProgress = 0f;
				victim.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 2, 1));
			}
			victim.getPlayer().damage(damage);
			// protection hit
			if(victim.protectionHitCooldown.isDone() && attack != AttackType.TRAP) {
				int nearbyPlayers = 0;
				for(DLUser nearby : getSurvivors()) {
					if(nearby.getLocation().distance(victim.getLocation()) < 5 && !nearby.equals(victim)) {
						if(victim.getPlayer().getHealth() >= nearby.getPlayer().getHealth())
							nearbyPlayers++;
					}
				}
				if(nearbyPlayers > 0) {
					victim.addScoreEvent(new ScoreEvent("protection_hit", ScoreType.BLOOD));
					victim.getStatTracker().incrementStat(DeadlightStat.PROTECTION_HITS);
					Deadlight.getPerkManager().callEvent(victim, PerkEventType.PROTECTION_HIT);
					victim.protectionHitCooldown.start(5f);
				}
			}
			// are any objectives being progressed
			for(PortalFragment frag : gens) {
				boolean isFarAway = victim.getLocation().distance(frag.getPosition().getLocation(offset)) > 7;
				if(System.currentTimeMillis()-frag.lastProgressed < 5000 && isFarAway) {
					if(victim.getPlayerLoop().boldnessTicks > 20) {
						ScoreEvent score = new ScoreEvent("distraction", ScoreType.BLOOD);
						score.addMultiplier(user.getPlayerLoop().boldnessTicks);
						victim.addScoreEvent(score);
						victim.getPlayerLoop().boldnessTicks = 0;
						victim.getPlayerLoop().boldnessEventTriggered = true;
						victim.getStatTracker().incrementStat(DeadlightStat.DISTRACTIONS);
						break;
					}
				}
			}
		}
	}
	
	// Checks if the provided location is within the map's allocated size area
	public boolean isWithinMapBoundaries(Location location) {
		Location starting = this.getMapLocation();
		int[] size = MapLoader.getStructure(this.map.getName()).getSize();
		Location ending = this.getMapLocation().clone().add(size[0], size[1], size[2]);
		return (location.getX() > starting.getX() && location.getX() < ending.getX()) &&
				(location.getY() > starting.getY() && location.getY() < ending.getY()) &&
				(location.getZ() > starting.getZ() && location.getZ() < ending.getZ());
	}
	
	public void placeFlowerTotem(Location location, DLUser owner) {
		Items item = Items.valueOf(owner.equippedItem);
		FlowerTotem totem = null;
		switch(item) {
		case FLOWER_DAISY:
			totem = new HealingFlower();
			location.getBlock().setType(Material.POTTED_OXEYE_DAISY);
			break;
		case FLOWER_LILY:
			totem = new SpeedFlower();
			location.getBlock().setType(Material.POTTED_LILY_OF_THE_VALLEY);
			break;
		case FLOWER_TULIP:
			totem = new KnowledgeFlower();
			location.getBlock().setType(Material.POTTED_RED_TULIP);
			break;
		case FLOWER_WITHER:
			totem = new UndyingFlower();
			location.getBlock().setType(Material.POTTED_WITHER_ROSE);
			break;
		default:
			Deadlight.debug("Placed flower totem but there is no flower equipped???");
			return;
		}
		owner.send("flower_placed", item.name);
		totem.place(location, owner);
		flowers.add(totem);
	}
	
	public List<FlowerTotem> getFlowers() {
		return flowers;
	}
	
	public void spawnPlayerSoul(DLUser owner, Location deathLocation) {
		PlayerSoul soul = new PlayerSoul(this);
		soul.spawn(owner, deathLocation);
		playerSouls.add(soul);
	}
	
	public PlayerSoul getPlayerSoul(UUID uuid) {
		for(PlayerSoul soul : playerSouls) {
			if(soul.getOwnerUUID().equals(uuid)) {
				return soul;
			}
		}
		return null;
	}
	
	public PlayerSoul getPlayerSoul(String uuid) {
		return getPlayerSoul(UUID.fromString(uuid));
	}
	
	public PlayerSoul getPlayerSoul(Entity entity) {
		
		return getPlayerSoul(PlayerSoul.getOwnerUUID(entity));
	}
	
	public void removePlayerSoul(ArmorStand entity) {
		removePlayerSoul(getPlayerSoul(PlayerSoul.getOwnerUUID(entity)));
	}
	
	public void removePlayerSoul(PlayerSoul soul) {
		if(soul == null) {
			return;
		}
		soul.getEntity().remove();
		soul.getTask().cancel();
		soul.getBleedoutTimer().stop();
		playerSouls.remove(soul);
	}
	
	public void removePlayerSoul(int index) {
		removePlayerSoul(playerSouls.get(index));
	}
	
	public void survivorRevive(DLUser user) {
		// Revive the survivor
		if(user.getLobby() != null && user.getLobby().equals(this)) {
			user.getStatTracker().incrementStat(DeadlightStat.REVIVES_RECEIVED);
			user.getMatchData().revived = true;
			Location spawn = getFurthestSpawnFromKiller();
			user.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
			for(DLUser player : getPlayers()) {
				player.getPlayer().showPlayer(Deadlight.inst, user.getPlayer());
			}
			Player p = user.getPlayer();
			user.playSound(Sound.BLOCK_BELL_RESONATE, 2);
			survivorTeam.addEntry(user.getName());
			p.getInventory().clear();
			p.teleport(spawn);
			p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(settings.survivorHealth);
			p.setFlying(false);
			p.setAllowFlight(false);
			p.setGameMode(GameMode.SURVIVAL);
			user.getMatchData().reset();
			user.bloodBufferHasCleared = false;
			user.getPlayer().setHealth(4);
			Deadlight.getPerkManager().callEvent(user, PerkEventType.SURVIVOR_SPAWN);
			Deadlight.getPerkManager().callEvent(user, PerkEventType.SURVIVOR_REVIVE);
		}else {
			sendGlobal("player_soul_cant_revive");
		}
	}
	
	public void soulDestroyed(DLUser user) {
		if(user == null) {
			return;
		}
		sendGlobal("player_soul_destroyed", user.getName());
		if(isSpectating(user)) {
			user.clearBloodBuffer();
		}
	}
	
	public void soulExpire(DLUser user, PlayerSoul soul) {
		sendGlobal("player_soul_expired", user.getName());
		removePlayerSoul(soul.getEntity());
		if(isSpectating(user)) {
			getKiller().addScoreEvent(new ScoreEvent("soul_expired", ScoreType.BLOOD));
			user.clearBloodBuffer();
		}
	};
	
	public Location getFurthestSpawnFromKiller() {
		double highestDistance = 0.0D;
		MapPosition furthest = null;
		// Loop every single spawn point set in the map
		for(MapPosition spawn : this.getMap().getSpawns()) {
			// If there is no "furthest distance" set, we use the first position as our furthest location.
			if(furthest == null) {
				furthest = spawn;
				highestDistance = furthest.getLocation(offset).distance(getKiller().getLocation());
			}
			// Location.distance(Location) -> get distance between two locations
			double distance = spawn.getLocation(offset).distance(getKiller().getLocation());
			// check if it's bigger than our current highest distance
			if(distance > highestDistance) {
				// this is now our highest distance
				highestDistance = distance;
				furthest = spawn;
			}
		}
		// Return a Location object for our spawn point
		return furthest.getLocation(offset);
	}
	
	/** Called when a player dies, or disconnects and we count it as a death. */
	public void survivorDeath(DLUser user, AttackType attackType) {
		user.getStatTracker().incrementStat(DeadlightStat.DEATHS);
		getKiller().getStatTracker().incrementStat(DeadlightStat.KILLS);
		
		getKiller().kills++;
		user.deaths++;
		
		if(attackType.equals(AttackType.ABILITY) || attackType.equals(AttackType.TRAP)) {
			user.getStatTracker().incrementStat(DeadlightStat.ABILITY_KILLS);
		}
		if(user.isObsession()) {
			// Killing your obsession gets you a soul.
			getKiller().addScoreEvent(new ScoreEvent("obsession_killed", ScoreType.SOULS));
		}
		if(isKiller(user)) {
			end();
			return;
		}
		
		final ChatColor color = ChatColor.RED;
		final ChatColor playerColor = ChatColor.YELLOW;
		for(DLUser p : getPlayers()) {
			if(p.isKiller()) {
				p.playSound(Sound.ENTITY_WITHER_BREAK_BLOCK, .7);
			}else {
				p.playSound(Sound.ENTITY_GHAST_HURT, 1.2);
			}
			if(getKiller().killerItem != null && getKiller().killerItem.deathMessage != null)
				p.getPlayer().sendMessage(color + FormatUtil.color(getKiller().killerItem.deathMessage.replace("$1", playerColor + user.getName() + color)));
			else {
				p.send("match_survivor_death", user.getPlayer().getName());
			}
		}
		
		user.getItemInventory().removeItem(user.getEquippedItem());
		if(user.getItemInventory().getItem(user.equippedItem) <= 0) {
			user.equippedItem = null;
		}
		
		getKiller().addScoreEvent(new ScoreEvent("kill", ScoreType.BLOOD));
		if(user.isObsession()) {
			getKiller().addScoreEvent(new ScoreEvent("kill_obsession", ScoreType.BLOOD));
			user.addScoreEvent(new ScoreEvent("obsession_dead", ScoreType.BLOOD));
		}
		if(isEndGame()) {
			getKiller().addScoreEvent(new ScoreEvent("kill_end_game", ScoreType.BLOOD));
			getKiller().getStatTracker().incrementStat(DeadlightStat.END_GAME_KILLS);
		}
		if(timeLimit.getTimeLeft() < 20) {
			getKiller().addScoreEvent(new ScoreEvent("out_of_time", ScoreType.BLOOD));
		}
		
		// Player Souls
		List<UUID> lostSouls = getSoulsCarrying(user);
		for(UUID uuid : lostSouls) {
			soulDestroyed(PlayerManager.getUser(uuid));
		}
		
		getKiller().addExp(user.isObsession() ? 500 : 250);
		Deadlight.getPerkManager().callEvent(this, PerkEventType.SURVIVOR_DEATH, user);
		
		if(!settings.spectateAfterDeath) {
			user.quitLobby();
			removePlayer(user);
		} else {
			if(getSurvivorCount() != 0 || isAnySoulReviving()) {
				if(survivorTeam.hasEntry(user.getName())) {
					survivorTeam.removeEntry(user.getName());
				}
				spectatorTeam.addEntry(user.getName());
				user.spectate();
				user.sendTitle("you_died", "spectating");
				if(getSurvivorCount() >= 1) {
					if(user.getMatchData().allowRevive) {
						if(!user.getMatchData().revived) {
							spawnPlayerSoul(user, user.getLocation());
							user.send("player_soul_revivable");
						}else {
							user.send("player_soul_already_revived");
							user.clearBloodBuffer();
						}
					}else {
						user.send("player_soul_revive_disabled");
					}
				}
				updateGlowing();
			}
		}
		checkState();
	}
	
	public List<UUID> getSoulsCarrying(DLUser user) {
		ArrayList<UUID> soulIds = new ArrayList<>();
		PlayerInventory inv = user.getInventory();
		if(inv.contains(Material.PLAYER_HEAD)) {
			for(int i = 0; i < 8; i++) {
				ItemStack item = inv.getItem(i);
				if(item == null) continue;
				if(item.getType().equals(Material.PLAYER_HEAD)) {
					String uuid = PlayerSoul.getOwnerUUID(item);
					if(uuid != null) {
						soulIds.add(UUID.fromString(uuid));
					}
				}
			}
		}
		return soulIds;
	}
	
	public DLUser getSoulCarrier(DLUser user) {
		for(DLUser player : getSurvivors()) {
			List<UUID> souls = getSoulsCarrying(player);
			if(souls.contains(user.getUUID())) {
				return player;
			}
		}
		return null;
	}
	
	public boolean isBeingRevived(DLUser user) {
		for(MapObject object : this.getGenerators()) {
			PortalFragment frag = (PortalFragment)object;
			if(frag.isReviving()) {
				if(frag.reviving.equals(user.getUUID().toString())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isAnySoulReviving() {
		for(DLUser user : this.getPlayers()) {
			if(user.isSpectating()) {
				if(isBeingRevived(user)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** Called ONLY when a survivor escapes. */
	public void survivorEscape(DLUser user) {
		user.getStatTracker().incrementStat(DeadlightStat.ESCAPES);
		escapes++;
		user.escapes++;
		if(user.isObsession()) {
			user.addScoreEvent(new ScoreEvent("obsession_escaped", ScoreType.SOULS));
			user.getStatTracker().incrementStat(DeadlightStat.OBSESSION_ESCAPES);
		}
		if(!user.getMatchData().hasTakenDamage) {
			user.addScoreEvent(new ScoreEvent("untouchable", ScoreType.BLOOD));
			user.getStatTracker().incrementStat(DeadlightStat.UNTOUCHABLES);
		}
		if(timeLimit.getTimeLeft() < 20) {
			getKiller().addScoreEvent(new ScoreEvent("out_of_time", ScoreType.BLOOD));
		}
		user.addScoreEvent(new ScoreEvent(user.isObsession() ? "escaped_obsession" : "escaped", ScoreType.BLOOD));
		sendGlobal("match_survivor_escaped", user.getPlayer().getName());
		
		Deadlight.getPerkManager().callEvent(this, PerkEventType.SURVIVOR_ESCAPE);
		
		user.teleportToSpawn();
		user.addExp(500);
		removePlayer(user);
		user.playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5);
		user.clearBloodBuffer();
		checkState();
	}
	
	public void checkState() {
		if(getSurvivorCount() == 1) {
			for(DLUser player : players.values()) {
				player.playSound(Sound.BLOCK_BELL_USE, 0.4);
			}
			Deadlight.getPerkManager().callEvent(this, PerkEventType.LAST_SURVIVOR_ALIVE);
			if(!isEndGame()) {
				startEndGame();
				getKiller().getStatTracker().incrementStat(DeadlightStat.EARLY_END_GAME);
			}
		}else if(getSurvivorCount() == 0) {
			// don't end the game if there is a survivor being revived.
			if(isAnySoulReviving()) {
				return;
			}
			if(escapes == 0) {
				getKiller().addScoreEvent(new ScoreEvent("nobody_escapes_death", ScoreType.BLOOD));
				getKiller().getStatTracker().incrementStat(DeadlightStat.NO_ESCAPES);
			}
			if(gensDone == 0) {
				getKiller().addScoreEvent(new ScoreEvent("no_gens_done", ScoreType.BLOOD));
				getKiller().getStatTracker().incrementStat(DeadlightStat.NO_OBJECTIVES_COMPLETED);
			}
			getKiller().playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.6);
			getKiller().quitLobby();
			getKiller().getStatTracker().incrementStat(DeadlightStat.GAMES_PLAYED);
			if(getKiller().killerTypeInstance != null) {
				getKiller().getStatTracker().incrementStat(DeadlightStat.valueOf(getKiller().killerType.name() + "_GAMES_PLAYED"));
			}
			end();
		}
	}

	
	
	/** Returns all players actively in this lobby */
	public DLUser[] getPlayers() {
		ArrayList<DLUser> p = new ArrayList<>();
		for(DLUser user : players.values()) {
			if(user != null) {
				p.add(user);
			}
		}
		return p.toArray(new DLUser[0]);
	}
	
	/** Returns all survivors in this lobby */
	public List<DLUser> getSurvivors() {
		ArrayList<DLUser> p = new ArrayList<>();
		for(DLUser user : players.values()) {
			if(!user.isKiller() && !user.isSpectating()) {
				p.add(user);
			}
		}
		return p;
	}

	public void startEndGame() {
		if(state == GameState.END_GAME_COLLAPSE) {
			return;
		}
		setState(GameState.END_GAME_COLLAPSE);
		Deadlight.getPerkManager().callEvent(this, PerkEventType.END_GAME_COLLAPSE_START);
		for(PortalFragment genBlock : gens) {
			genBlock.finish(this, true);
		}
		for(SurvivorObjective portal : portals) {
			portal.setType(Material.REDSTONE_LAMP);
			portal.createHighlightShulker(this);
		}
		getKiller().send("killer_portals_open");
		for(DLUser user : players.values()) {
			if(!user.isKiller()) {
				user.send("survivor_portals_open");
			}
			user.playSound(Sound.ENTITY_WITHER_SPAWN, .5);
		}
		for(DLUser p : getSurvivors()) {
			p.addExp(250);
			p.addScoreEvent(new ScoreEvent("portals_opened", ScoreType.BLOOD));
			p.startTrack(DLTrack.COLLAPSE);
		}
	}
	
	/** Get the amount of generators required to start end game. */
	public int getRequiredGens() {
		return requiredGens;
	}
	
	public int getGeneratorCount() {
		return gens.length;
	}
	
	public void setGeneratorsBlocked(long msDuration) {
		BlockedState state = new BlockedState(msDuration);
		for(PortalFragment frag : gens) {
			frag.addState(state);
		}
	}
	
	public GameMap getMap() {
		return this.map;
	}
	
	public boolean equals(Object other) {
		if(other == this)return true;
		if(!(other instanceof Lobby)) return false;
		return ((Lobby)other).index == index;
	}
	
	public String getStateString() {
		String s = state.toString();
		s += " &8(&e";
		s += isEndGame() ? ""+portalsOpened : ""+gensDone;
		s += "&8/&e";
		s += isEndGame() ? "2" : ""+requiredGens;
		s += "&8)";
		return s;
	}
	
	public int getPortalsOpened() {
		return portalsOpened;
	}

	public MapObject[] getGenerators() {
		return gens;
	}
	
	public MapObject[] getPortals() {
		return portals;
	}

	public Location getMapLocation() {
		return offset;
	}

	/** Safely stop the current instance, assuming it broke somewhere in {@link Lobby#start()} */
	public StartingState forceStop() {
		
		if(map != null) {
			
		}
		
		if(players != null && !players.isEmpty()) {
			for(DLUser user : players.values()) {
				user.teleportToSpawn();
				user.setGameLobby(null);
			}
		}
		LobbyManager.getInstance().setLobby(index, null);
		return startingState;
	}
}
