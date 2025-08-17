package net.blixate.deadlight.player;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.blixate.deadlight.kit.offerings.Offering;
import net.blixate.deadlight.kit.offerings.OfferingManager;

/** Contains data associated with a Killer Type. */
public class KillerData {
	public long level;
	public long xp;
	
	public long kills;
	public long obliterations;
	
	public List<String> selectedAugments;
	public List<Integer> documents;
	public boolean inDarkCommand;
	
	@SuppressWarnings("unchecked")
	public KillerData(JSONObject data) {
		this.level = (long) data.getOrDefault("level", 1L);
		this.xp = (long) data.getOrDefault("xp", 0L);
		this.inDarkCommand = (boolean) data.getOrDefault("joined", inDarkCommand);
		JSONArray addons = (JSONArray) data.getOrDefault("addons", new JSONArray());
		this.selectedAugments = new ArrayList<String>();
		for(int i = 0; i < addons.size(); i++) {
			this.selectedAugments.add((String) addons.get(i));
		}
	}
	
	public List<String> getAddons() {
		return selectedAugments;
	}
	
	public long getLevelUpExp() {
		return ((1 + level/4) * level * 50);
	}
	
	public boolean addExp(int amount) {
		int boost = 1 + OfferingManager.isOfferingActive(Offering.EXTERMINATOR_CREST);
		
		xp += amount * boost;
		boolean hasLevelChanged = false;
		while(xp > getLevelUpExp()) {
			xp -= getLevelUpExp();
			level ++;
			hasLevelChanged = true;
		}
		return hasLevelChanged;
	}
}
