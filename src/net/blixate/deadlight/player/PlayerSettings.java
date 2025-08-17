package net.blixate.deadlight.player;

import java.util.HashMap;
import java.util.List;

import net.blixate.deadlight.music.DLTrack;

public class PlayerSettings {
	
	public boolean globalGameChat = false; // Whether your messages should be viewable to everyone on the server or not
	public boolean muteGlobalChat = false; // Whether messages at spawn should be visible to you in a game.
	public char gameChatPrefix = '@'; // Prefix your message with this to just tell your team mates the contents of the message
	
	public boolean musicEnabled; // Whether to play music or not
	public List<DLTrack> enabledTracks; // What tracks are allowed/not allowed to play
	public HashMap<DLTrack, Float> musicVolume; // The volume of each track.
	
}
