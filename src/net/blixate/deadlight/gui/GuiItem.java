package net.blixate.deadlight.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;

public interface GuiItem extends GuiListener, GuiIcon {
	
	public static GuiItem blank(Material mat, String name) {
		return new GuiIcon() {
			public ItemStack item() {
				return GuiInventory.newItem(mat, name);
			}
		}.toGuiItem();
	}
	
	public static GuiItem toGuiItem(GuiListener listener, Material mat, String name, String...lore) {
		return new GuiItem() {
			@Override
			public void click(DLUser user, GuiInventory inventory) {
				if(listener == null) {
					return;
				}
				listener.click(user, inventory);
			}

			@Override
			public ItemStack item() {
				return GuiInventory.newItem(mat, name, lore);
			}
		};
	}

	public static GuiItem playerHead(DLUser user) {
		return new GuiIcon() {
			public ItemStack item() {
				ItemStack item = new ItemStack(Material.PLAYER_HEAD);
				SkullMeta meta = (SkullMeta)item.getItemMeta();
				meta.setOwningPlayer(user.getPlayer());
				meta.setDisplayName(FormatUtil.color("&a" + user.getPlayer().getDisplayName()));
				List<String> lore = new ArrayList<>();
				if(user.getPrestige() >= 0) lore.add("&aPrestige " + user.getPrestige());
				lore.add("&aLevel " + user.getLevel());
				lore.add("");
				lore.add("&aPing &e" + user.getPing() + "ms");
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

	public static ItemStack errorItem(Throwable t) {
		ItemStack item = new ItemStack(Material.BARRIER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("&c" + t.getClass().getName() + ": " + t.getMessage());
		List<String> lore = new ArrayList<>();
		for(StackTraceElement element : t.getStackTrace()) {
			lore.add("&7at " + element.getClassName() + "." + element.getMethodName() + " (" + element.getFileName() + element.getLineNumber() + ")");
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static GuiItem air() {
		return new GuiIcon() {
			public ItemStack item() {
				return new ItemStack(Material.AIR);
			}
		}.toGuiItem();
	}
}
