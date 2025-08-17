package net.blixate.deadlight.challenges;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.blixate.deadlight.Deadlight;

public class ChallengeManager {
	
	protected JSONParser parser = new JSONParser();
	
	private Random random = new Random();
	
	public HashMap<ChallengeCategory, List<JsonChallenge>> challenges = new HashMap<>();
	
	public void loadChallenges() throws IOException, ParseException {
		JSONObject json;
		
		File file = new File(Deadlight.dataFolder, "challenges.json");
		FileInputStream stream = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
		json = (JSONObject) parser.parse(reader);
		
		// load all the challenges
		for(ChallengeCategory cat : ChallengeCategory.values()) {
			if(json.containsKey(cat.name().toLowerCase())) {
				loadChallenges(cat, (JSONArray)json.get(cat.name().toLowerCase()));
			}
		}
		Deadlight.inst.getLogger().info("Loaded " + challenges.get(ChallengeCategory.KILLER).size() + " killer challenges");
		Deadlight.inst.getLogger().info("Loaded " + challenges.get(ChallengeCategory.SURVIVOR).size() + " survivor challenges");
		reader.close();
		stream.close();
	}
	
	public void loadChallenges(ChallengeCategory category, JSONArray challengeArray) {
		if(challengeArray == null || challengeArray.isEmpty()) {
			return;
		}
		List<JsonChallenge> list = new ArrayList<>();
		int i = 0;
		String text, stat;
		long reqs;
		for(Object object: challengeArray) {
			try {
				JSONObject challenge = (JSONObject) object;
				text = (String) challenge.get("text");
				stat = (String) challenge.get("stat");
				reqs = (Long) challenge.get("reqs");
				list.add(new JsonChallenge(i++, text, stat, (int)reqs));
			}catch(Throwable t) {
				System.err.println("Failed to load " + category.name() + " challenges. (" + list.size() + " loaded before error)");
				Deadlight.error(t);
				break;
			}
		}
		challenges.put(category, list);
	}
	
	public AssignedChallenge getNewRandomAssignedChallenge(ChallengeCategory category) {
		JsonChallenge randomChallenge = getRandomChallenge(category);
		return new AssignedChallenge(category, randomChallenge);
	}
	
	public JsonChallenge getRandomChallenge(ChallengeCategory category) {
		return challenges.get(category).get(random.nextInt(challenges.get(category).size()));
	}
	
	public JsonChallenge getChallengeById(ChallengeCategory category, int id) {
		return challenges.get(category).get(id);
	}
	
}
