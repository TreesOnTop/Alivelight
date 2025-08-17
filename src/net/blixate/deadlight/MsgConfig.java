package net.blixate.deadlight;

public class MsgConfig {
	public static final String DEFAULT_LANGUAGE = "English";
	
	public static String getTip() {
		return getRandomMessage("tips");
	}
	
	public static String getRandomMessage(String name) {
		String[] strings = getMessageList(name);
		return strings[Deadlight.RNG.nextInt(strings.length)];
	}
	
	public static String[] getMessageList(String location) {
		return Deadlight.messages.property(DEFAULT_LANGUAGE, location).asArray().toStringArray();
	}
}
