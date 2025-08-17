package net.blixate.deadlight.util.file;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;

import net.blixate.deadlight.Deadlight;

public class SongManager {
	
	static HashMap<String, Song> cache = new HashMap<>();
	
	public static boolean hasNoteBlockAPI() {
		return Deadlight.inst.noteBlockApi != null;
	}
	
	public static Song getSong(String name) {
		if(Deadlight.inst.noteBlockApi == null) {
			return null;
		}
		if(!cache.containsKey(name)) {
			Song song = NBSDecoder.parse(new File(Deadlight.dataFolder, "songs/" + name + ".nbs"));
			cache.put(name, song);
			return song;
		}
		return cache.get(name);
	}
	
	public static void playSong(Player player, Song song) {
		if(Deadlight.inst.noteBlockApi == null) {
			return;
		}
		RadioSongPlayer songPlayer = new RadioSongPlayer(song, SoundCategory.MASTER);
		songPlayer.addPlayer(player);
		songPlayer.setPlaying(true);
	}
	
	public static void playSong(Song song, Player...players) {
		if(Deadlight.inst.noteBlockApi == null) {
			return;
		}
		RadioSongPlayer songPlayer = new RadioSongPlayer(song, SoundCategory.MASTER);
		for(Player p : players) {
			songPlayer.addPlayer(p);
		}
		songPlayer.setPlaying(true);
	}
	public static void playSong(Song song, Collection<? extends Player> players) {
		if(Deadlight.inst.noteBlockApi == null) {
			return;
		}
		RadioSongPlayer songPlayer = new RadioSongPlayer(song, SoundCategory.MASTER);
		for(Player p : players) {
			songPlayer.addPlayer(p);
		}
		songPlayer.setPlaying(true);
	}
}
