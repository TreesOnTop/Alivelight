package net.blixate.deadlight.queue;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.gui.GuiIcon;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;

public class QueueGui extends GuiInventory {
	
	QueueLobby queue;
	
	public QueueGui(QueueLobby lobby) {
		super("Queue", 3);
		this.queue = lobby;
	}
	
	public void construct() {
		
		setItem(9, new GuiItem() {
			public ItemStack item(){return GuiInventory.newItem(Material.BARRIER, "&cLeave Queue", "&7Click to leave the queue.");}
			public void click(DLUser user, GuiInventory inventory) {
				if(queue.players.contains(user)) {
					queue.removeDLUser(user);
					user.getPlayer().closeInventory();
					user.send("user_leave_queue");
					inventory.update();
				}
			}
		});
		
		setItem(9+8, new GuiItem() {
			public ItemStack item(){
				ItemStack item = new ItemStack(Material.LIME_DYE);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName("&aVote to Start");
				List<String> lore = new ArrayList<>();
				lore.add("&7Votes: &e" + queue.votesToStart.size());
				lore.add("");
				lore.add("&d&lNOTE: &eThis server is best played");
				lore.add("&ewith 5 players.");
				meta.setLore(lore);
				item.setItemMeta(meta);
				return item;
			}
			
			public void rightClick(DLUser user, GuiInventory inventory) {
				if(user.checkPerm("deadlight.lobbies")) {
					queue.start();
					return;
				}
			}
			
			public void click(DLUser user, GuiInventory inventory) {
				if(user.guiCooldown.isDone()) {
					if(queue.votesToStart.contains(user)) {
						queue.votesToStart.remove(user);
						for(DLUser player : Deadlight.queue.players) {
							if(player.equals(user)) {
								user.send("queue_start_unvote");
								continue;
							}
							player.send("queue_announce_unvote", user.getName());
						}
					}else{
						queue.votesToStart.add(user);
						for(DLUser player : Deadlight.queue.players) {
							if(player.equals(user)) {
								user.send("queue_start_vote");
								continue;
							}
							player.send("queue_announce_vote", user.getName());
						}
						if(queue.players.size() > 1 && queue.votesToStart.size() == queue.players.size()) {
							queue.start();
							return;
						}
						user.guiCooldown.start(1f);
						inventory.update();
					}
				}
			}
		});
		for(int i = 11; i <= 11+4; i++)
			setItem(i, GuiItem.blank(Material.SKELETON_SKULL, "&aPlayer Slot"));
		int i = 0;
		for(DLUser user : queue.players) {
			setItem(11+i, playerHead(user));
			i++;
		}
	}
	
	public GuiItem playerHead(DLUser user) {
		return new GuiIcon() {
			public ItemStack item() {
				ItemStack item = new ItemStack(Material.PLAYER_HEAD);
				SkullMeta meta = (SkullMeta)item.getItemMeta();
				meta.setOwningPlayer(user.getPlayer());
				meta.setDisplayName(FormatUtil.color("&a" + user.getPlayer().getDisplayName()));
				List<String> lore = new ArrayList<>();
				if(user.getPrestige() > 0) lore.add("&7Prestige " + user.getPrestige());
				lore.add("&7Level " + user.getLevel());
				lore.add("&7Ping " + user.getPing() + "ms");
				if(Deadlight.queue.votesToStart.contains(user)) {
					lore.add("");
					lore.add("&aVoted to start");
				}
				meta.setLore(lore);
				item.setItemMeta(meta);
				return item;
			}
		}.toGuiItem();
	}
	
}
