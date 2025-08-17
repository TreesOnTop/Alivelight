package net.blixate.deadlight.music;

import net.blixate.deadlight.util.time.TimeParser;

public enum DLTrack {
	DOOMED("blixate:music.theme", "2m17s", 0.7f),
	LOBBY_ALT("blixate:music.lobby", "3m5s"),
	TYRANNY("blixate:music.tyranny", "1m38s", 0.5f),
	LOST("blixate:music.lost", "1m25s", 0.4f),
	COLD_SPIRITS("blixate:music.scary", "3m20s"),
	COLLAPSE("blixate:music.collapse", "2m28s");
	
	String id;
	long length;
	float volume;
	
	DLTrack(String id, String length) {
		this.id = id;
		this.length = TimeParser.parseTime(length);
		this.volume = 1f;
	}
	
	DLTrack(String id, String length, float volume) {
		this.id = id;
		this.length = TimeParser.parseTime(length);
		this.volume = volume;
	}
	
	public String getResourcePackId() {
		return this.id;
	}
	
	public long getLength() {
		return this.length;
	}
	
	public double getLengthInSeconds() {
		return (double) this.length / 1000d;
	}

	public float getVolume() {
		return volume;
	}
}
