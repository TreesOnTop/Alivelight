package net.blixate.deadlight.gui.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.DLUser;

public class LobbyListGui extends GuiInventory {
	
	DLUser user;
	
	public LobbyListGui(DLUser user) {
		super("Lobby List", 3);
		this.user = user;
	}
	
	public void construct() {
		boolean isAnyLobbyActive = false;
		for(int i = 0; i < Deadlight.getLobbyManager().getCount(); i++) {
			Lobby lobby = Deadlight.getLobbyManager().getLobby(i);
			if(lobby != null) {
				setItem(i, new ActiveLobbyItem(i));
				isAnyLobbyActive = true;
			}
		}
		if(!isAnyLobbyActive) {
			setItem(13, GuiItem.blank(Material.BARRIER, "&cNo active matches."));
		}
	}
	
	class ActiveLobbyItem implements GuiItem {
		
		int index;
		
		public ActiveLobbyItem(int lobby) {
			this.index = lobby;
		}
		
		@Override
		public void click(DLUser user, GuiInventory inventory) {
			if(user.getLobby() != null) {
				return;
			}
			// spectating moment
			if(Deadlight.cfg().getBoolean("global.enable spectating")) {
				Lobby lobby = Deadlight.getLobbyManager().getLobby(index);
				if(lobby == null) {
					return;
				}
				if(!lobby.getMap().isBeta()) {
					lobby.addSpectator(user);
					user.spectate();
					user.getPlayer().teleport(lobby.getKiller().getLocation());
					user.send("spectating");
				} else {
					user.send("disabled");
				}
			}else {
				user.send("disabled");
			}
		}
		
		@Override
		public void rightClick(DLUser user, GuiInventory inventory) {
			if(!user.checkPerm("deadlight.lobbies")) {
				return;
			}
			try {
				Lobby lobby = Deadlight.getLobbyManager().getLobby(index);
				if(lobby == null) {
					user.send("command_error", "Lobby " + index + " doesn't exist anymore.");
					return;
				}
				lobby.end();
				inventory.update();
			}catch(Throwable t) {
				user.send("command_error", "Couldn't stop lobby " + index);
			}
		}

		@Override
		public ItemStack item() {
			ItemStack item = new ItemStack(Material.GREEN_CONCRETE);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("&aLobby " + (index + 1));
			List<String> lore = new ArrayList<String>();
			Lobby lobby = Deadlight.getLobbyManager().getLobby(index);
			lore.add("&8Game Lobby");
			lore.add("");
			try {
				lore.add("&7Map: &e" + lobby.getMap());
			}catch(Exception e) {
				lore.add("&cMap Error");
			}
			try {
				lore.add("&7State: &e" + lobby.getStateString());
			}catch(Exception e) {
				lore.add("&cState Error");
			}
			lore.add("");
			lore.add("&7Players:");
			for(DLUser user : lobby.getPlayers()) {
				try {
					if(user == null) {
						lore.add("&7 - ???");
					}
					else if(user.isSpectating()) {
						lore.add("&7 - &m" + user.getName());
					}else if(user.isKiller()) {
						lore.add("&7 - &c" + user.getName());
					}else {
						if(user.isObsession()) {
							lore.add("&7 - &b" + user.getName());
						}else {
							lore.add("&7 - &a" + user.getName());
						}
					}
				}catch(Throwable t) {
					lore.add("&c - Player Error");
				}
				
			}
			if(user.getLobby() == null && !lobby.getMap().isBeta()) {
				if(Deadlight.cfg().getBoolean("global.enable spectating")) {
					lore.add("");
					lore.add("&7Left-Click to spectate.");
				}
			}
			if(user.checkPerm("deadlight.lobbies")) {
				lore.add("&c&l[STAFF] &7Right-click to stop game in progress.");
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		}
	}
	
	class InactiveLobbyItem implements GuiItem {

		int index;
		
		public InactiveLobbyItem(int lobby) {
			this.index = lobby;
		}
		
		@Override
		public void click(DLUser user, GuiInventory inventory) {
			
		}

		@Override
		public ItemStack item() {
			ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("&7Inactive Lobby");
			item.setItemMeta(meta);
			return item;
		}
	}
}
