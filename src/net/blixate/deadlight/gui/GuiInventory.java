package net.blixate.deadlight.gui;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;

/**
 * <h1>Used for creating an inventory for GUI purposes.</h1><br>
 * Stores the title, rows, and items in this class.
 * <p>This class can be inherited to create an inventory in a seperate class. If needed,
 * extends this class then put your entire inventory creation in the constructor. Do not
 * override any methods unless you know what your doing, and it's absolutely necessary.</p>
 * This does not have a listener implemented by default, these events are handled outside of this class.
 * @see GuiItem
 * @see Inventory
 * @see DLUser
 */
public class GuiInventory {

	String title;
	int size;
	public HashMap<Integer, GuiItem> items;
	public Inventory inventory;
	
	public GuiInventory(String title, int rows) {
		this.title = FormatUtil.color(title);
		this.size = rows*9;
		this.items = new HashMap<>();
	}
	
	public GuiInventory(String title, int rows, HashMap<Integer, GuiItem> items) {
		this(title, rows);
		this.items = items;
		this.inventory = create();
	}
	
	public void openInventory(DLUser user) {
		if(inventory == null) update();
		user.getPlayer().openInventory(this.inventory);
	}
	
	/**
	 * Create your inventory here.
	 * 
	 * This is called when {@code update()} is called, as well as when the inventory itself is created.
	 * */
	public void construct() {
		
	}
	
	public void update() {
		update(true);
	}
	
	/**
	 * Updates the user's inventory without closing and reopening the inventory.
	 */
	public void update(boolean clear) {
		if(this.inventory == null) {
			this.inventory = create();
		}
		if(clear) {
			clear();
		}
		this.construct();
		
		for(int slot : items.keySet()) {
			if(slot >= size) {
				throw new IllegalArgumentException("Cannot set slot " + slot + " to " + items.get(slot));
			}
			this.inventory.setItem(slot, formatItem(items.get(slot)));
		}
	}
	
	protected Inventory create() {
		Inventory inv = Bukkit.createInventory(null, size, title);
		
		for(int slot : items.keySet()) {
			inv.setItem(slot, formatItem(items.get(slot)));
		}
		return inv;
	}
	
	private ItemStack formatItem(GuiIcon guiItem) {
		ItemStack item = (guiItem == null ? new ItemStack(Material.BARRIER) : guiItem.item());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(FormatUtil.color(meta.getDisplayName()));
		List<String> lore = Lists.newArrayList();
		if(meta.getLore() != null) {
			for(String s : meta.getLore()) {
				lore.add(FormatUtil.color(s));
			}
		}
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public void setItem(int slot, GuiItem item) {
		this.items.put(slot, item);
	}
	
	public void setItem(int slot, GuiListener item, ItemStack stack) {
		if(item == null) {
			// there is no listener
			setItem(slot, GuiIcon.fromItemStack(stack));
		}else {
			this.items.put(slot, item.toGuiItem(stack));
		}
		
	}
	
	public void setItem(int slot, GuiIcon item) {
		this.items.put(slot, item.toGuiItem());
	}
	
	public void fill(GuiItem item) {
		for(int i = 0; i < size; i++) {
			setItem(i, item);
		}
	}
	
	public void clear() {
		for(int i = 0; i < size; i++) {
			//inventory.clear(i);
			inventory.setItem(0, new ItemStack(Material.AIR));
		}
	}
	
	public static ItemStack newItem(Material mat, String name, String...lore) {
		ItemStack item = new ItemStack(mat, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		if(lore.length != 0)
			meta.setLore(Lists.newArrayList(lore));
		item.setItemMeta(meta);
		return item;
	}
	
	

	public GuiItem getSlot(int slot) {
		return items.get(slot);
	}
	
	public boolean equals(Object other) {
		if(other == this) return true;
		if(!(other instanceof GuiInventory)) return false;
		GuiInventory comp = (GuiInventory)other;
		return this.title.equals(comp.title) && this.size == comp.size;
	}
	
	public String toString() {
		return "GuiInventory[size=" + this.size+ "title=" + this.title + "]";
	}
	
	public void setBackArrow(int slot, GuiInventory previousGui) {
		if(previousGui != null) {
			this.setItem(slot, new GuiListener.OpenInventory(previousGui), GuiIcon.createItemStack(Material.ARROW, "&cBack"));
		}
	}
}
