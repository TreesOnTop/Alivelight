package net.blixate.deadlight.challenges;

import org.json.simple.JSONObject;

public class AssignedChallenge {
	
	public ChallengeCategory category;
	public int id;
	public int progress;
	public int requirement;
	
	public AssignedChallenge(ChallengeCategory category, JsonChallenge challenge) {
		this.id = challenge.index;
		this.category = category;
		this.progress = 0;
		this.requirement = challenge.getRequirement();
	}
	
	public AssignedChallenge(ChallengeCategory category, long id, long progress, long requirement) {
		this.id = (int)id;
		this.category = category;
		this.progress = (int)progress;
		this.requirement = (int)requirement;
	}
	
	public AssignedChallenge(JSONObject object) {
		this(ChallengeCategory.valueOf((String)object.get("cat")), (Long)object.get("id"), (Long) object.get("prog"), (Long) object.get("req"));
	}
	
	public boolean isCompleted() {
		return this.progress >= this.requirement;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject obj = new JSONObject();
		obj.put("id", this.id);
		obj.put("prog", progress);
		obj.put("req", requirement);
		obj.put("cat", category.name());
		return obj;
	}
}
