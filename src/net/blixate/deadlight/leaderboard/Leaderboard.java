package net.blixate.deadlight.leaderboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.FormatUtil;

/** Helps with caching the leaderboard results */
public class Leaderboard {
	
	public static final int TOP_RESULTS = 10;
	
	JSONParser parser = new JSONParser();
	
	File storage;
	ArrayList<String> eligible;
	
	public List<LeaderboardMember> cache;
	public long lastSort;
	
	public Leaderboard(File storageFile) {
		if(!storageFile.exists()) {
			try {
				storageFile.createNewFile();
				PrintWriter pw = new PrintWriter(storageFile, "UTF-8");
				pw.print("{}");
				pw.flush();
				pw.close();
			}catch(Throwable t) {
				t.printStackTrace();
			}
		}
		this.storage = storageFile;
		this.eligible = new ArrayList<>();
	}
	
	public ArrayList<String> getEligiblePlayers() {
		return eligible;
	}
	
	public boolean isEligible(String uuid) {
		return eligible.contains(uuid);
	}
	
	public void setEligible(String uuid) {
		if(!isEligible(uuid)) {
			eligible.add(uuid);
		}
	}
	
	public void load() throws Exception {
		JSONObject json = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(storage), "UTF-8"));
		JSONArray players = (JSONArray) json.get("players");
		for(int i = 0; i < players.size(); i++) {
			eligible.add((String)players.get(i));
		}
	}
	
	public void save() throws Exception {
		TreeMap<String, Object> treeMap = new TreeMap<>();
		treeMap.put("players", eligible);
		
		Gson g = new GsonBuilder().create();
	 	String prettyJsonString = g.toJson(treeMap);
	 	
		java.io.FileWriter fw = new java.io.FileWriter(storage);
		fw.write(prettyJsonString);
		fw.flush();
		fw.close();
	}
	
	public void findEligible() {
		File file;
		FileInputStream stream;
		InputStreamReader reader;
		JSONObject playerData;
		for(String uuid : PlayerManager.getPlayerFolder().list()) {
			// Do not include bedrock players
			if(uuid.startsWith("0000000000000000000")) {
				continue;
			}
			uuid = uuid.substring(0, 32);
			try {
				file = new File(PlayerManager.getPlayerFolder(), uuid + ".json");
				if(!file.exists()) {
					continue;
				}
				stream = new FileInputStream(file);
				reader = new InputStreamReader(stream, "UTF-8");
				playerData = (JSONObject) parser.parse(reader);
				JSONObject progress = (JSONObject) playerData.get("progress");
				if(progress == null) {
					continue;
				}
				int prestige = ((Long)progress.get("prestige")).intValue();
				if(prestige >= 1) {
					setEligible(uuid);
				}
				stream.close();
			}catch(Throwable t) {
				continue;
			}
		}
	}
	
	public List<LeaderboardMember> checkCache() {
		if(cache == null || (System.currentTimeMillis() - lastSort) > 60000 * 5) {
			lastSort = System.currentTimeMillis();
			try {
				cache = sort();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return cache;
	}
	
	public List<LeaderboardMember> sort() throws Exception {
		final LinkedList<LeaderboardMember> fullPlayerList = new LinkedList<>();
		for(String uuid : Deadlight.leaderboard.getEligiblePlayers()) {
			// Do not include bedrock players #BedrockLivesDontMatter
			if(uuid.startsWith("0000000000000000000")) {
				continue;
			}
			try {
				File file = new File(PlayerManager.getPlayerFolder(), uuid + ".json");
				if(!file.exists()) {
					continue;
				}
				JSONObject json = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				JSONObject progress = (JSONObject) json.get("progress");
				if(progress == null) {
					continue;
				}
				LeaderboardMember user = new LeaderboardMember(uuid,((Long)progress.get("level")).intValue(),((Long)progress.get("prestige")).intValue());
				if(user.prestige <= 0) {
					continue;
				}
				fullPlayerList.add(user);
			}catch(Throwable t) {
				continue;
			}
		}
		fullPlayerList.sort(new LevelComparator());
		List<LeaderboardMember> top5Cache = new ArrayList<>();
		for(int i = 0; i < Math.min(fullPlayerList.size(), TOP_RESULTS); i++) {
			top5Cache.add(fullPlayerList.get(i));
		}
		return top5Cache;
	}
	
	public static class LeaderboardMember {
		public String name;
		public int level;
		public int prestige;
		
		public LeaderboardMember(final String name, final int level, final int prestige) {
			this.name = name; // just keep the UUID
			this.level = level;
			this.prestige = prestige;
		}
		
		public UUID getUUID() {
			return FormatUtil.getUUIDFromString(name);
		}
		
		public String getName() {
			OfflinePlayer player = Bukkit.getOfflinePlayer(getUUID());
			if(player == null || player.getName() == null) {
				return "Unknown Player";
			}
			return player.getName();
		}
	}
}
