package net.blixate.deadlight.advertising;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.util.WebUtils;

public class MinehutRankManager {
	
	public MinehutRank[] ranks;
	
	public MinehutRank getRank(String name) {
		for(MinehutRank rank : ranks) {
			if(rank.id.equals(name)) {
				return rank;
			}
		}
		return null;
	}
	
	public MinehutRank[] getRanks() {
		return ranks;
	}
	
	public void loadRanks() {
		String content = WebUtils.getContent("https://api.minehut.com/network/ranks");
		JSONArray rankList = null;
		try {
			rankList = (JSONArray)new JSONParser().parse(content);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(rankList == null) {
			Deadlight.error(new RuntimeException("Failed to get rank list!"));
			return;
		}
		this.ranks = new MinehutRank[rankList.size()];
		for(Object rank : rankList) {
			JSONObject object = (JSONObject) rank;
			MinehutRank mhrank = new MinehutRank(object);
			// totally safe code, trust
			this.ranks[(int) mhrank.ordinal] = mhrank;
		}
	}
	
}
