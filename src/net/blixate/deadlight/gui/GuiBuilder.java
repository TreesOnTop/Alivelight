package net.blixate.deadlight.gui;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import net.blixate.deadlight.player.DLUser;

public class GuiBuilder {
	HashMap<Integer, GuiItem> items;
	String title = "";
	int size = 0;
	
	public GuiBuilder(String title, int size) {
		this.title = title;
		this.size = size;
		this.items = new HashMap<>();
	}
	
	public GuiBuilder setTitle(String title) {
		this.title = title;
		return this;
	}
	
	public GuiBuilder setItem(int slot, Material mat, String name) {
		items.put(slot, GuiItem.blank(mat, name));
		return this;
	}
	
	public GuiBuilder setIcon(int slot, GuiIcon icon) {
		items.put(slot, icon.toGuiItem());
		return this;
	}
	
	public GuiBuilder addBackArrow(GuiInventory prevInv) {
		return setItemListener(size * 9 - 9, new GuiListener() {
			public void click(DLUser user, GuiInventory inventory) {
				user.openInventory(prevInv);
			}
		}, Material.ARROW, "&cBack");
	}
	
	public GuiBuilder setItemListener(int slot, GuiListener listener, Material mat, String name, String...lore) {
		items.put(slot, GuiItem.toGuiItem(listener, mat, name, lore));
		return this;
	}
	
	public GuiBuilder setGuiItem(int slot, GuiItem item) {
		items.put(slot, item);
		return this;
	}
	
	/** Builds an inventory with click listeners. */
	public GuiInventory build() {
		return new GuiInventory(title, size, items);
	}
	
	/** Fill the entire inventory with a specific item*/
	public GuiBuilder fill(GuiItem item) {
		for(int i = 0; i < size*9;i++) {
			setGuiItem(i, item);
		}
		return this;
	}
	
	/** Builds an inventory without any click listeners. */
	public Inventory buildInventory() {
		Inventory inv = Bukkit.createInventory(null, size * 9, title);
		for(int item : items.keySet()) {
			inv.setItem(item, items.get(item).item());
		}
		return inv;
	}
}
