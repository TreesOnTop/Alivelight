package net.blixate.deadlight;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;

import com.comphenix.protocol.ProtocolLibrary;

import net.blixate.config.ConfigArray;
import net.blixate.config.ConfigFile;
import net.blixate.config.ConfigProperty;
import net.blixate.deadlight.advertising.MinehutRankManager;
import net.blixate.deadlight.challenges.ChallengeManager;
import net.blixate.deadlight.commands.CurrencyCommand;
import net.blixate.deadlight.commands.DailyChallengesCommand;
import net.blixate.deadlight.commands.DeadlightCommand;
import net.blixate.deadlight.commands.FishingCommand;
import net.blixate.deadlight.commands.InfoCommands;
import net.blixate.deadlight.commands.KitCommand;
import net.blixate.deadlight.commands.LeaderboardCommand;
import net.blixate.deadlight.commands.LobbiesCommand;
import net.blixate.deadlight.commands.MusicCommand;
import net.blixate.deadlight.commands.PrestigeCommand;
import net.blixate.deadlight.commands.QueueCommand;
import net.blixate.deadlight.commands.ReportCommand;
import net.blixate.deadlight.commands.SpawnCommand;
import net.blixate.deadlight.commands.UnimplementedCommand;
import net.blixate.deadlight.commands.VerifyCommand;
import net.blixate.deadlight.commands.admin.MapCommand;
import net.blixate.deadlight.commands.admin.RankPurchaseCommand;
import net.blixate.deadlight.commands.donor.NickCommand;
import net.blixate.deadlight.commands.staff.ClearChatCommand;
import net.blixate.deadlight.commands.staff.MuteChatCommand;
import net.blixate.deadlight.commands.staff.MuteCommand;
import net.blixate.deadlight.commands.staff.QueueBanCommand;
import net.blixate.deadlight.commands.staff.QueueUnbanCommand;
import net.blixate.deadlight.commands.staff.StaffCommand;
import net.blixate.deadlight.commands.staff.TempmuteCommand;
import net.blixate.deadlight.commands.staff.UnmuteCommand;
import net.blixate.deadlight.discord.DiscordManager;
import net.blixate.deadlight.fishing.FishingDropTable;
import net.blixate.deadlight.kit.offerings.OfferingManager;
import net.blixate.deadlight.kit.perks.PerkManager;
import net.blixate.deadlight.leaderboard.Leaderboard;
import net.blixate.deadlight.lobby.GameListener;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.LobbyManager;
import net.blixate.deadlight.maps.MapLoader;
import net.blixate.deadlight.npc.CitizensListener;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.queue.QueueLobby;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.nms.EntityMetadataPacketListener;
import net.blixate.deadlight.util.random.PostMatchDropTable;
import net.blixate.deadlight.utils.holograms.HoloManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

/**
 * <h1>Main plugin class for this plugin</h1>
 * <p>This contains some utility functions, but also handles most API calls for starting/stopping
 * this plugin, as well as loading configuration data and global plugin storage.</p>
 * 
 * @see JavaPlugin
 */ 
public class Deadlight extends JavaPlugin {
	public static String DEFAULT_WORLD = "world";
	public static Location spawn;
	public static File dataFolder;
	public static String[] folders = {
		"data",
		"maps"
	};
	
	public static String[] resources = {
		"config.yml", "spawn.yml",
		"messages.bcfg",
		"killer_perk_registry.bcfg",
		"survivor_perk_registry.bcfg",
		"drop_tables.json",
		"score_events.yml", "abilities.yml",
		"challenges.json"
		
	};
	public static boolean debug = false;
	public static boolean locked = false;
	public static boolean muteChat = false;
	
	public static Random RNG = new Random(System.currentTimeMillis());
	
	/** Global instance of the {@link Deadlight} plugin. */
	public static Deadlight inst;
	
	// Configuration files loaded.
	public static ConfigFile messages;
	public static FishingDropTable fishingDrops;
	public static PostMatchDropTable postMatchDrops;
	public static FileConfiguration scoreEvents;
	public static FileConfiguration abilities;
	
	public static QueueLobby queue;
	public static ServerLoop serverLoop;
	
	public static PerkManager perkManager;
	public static LobbyManager lobbyManager;
	public static ChallengeManager challengeManager;
	public static MinehutRankManager minehutRankManager;
	
	public static String[] prestigeColors;
	
	public static Leaderboard leaderboard;
	
	public static String setMap = null;
	
	public Plugin vaultPlugin;
	public Plugin citizensPlugin;
	public Plugin noteBlockApi;
	public Plugin worldEditPlugin;
	public Plugin deadlightBot;
	public Chat chat;
	public Permission permission;
	
	/* Create singleton */
	public void onLoad() {
		inst = this;
	}
	
	public void onEnable() {
		this.getLogger().log(Level.INFO, "Starting Deadlight...");
		perkManager = new PerkManager();
		lobbyManager = new LobbyManager((int)Math.round(Bukkit.getServer().getMaxPlayers() / 5) * 2);
		challengeManager = new ChallengeManager();
		minehutRankManager = new MinehutRankManager();
		queue = new QueueLobby();
		/* Hook with APIs */
		hookApis();
		dataFolder = this.getDataFolder();
		/* Register commands */
		registerCommand("spawn", new SpawnCommand());
		registerCommand("map", new MapCommand());
		registerCommand("queue", new QueueCommand());
		registerCommand("deadlight", new DeadlightCommand());
		registerCommand("blood", new CurrencyCommand());
		registerCommand("souls", new CurrencyCommand());
		registerCommand("level", new CurrencyCommand());
		registerCommand("prestige", new PrestigeCommand());
		// info commands
		registerCommand("discord", new InfoCommands());
		registerCommand("admsg", new InfoCommands());
		registerCommand("rules", new InfoCommands());
		registerCommand("ping", new InfoCommands());
		registerCommand("stats", new InfoCommands());
		registerCommand("tutorial", new InfoCommands());
		registerCommand("guide", new InfoCommands());
		registerCommand("playtime", new InfoCommands());
		// end of info commands
		//registerCommand("bloodtransfer", new BloodTransferCommand());
		registerCommand("kit", new KitCommand());
		registerCommand("nickname", new NickCommand());
		registerCommand("report", new ReportCommand());
		registerCommand("fishing", new FishingCommand());
		registerCommand("leaderboard", new LeaderboardCommand());
		registerCommand("lobbies", new LobbiesCommand());
		if(deadlightBot != null) {
			registerCommand("verify", new VerifyCommand());
		}else {
			registerCommand("verify", new UnimplementedCommand());
		}
		//
		// Staff commands
		registerCommand("staffchat", new StaffCommand());
		registerCommand("mute", new MuteCommand());
		registerCommand("unmute", new UnmuteCommand());
		registerCommand("tempmute", new TempmuteCommand());
		registerCommand("mutechat", new MuteChatCommand());
		registerCommand("clearchat", new ClearChatCommand());
		registerCommand("queueban", new QueueBanCommand());
		registerCommand("queueunban", new QueueUnbanCommand());
		//registerCommand("image", new ImageCommand());
		registerCommand("rankpurchase", new RankPurchaseCommand());
		registerCommand("dailychallenges", new DailyChallengesCommand());
		//registerCommand("rank", new RankCommand());
		registerCommand("music", new MusicCommand());
		/* Register events */
		Bukkit.getPluginManager().registerEvents(new DeadlightEvents(), this);
		Bukkit.getPluginManager().registerEvents(new GameListener(), Deadlight.inst);
		if(citizensPlugin != null) {
			Bukkit.getPluginManager().registerEvents(new CitizensListener(), this);
		}
		//Bukkit.getPluginManager().registerEvents(new MapEventListener(), Deadlight.inst);
		//MapManager.init();
		for(String folder : folders) {
			File dir = new File(dataFolder, folder);
			if(!dir.exists()) {
				dir.mkdir();
			}
		}
		reload();
		/* Code to make this plugin reload-safe */
		try {
			if(Bukkit.getOnlinePlayers().size() != 0) {
				reloadPlayers();
			}
		}catch(Throwable t) {
			t.printStackTrace();
		}
		
		try {
			DiscordManager.createBot();
		}catch(Throwable t) {
			t.printStackTrace();
		}
		
		ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMetadataPacketListener(this));
		
		Bukkit.getScheduler().runTaskLater(Deadlight.inst, () -> {
			minehutRankManager.loadRanks();
		}, 1);
	}
	
	public void registerCommand(String name, CommandExecutor executor) {
		PluginCommand cmd = this.getCommand(name);
		if(cmd == null) 
			return;
		cmd.setExecutor(executor);
	}
	
	public void onDisable() {
		/* If any users are online when we disable this plugin, save their data. */
		for(Lobby lobby : lobbyManager.getLobbies()) {
			if(lobby != null)
				lobby.end();	
		}
		this.getLogger().log(Level.FINE, "Shutting down...");
		try {
			DLUser[] players = PlayerManager.getPlayers().toArray(new DLUser[0]);
			for(int i = 0; i < players.length; i++) {
				players[0].delete();
			}
			this.getLogger().log(Level.INFO, "Saved player data!");
			DiscordManager.shutdownBot();
			DiscordManager.save();
			PlayerManager.getPlayers().clear();
			HoloManager.removeAllHolograms();
			HoloManager.savePermanentHolograms();
			//MapManager.uninit();
			OfferingManager.saveOfferings();
			leaderboard.save();
		} catch (Exception e) {
			this.getLogger().log(Level.WARNING, "An error occurred while saving data for Deadlight.");
			e.printStackTrace();
		}
	}
	
	public static FileConfiguration cfg() {
		return inst.getConfig();
	}
	
	public static ChatColor getChatColor() {
		return ChatColor.WHITE;
	}
	
	/** Save all registered players save data. */
	public void savePlayers() {
		for(DLUser user : PlayerManager.getPlayers()) {
			user.savePlayerData();
		}
	}
	
	public static String getVersion() {
		return inst.getDescription().getVersion();
	}
	
	private Plugin hookPlugin(String name) {
		if(Bukkit.getPluginManager().isPluginEnabled(name)) {
			this.getLogger().log(Level.INFO, "Hooked with " + name);
			return Bukkit.getPluginManager().getPlugin(name);
		}else{
			this.getLogger().log(Level.INFO, "Couldn't hook " + name);
		}
		return null;
	}
	
	public void hookApis() {
		vaultPlugin = hookPlugin("Vault");
		if(vaultPlugin != null) {
			chat = Bukkit.getServicesManager().load(Chat.class);
			permission = Bukkit.getServicesManager().load(Permission.class);
		}
		citizensPlugin = hookPlugin("Citizens");
		noteBlockApi = hookPlugin("NoteBlockAPI");
		deadlightBot = hookPlugin("Deadlight-Bot");
		worldEditPlugin = hookPlugin("WorldEdit");
	}

	public void reloadPlayers() {
		Collection<DLUser> list = PlayerManager.getPlayers();
		for(DLUser user : list) {
			user.delete();
		}
		PlayerManager.getPlayers().clear();
		for(Player p : Bukkit.getOnlinePlayers()) {
			PlayerManager.createUser(p);
		}
	}
	
	public void saveRequiredResources() {
		for(String n : resources) {
			File f = new File(dataFolder, n);
			if(f.exists()) {
				continue;
			}
			this.saveResource(n, false);
		}
	}
	
	public void reload() {
		try {
			this.saveRequiredResources();
			this.reloadConfig();
			
			try {
				OfferingManager.loadOfferings();
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			
			HoloManager.removeAllHolograms();
			HoloManager.loadPermanentHolograms();
			
			if(leaderboard != null) {
				leaderboard.save();
			}
			
			DEFAULT_WORLD = this.getConfig().getString("global.world");
			
			File dropTable = new File(dataFolder, "drop_tables.json");
			
			fishingDrops = new FishingDropTable(dropTable);
			postMatchDrops = new PostMatchDropTable(dropTable);
			
			messages = readConfigFile("messages.bcfg");
			
			scoreEvents = YamlConfiguration.loadConfiguration(new File(dataFolder, "score_events.yml"));
			abilities = YamlConfiguration.loadConfiguration(new File(dataFolder, "abilities.yml"));
			
			prestigeColors = MsgConfig.getMessageList("prestige_colors");
			
			// Manager registry
			perkManager.createRegistry();
			
			/* Load server loop */
			if(serverLoop != null && serverLoop.task != null && !serverLoop.task.isCancelled()) {
				serverLoop.task.cancel();
			}
			serverLoop = new ServerLoop();
			serverLoop.task = Bukkit.getScheduler().runTaskTimer(this, serverLoop, 0L, 20L);
			
			// Could've just used EssentialsSpawn but fuck it
			spawn = SpawnCommand.load(new File(dataFolder, "spawn.yml"));
			
			// Load the leaderboard cache
			File file = new File(dataFolder, "leaderboard.json");
			leaderboard = new Leaderboard(file);
			leaderboard.load();
			
			DiscordManager.load();
			
			challengeManager.loadChallenges();
			
			MapLoader.loadMaps();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getFormattedPlayerName(DLUser p) {
		if(chat != null) {
			String prefix = chat.getPlayerPrefix(p.getPlayer());
			String name = (p.nickname == null ? p.getName() : p.getFormattedNickName());
			String suffix = chat.getPlayerSuffix(p.getPlayer());
			return FormatUtil.color(prefix) + name + FormatUtil.color(suffix);
		}
		return p.getName(); // Return if Vault doesn't exist.
	}
	
	private static ConfigFile readConfigFile(String name) {
		File f = new File(dataFolder, name);
		if(!f.exists()) { // Resources are expected to be saved way before this.
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ConfigFile cfg = new ConfigFile(f);
		try {
			cfg.read();
		}catch(IOException e) {
			e.printStackTrace();
		}
		return cfg;
	}
	
	public static void setNewSpawn(Location l) throws IOException {
		SpawnCommand.save(l, new File(dataFolder, "spawn.yml"));
		spawn = l;
	}
	
	public static String msg(String address) {
		ConfigProperty msg = messages.property(MsgConfig.DEFAULT_LANGUAGE, address);
		if(msg == null) {
			System.out.println("Unknown message address '"+address+"'");
			return FormatUtil.color("&4&lERROR&c: Unknown message '" + address + "'");
		}
		if(msg.isArray()) {
			ArrayList<String> strings = new ArrayList<>();
			ConfigArray array = msg.asArray();
			for(ConfigProperty prop : array.values()) {
				strings.add(prop.getAsString());
			}
			return FormatUtil.color(String.join("\n", strings));
		}
		return FormatUtil.color(msg.getAsString());
	}
	
	public static String msg(String address, String[] arguments) {
		String message = Deadlight.msg(address);
		for(int i=0;i<arguments.length;i++) {
			String ix = ""+(i+1);
			if(message.contains("&$"+ix)) {
				message = message.replace("&$"+ix, FormatUtil.color(arguments[i]));
			}else if(message.contains("%$"+ix)) {
				message = message.replace("%$"+ix, FormatUtil.colorOnly(arguments[i]));
			}else {
				message = message.replace("$"+ix, arguments[i]);
			}
		}
		return message;
	}
	
	public static String msg(DLUser user, String address, String[] arguments) {
		return msg(address, arguments);
	}
	
	public static String msg(DLUser user, String address) {
		return msg(address);
	}
	
	@SuppressWarnings("deprecation")
	public static UUID getUUID(String name) {
		Player p = Bukkit.getPlayerExact(name);
		if(p == null) {
			OfflinePlayer op = Bukkit.getOfflinePlayer(name);
			if(op != null) {
				return op.getUniqueId();
			}
		}
		return p.getUniqueId();
	}
	
	public static void debug(Object...objs) {
		if(!debug)
			return;
		String s = "";
		for(Object o : objs) {
			s += (String.valueOf(o));
		}
		Deadlight.inst.getLogger().info(s);
	}
	
	public static void error(Throwable e) {
		e.printStackTrace();
	}
	
	public static void staffMsg(String msg) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.hasPermission("deadlight.staff")) {
				p.sendMessage(msg);
			}
		}
		Bukkit.getConsoleSender().sendMessage(msg);
	}
	
	public static PerkManager getPerkManager() {
		return perkManager;
	}
	
	public static LobbyManager getLobbyManager() {
		return lobbyManager;
	}
	
	public static ChallengeManager getChallengeManager() {
		return challengeManager;
	}
	
	public static World getWorld() {
		return Bukkit.getWorld(DEFAULT_WORLD);
	}

	public static Leaderboard getLeaderboard() {
		return leaderboard;
	}

	public static long sizeOfDirectory(File folder) {
		long length = 0;
	   
		// listFiles() is used to list the
		// contents of the given folder
		File[] files = folder.listFiles();
		
		int count = files.length;
		
		// loop for traversing the directory
		for (int i = 0; i < count; i++) {
			if (files[i].isFile()) {
				length += files[i].length();
			}
			else {
				length += sizeOfDirectory(files[i]);
			}
		}
		return length;
	}

	public static MinehutRankManager getMinehutRankManager() {
		return minehutRankManager;
	}
	 
}
