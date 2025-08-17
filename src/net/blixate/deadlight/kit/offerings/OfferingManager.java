package net.blixate.deadlight.kit.offerings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLUser;

public class OfferingManager {
	
	public static class ActiveOffering {
		public String name;
		public String ownerUuid;
		public long expiresAt;
		
		public ActiveOffering(JSONObject object) {
			this.name = (String) object.get("name");
			this.ownerUuid = (String) object.get("owner");
			if(object.containsKey("expires")) {
				this.expiresAt = (Long) object.get("expires");
			}else {
				this.expiresAt = -1;
			}
		}
		
		public ActiveOffering(String name, String owner, long expires) {
			this.name = name;
			this.ownerUuid = owner;
			this.expiresAt = expires;
		}
		
		@SuppressWarnings("unchecked")
		public JSONObject getJSONObject() {
			JSONObject object = new JSONObject();
			object.put("name", name);
			object.put("owner", ownerUuid);
			object.put("expires", expiresAt);
			return object;
		}
		
		public boolean isActive() {
			return expiresAt > System.currentTimeMillis();
		}
	}
	
	public static final int MAX_ACTIVE_OFFERINGS = 10;
	
	static ArrayList<ActiveOffering> jsonOfferings = new ArrayList<>();
	
	static File offeringsFile = new File(Deadlight.dataFolder, "offerings.json");
	
	public static void loadOfferings() throws UnsupportedEncodingException, FileNotFoundException, IOException, ParseException {
		if(!offeringsFile.exists()) {
			offeringsFile.createNewFile();
		}
		JSONParser parser = new JSONParser();
		JSONArray array = (JSONArray) parser.parse(new InputStreamReader(new FileInputStream(offeringsFile), "UTF-8"));
		for(int i = 0; i < array.size(); i++) {
			JSONObject object = (JSONObject) array.get(i);
			jsonOfferings.add(new ActiveOffering(object));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void saveOfferings() throws IOException {
		JSONArray toSave = new JSONArray();
		
		for(ActiveOffering offering : jsonOfferings) {
			if(!offering.isActive()) {
				continue;
			}
			toSave.add(offering.getJSONObject());
		}
	
		java.io.FileWriter fw = new java.io.FileWriter(offeringsFile);
		fw.write(toSave.toJSONString());
		fw.flush();
		fw.close();
		
	}
	
	public static boolean isCountMax(Offering offering) {
		return (isOfferingActive(offering) >= offering.limit);
	}
	
	public static void addOffering(Offering offering, DLUser owner, long duration) {
		jsonOfferings.add(new ActiveOffering(offering.name(), owner.getUUIDString(), System.currentTimeMillis() + duration));
	}
	
	public static int isOfferingActive(Offering check) {
		int count = 0;
		for(ActiveOffering offering : jsonOfferings) {
			if(offering.name.equals(check.name()) && offering.isActive()) {
				count++;
			}
		}
		return count;
	}
	
	public static boolean isOfferingActive(Offering check, DLUser owner) {
		for(ActiveOffering offering : jsonOfferings) {
			if(offering.name.equals(check.name()) && offering.ownerUuid.equals(owner.getUUIDString()) && offering.isActive()) {
				return true;
			}
		}
		return false;
	}
	
	public static double getXPBoost() {
		double boost = 1;
		if(OfferingManager.isOfferingActive(Offering.XP_BOOSTER_x2) != 0) {
			boost += .25 * OfferingManager.isOfferingActive(Offering.XP_BOOSTER_x2);
		}
		if(OfferingManager.isOfferingActive(Offering.XP_BOOSTER_x4) != 0) {
			boost += .5 * OfferingManager.isOfferingActive(Offering.XP_BOOSTER_x4);
		}
		if(OfferingManager.isOfferingActive(Offering.FIRST_BORN_CHILD) != 0) {
			boost += .25 * OfferingManager.isOfferingActive(Offering.FIRST_BORN_CHILD);
		}
		if(boost > 10) {
			return 10;
		}
		return boost;
	}
	
	public static double getBloodBoost() {
		double boost = 1;
		if(OfferingManager.isOfferingActive(Offering.BLOOD_BOOSTER_x2) != 0) {
			boost += .25 * OfferingManager.isOfferingActive(Offering.BLOOD_BOOSTER_x2);
		}
		if(OfferingManager.isOfferingActive(Offering.BLOOD_BOOSTER_x4) != 0) {
			boost += .5 * OfferingManager.isOfferingActive(Offering.BLOOD_BOOSTER_x4);
		}
		if(OfferingManager.isOfferingActive(Offering.FIRST_BORN_CHILD) != 0) {
			boost += .25 * OfferingManager.isOfferingActive(Offering.FIRST_BORN_CHILD);
		}
		if(boost > 10) {
			return 10;
		}
		return boost;
	}
	
	public static double getFishingDiscount() {
		double boost = 0;
		if(OfferingManager.isOfferingActive(Offering.BIG_BASS) != 0) {
			boost += .05 * OfferingManager.isOfferingActive(Offering.BIG_BASS);
		}
		return boost;
	}

	public static List<ActiveOffering> getActive() {
		return jsonOfferings;
	}

	public static void clearInactive() {
		ArrayList<ActiveOffering> offerings = new ArrayList<>();
		for(ActiveOffering offering : OfferingManager.getActive()) {
			if(offering.isActive()) {
				offerings.add(offering);
			}
		}
		jsonOfferings = offerings;
	}
	
}
