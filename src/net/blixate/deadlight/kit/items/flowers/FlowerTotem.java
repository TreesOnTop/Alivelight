package net.blixate.deadlight.kit.items.flowers;

import org.bukkit.Location;

import net.blixate.deadlight.kit.perks.PerkEvent;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.DLUser;

public class FlowerTotem {
	
	Location location;
	DLUser owner;
	
	public FlowerTotem() {
	}
	
	public void place(Location location, DLUser owner) {
		this.location = location;
		this.owner = owner;
	}
	
	public void tick(Lobby lobby) {
		
	}
	
	public void destroy() {
		
	}
	
	public void onRadiusEnter(DLUser user) {}
	public void onRadiusExit(DLUser user) {}
	public void event(PerkEvent event) {}
	
	public Location getLocation() {
		return location;
	}
	
	public DLUser getOwner() {
		return owner;
	}
	
}
