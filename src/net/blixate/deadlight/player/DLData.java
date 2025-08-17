package net.blixate.deadlight.player;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.challenges.AssignedChallenge;
import net.blixate.deadlight.challenges.ChallengeCategory;
import net.blixate.deadlight.fishing.FishingRods;
import net.blixate.deadlight.kit.cosmetics.BloodColor;
import net.blixate.deadlight.kit.cosmetics.KillEffect;
import net.blixate.deadlight.kit.cosmetics.KillerItem;
import net.blixate.deadlight.kit.items.Items;
import net.blixate.deadlight.kit.type.KillerTypes;
import net.blixate.deadlight.player.stats.StatTracker;
import net.blixate.deadlight.util.file.JSONRegistry;
import net.blixate.deadlight.util.time.TimeParser;

/** This handles all serializable data for any DLUser, can be used whether or not the user is offline. */
public class DLData {
	
	public static long DAILY_REWARD_TIME = 86400000L - TimeParser.hours(2);
	
	UUID uuid;
	
	public BigInteger blood = BigInteger.valueOf(0);
	public BigInteger souls = BigInteger.valueOf(0);
	
	protected DLInventory inventory;
	
	protected int level = 0;
	protected long exp = 0;
	protected int prestige = 0;
	// perks
	protected List<String> killerPerks = new ArrayList<>();
	protected List<String> survivorPerks = new ArrayList<>();
	protected Map<String, Integer> purchasedPerks = new HashMap<String, Integer>();
	
	public Map<String, Integer> offerings = new HashMap<String, Integer>();
	
	// cosmetic stuff
	public List<String> purchasedKillEffects = new ArrayList<>();
	public List<String> purchasedKillerItems = new ArrayList<>();
	public KillEffect killEffect = KillEffect.NONE;
	public KillerItem killerItem = KillerItem.DEFAULT;
	public KillerTypes killerType = KillerTypes.SCRUBBER;
	
	public List<String> purchasedBloodColors = new ArrayList<>();
	public BloodColor bloodColor = BloodColor.RED;
	
	// donors
	public String nickname = null;
	// survivor stuff
	public String equippedItem;
	// killer stuff
	public HashMap<String, KillerData> killerData = new HashMap<>();
	// tracking stats
	public int escapes 			= 0;
	public int kills 			= 0;
	public int deaths 			= 0;
	public long lastLogin		= 0;
	public int escapeStreak 	= 0;
	public long lastLoginReward = 0;
	public int loginReward 		= 0;
	
	public int killerWeight = 1; // Used for weight system.
	public int obsessionWeight = 5; // Used for weight system.
	
	// punishments
	public long muteDuration = -1;
	public String muteReason = null;
	public long banDuration = -1;
	public String banReason = null;
	public long queueBan = -1;
	
	public FishingRods fishingRodType = null;
	public long fishingRodUsesLeft = -1;
	
	public boolean hasReadTutorial = false;
	
	// Discord stuff
	private long discordUserId;
	
	protected StatTracker statTracker;
	
	public List<AssignedChallenge> dailyChallenges = new ArrayList<>();
	public long rerolls = 0;
	
	public long playtime = 0;
	
	public boolean musicEnabled = true;
	
	public DLData(Player p) {
		this(p.getUniqueId());
	}
	
	public DLData(UUID uuid) {
		this.uuid = uuid;
		this.inventory = new DLInventory();
		
		File f = getSaveFile();
		hasReadTutorial = f.exists();
		if(hasReadTutorial) {
			loadPlayerData();
		}
	}
	
	public DLData(File file) {
		loadPlayerData(file);
	}

	public void loadPlayerData() {
		loadPlayerData(getSaveFile());
	}
	
	@SuppressWarnings({ "unchecked" })
	public void loadPlayerData(File file) {
		// load from json
		JSONRegistry reg = new JSONRegistry(file);
		// currency
		blood = BigInteger.valueOf((long)reg.getObject("currency").get("blood"));
		souls = BigInteger.valueOf((long)reg.getObject("currency").get("souls"));
		// progress
		JSONObject progress = reg.getObject("progress");
		level = ((Long)progress.get("level")).intValue();
		exp = ((Long)progress.get("xp")).intValue();
		prestige = ((Long)progress.get("prestige")).intValue();
		// kit
		JSONObject kit = reg.getObject("kit");
		equippedItem = (String)kit.get("item");
		if(!Items.exists(equippedItem)) {
			equippedItem = null;
		}
		try {
			killerType = KillerTypes.valueOf((String)kit.getOrDefault("killerType", "SCRUBBER"));
		}catch(IllegalArgumentException | NullPointerException e) {
			killerType = KillerTypes.SCRUBBER;
		}
		JSONObject killers = reg.getObject("killers");
		// set defaults, make sure nothing is null
		for(KillerTypes killer : KillerTypes.values()) {
			if(!killerData.containsKey(killer.name())) {
				killerData.put(killer.name(), new KillerData(new JSONObject()));
			}
		}
		// override the data with our real data.
		for(Object killer : killers.keySet()) {
			String name = (String)killer;
			killerData.put(name, new KillerData((JSONObject)killers.get(name)));
		}
		JSONObject perks = (JSONObject)kit.get("perks");
		killerPerks = fromJSONArray((JSONArray)(perks.getOrDefault("killer", new JSONArray())));
		if(!killerPerks.isEmpty()) {
			for(int i = 0; i < survivorPerks.size(); i++) {
				if(Deadlight.getPerkManager().getKillerPerk(killerPerks.get(i)) == null) {
					survivorPerks.remove(i);
					i--;
				}
			}
		}
		survivorPerks = fromJSONArray((JSONArray)(perks.getOrDefault("survivor", new JSONArray())));
		if(!survivorPerks.isEmpty()) {
			for(int i = 0; i < survivorPerks.size(); i++) {
				if(Deadlight.getPerkManager().getSurvivorPerk(survivorPerks.get(i)) == null) {
					survivorPerks.remove(i);
					i--;
				}
			}
		}
		JSONObject purchased = (JSONObject)perks.get("purchased");
		purchasedPerks = new HashMap<>();
		for(Object key : purchased.keySet()) {
			String k = (String)key;
			purchasedPerks.put(k, ((Long)purchased.get(k)).intValue());
		}
		JSONObject items = (JSONObject)kit.get("inventory");
		inventory = new DLInventory();
		inventory.items = new HashMap<>();
		for(Object key : items.keySet()) {
			String k = (String)key;
			if(Items.exists(k)) {
				inventory.items.put(k, ((Long)items.get(k)).intValue());
			}
			
		}
		JSONObject offers = (JSONObject)kit.getOrDefault("offerings", new JSONObject());
		if(offers != null && !offers.isEmpty()) {
			offerings = new HashMap<>();
			for(Object key : offers.keySet()) {
				String k = (String)key;
				offerings.put(k, ((Long)offers.get(k)).intValue());
			}
		}
		
		// cosmetics
		JSONObject cosmetics = reg.getObject("cosmetics");
		purchasedKillEffects = fromJSONArray((JSONArray)(cosmetics.get("killEffects")));
		purchasedKillerItems = fromJSONArray((JSONArray)(cosmetics.get("items")));
		purchasedBloodColors = fromJSONArray((JSONArray)(cosmetics.get("bloodColors")));
		nickname = (String)cosmetics.get("nick");
		JSONObject selectedCosmetics = (JSONObject)cosmetics.get("selected");
		try {
			killEffect = KillEffect.valueOf((String)selectedCosmetics.getOrDefault("killEffect", "NONE"));
		}catch(Throwable t) {
			killEffect = KillEffect.NONE;
		}
		try {
			killerItem = KillerItem.valueOf((String)selectedCosmetics.getOrDefault("item", "DEFAULT"));
		}catch(Throwable t) {
			killerItem = KillerItem.DEFAULT;
		}
		try {
			bloodColor = BloodColor.valueOf((String)selectedCosmetics.getOrDefault("blood", "RED"));
		}catch(Throwable t) {
			bloodColor = BloodColor.RED;
		}
		// punishments
		JSONObject punishments = reg.getObject("punishments");
		if(punishments != null) {
			if(punishments.containsKey("mute")) {
				JSONObject mute = (JSONObject)punishments.get("mute");
				muteDuration = (long)mute.get("duration");
				if(mute.containsKey("reason")) muteReason = (String)mute.get("reason");
				else muteReason = null;
			}
			if(punishments.containsKey("queueBan")) {
				queueBan = (long)punishments.get("queueBan");
			}
		}
		
		// stats
		JSONObject stats = reg.getObject("stats");
		kills = ((Long)stats.getOrDefault("kills", 0L)).intValue();
		deaths = ((Long)stats.getOrDefault("deaths", 0L)).intValue();
		escapes = ((Long)stats.getOrDefault("escapes", 0L)).intValue();
		killerWeight = ((Long)stats.getOrDefault("weight", 1)).intValue();
		lastLogin = (long)stats.getOrDefault("lastLogin", 0L);
		lastLoginReward = (long)stats.getOrDefault("lastLoginReward", 0L);
		loginReward = ((Long)stats.getOrDefault("loginReward", 1L)).intValue();
		discordUserId = (long) stats.getOrDefault("discordId", 0L);
		playtime = (long)stats.getOrDefault("playtime", 0L);
		rerolls = (long)stats.getOrDefault("rerolls", 0L);
		musicEnabled = (boolean)stats.getOrDefault("musicEnabled", true);
		// fishing
		JSONObject fishing = reg.getObject("fishing");
		if(fishing != null) {
			if(fishing.containsKey("duration")) fishingRodUsesLeft = (Long)fishing.get("duration");
			if(fishing.containsKey("type")) fishingRodType = FishingRods.valueOf((String)fishing.get("type"));
		}
		
		JSONArray challenges = reg.getArray("challenges");
		if(challenges != null) {
			for(Object object : challenges) {
				JSONObject json = (JSONObject) object;
				if(object == null) {
					dailyChallenges.add(null);
				}else {
					AssignedChallenge challenge = new AssignedChallenge(json);
					dailyChallenges.add(challenge);
				}
			}
		}else {
			rollNewChallenges();
		}
	}
	
	public void savePlayerData() {
		savePlayerData(getSaveFile());
	}
	
	@SuppressWarnings("unchecked")
	public void savePlayerData(File f) {
		if(!hasReadTutorial) {
			return;
		}
		// Always save to json
		try {
			if(!f.exists()) {
				f.createNewFile();
			}
			JSONObject toSave = new JSONObject();
			JSONObject currency = new JSONObject();
			currency.put("blood", blood.longValue());
			currency.put("souls", souls.longValue());
			toSave.put("currency", currency);
			JSONObject progress = new JSONObject();
			progress.put("level", level);
			progress.put("xp", exp);
			progress.put("prestige", prestige);
			toSave.put("progress", progress);
			JSONObject kit = new JSONObject();
			JSONObject perks = new JSONObject();
			if(!killerPerks.isEmpty()) perks.put("killer", killerPerks);
			if(!survivorPerks.isEmpty()) perks.put("survivor", survivorPerks);
			perks.put("purchased", purchasedPerks);
			kit.put("inventory", inventory.items);
			kit.put("perks", perks);
			kit.put("item", equippedItem);
			kit.put("killerType", killerType.name());
			kit.put("offerings", offerings);
			toSave.put("kit", kit);
			JSONObject killers = new JSONObject();
			for(String key : killerData.keySet()) {
				JSONObject data = new JSONObject();
				data.put("level", killerData.get(key).level);
				data.put("xp", killerData.get(key).xp);
				data.put("joined", killerData.get(key).inDarkCommand);
				data.put("addons", killerData.get(key).getAddons());
				killers.put(key, data);
			}
			toSave.put("killers", killers);
			JSONObject stats = new JSONObject();
			stats.put("lastLogin", lastLogin);
			stats.put("lastLoginReward", lastLoginReward);
			stats.put("loginReward", loginReward);
			stats.put("discordId", discordUserId);
			if(kills != 0) stats.put("kills", kills);
			if(escapes != 0) stats.put("escapes", escapes);
			if(deaths != 0) stats.put("deaths", deaths);
			stats.put("weight", killerWeight);
			stats.put("streak", escapeStreak);
			stats.put("playtime", playtime);
			stats.put("rerolls", rerolls);
			stats.put("musicEnabled", musicEnabled);
			toSave.put("stats", stats);
			JSONObject cosmetics = new JSONObject();
			if(!purchasedKillEffects.isEmpty()) cosmetics.put("killEffects", purchasedKillEffects);
			if(!purchasedKillerItems.isEmpty()) cosmetics.put("items", purchasedKillerItems);
			if(!purchasedBloodColors.isEmpty()) cosmetics.put("bloodColors", purchasedBloodColors);
			JSONObject selected = new JSONObject();
			if(killEffect != KillEffect.NONE) selected.put("killEffect", killEffect.name());
			if(killerItem != KillerItem.DEFAULT) selected.put("item", killerItem.name());
			if(bloodColor != BloodColor.RED) selected.put("blood", bloodColor.name());
			cosmetics.put("selected", selected);
			if(nickname != null) cosmetics.put("nick", nickname);
			toSave.put("cosmetics", cosmetics);
			if(fishingRodType != null) {
				JSONObject fishing = new JSONObject();
				fishing.put("type", fishingRodType.name());
				fishing.put("duration", fishingRodUsesLeft);
				toSave.put("fishing", fishing);
			}
			JSONObject punishments = new JSONObject();
			if(muteDuration != -1) {
				JSONObject mute = new JSONObject();
				mute.put("duration", muteDuration);
				if(muteReason != null) mute.put("reason", muteReason);
				punishments.put("mute", mute);
			}
			if(queueBan != -1) punishments.put("queueBan", queueBan);
			if(!punishments.isEmpty()) {
				toSave.put("punishments", punishments);
			}
			
			if(!dailyChallenges.isEmpty()) {
				JSONArray dailys = new JSONArray();
				for(AssignedChallenge challenge : dailyChallenges) {
					if(challenge == null) {
						dailys.add(null);
					}else {
						dailys.add(challenge.getJson());
					}
					
				}
				toSave.put("challenges", dailys);
			}
			
			TreeMap<String, Object> treeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
			treeMap.putAll(toSave);
			
			Gson g = new GsonBuilder().create();
		 	String json = g.toJson(treeMap);
		 	
			FileWriter fw = new FileWriter(f);
			fw.write(json);
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public File getSaveFile() {
		return new File(Deadlight.dataFolder, "data/" + getUUIDString() + ".json");
	}
	
	/* For file storage purposes! */
	public String getUUIDString() {
		return uuid.toString().replaceAll("-", "");
	}
	
	private static List<String> fromJSONArray(JSONArray arr) {
		if(arr == null || arr.isEmpty()) {
			return new ArrayList<>();
		}
		List<String> list = new ArrayList<>();
		for(Object object : arr) {
			if(object instanceof String) {
				list.add((String)object);
			}
		}
		return list;
	}
	
	public String getName() {
		return Bukkit.getOfflinePlayer(uuid).getName();
	}
	
	public long getBloodCap() {
		return 250000 + (prestige * 100000);
	}
	
	public long getBlood() {
		return this.blood.longValue();
	}
	
	public long getSouls() {
		return this.souls.longValue();
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public long getExp() {
		return this.exp;
	}
	
	public int getPrestige() {
		return prestige;
	}
	
	public int getLevelUpExp() {
		return (level + 1) * 75 * (prestige + 1);
	}
	
	/** Returns Queue Ban time in minutes */
	public int getQueueBanTime() {
		return Math.round(queueBan / 1000 / 60 / 60);
	}
	
	public void queueUnban() {
		queueBan = -1;
	}
	
	public float getLevelProgress() {
		// adding this just incase to stop errors ig
		if(getExp() > getLevelUpExp()) return 1f;
		return (float)((float)getExp() / (float)getLevelUpExp());
	}
	
	public String getEquippedItem() {
		return equippedItem;
	}

	public void setEquippedItem(String id) {
		this.equippedItem = id;
	}
	
	/** Returns the perk tier this DLUser has purchased */
	public int getPerkTier(String perk) {
		return hasPurchasedPerk(perk) ? purchasedPerks.get(perk) : 0;
	}
	
	public Set<String> getPurchasedPerks() {
		return purchasedPerks.keySet();
	}
	
	public List<String> getKillerPerks() {
		return killerPerks;
	}
	
	public List<String> getSurvivorPerks() {
		return survivorPerks;
	}
	
	public boolean hasPurchasedPerk(String perk) {
		return purchasedPerks.containsKey(perk);
	}
	
	public boolean hasKillerPerk(String perk) {
		if(killerPerks != null)
			return killerPerks.contains(perk);
		return false;
	}
	
	public boolean hasSurvivorPerk(String perk) {
		if(survivorPerks != null)
			return survivorPerks.contains(perk);
		return false;
	}
	
	public void setPerkTier(String perk, int tier) {
		purchasedPerks.put(perk, tier);
	}
	
	public void removePerk(String name, Alignment align) {
		if(align == Alignment.KILLER) {
			killerPerks.remove(name);
		}else if(align == Alignment.SURVIVOR) {
			survivorPerks.remove(name);
		}
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public long getDiscordUserID() {
		return discordUserId;
	}
	
	public void setDiscordUserID(long id) {
		discordUserId = id;
	}
	
	public StatTracker getStatTracker() {
		return statTracker;
	}
	
	public long getPlaytime() {
		return playtime;
	}
	
	public long getTimeUntilDailyReset() {
		return DAILY_REWARD_TIME - (System.currentTimeMillis() - lastLoginReward);
	}
	
	public void rollNewChallenges() {
		this.dailyChallenges.clear();
		this.dailyChallenges.add(Deadlight.getChallengeManager().getNewRandomAssignedChallenge(ChallengeCategory.KILLER));
		this.dailyChallenges.add(Deadlight.getChallengeManager().getNewRandomAssignedChallenge(ChallengeCategory.SURVIVOR));
		this.dailyChallenges.add(Deadlight.getChallengeManager().getNewRandomAssignedChallenge(Deadlight.RNG.nextBoolean() ? ChallengeCategory.KILLER : ChallengeCategory.SURVIVOR));
	}
	
	public void rerollChallenge(int index) {
		if(index >= dailyChallenges.size()) {
			return;
		}
		this.dailyChallenges.set(index, Deadlight.getChallengeManager().getNewRandomAssignedChallenge(Deadlight.RNG.nextBoolean() ? ChallengeCategory.KILLER : ChallengeCategory.SURVIVOR));
	}
	
	public boolean hasCompletedAllDailyChallenges() {
		for(AssignedChallenge chal : dailyChallenges) {
			if(chal != null) {
				return false;
			}
		}
		return true;
	}
}
