package net.blixate.deadlight.kit.items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.blixate.deadlight.gui.GuiIcon;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.kit.KitGui;
import net.blixate.deadlight.player.DLInventory;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;

public class ItemGui extends GuiInventory {
	
	class InvItem implements GuiItem {
		
		Items item;
		
		public InvItem(Items item) {
			this.item = item;
		}
		
		public void click(DLUser user, GuiInventory inventory) {
			if(user.equippedItem == null || !user.equippedItem.equals(item.name())) {
				user.equippedItem = item.name();
				user.send("item_equip", item.name);
			}else if(user.equippedItem.equals(item.name())) {
				user.equippedItem = null;
				user.send("item_unequip", item.name);
			}
			inventory.update();
		}

		public ItemStack item() {
			String error = null;
			int amount = 0;
			if(item == null || item.icon == null) {
				error = "Item is null!";
			}
			else if(user.getItemInventory() == null) {
				error = "Inventory is null!";
			}
			else {
				amount = user.getItemInventory().getItem(item.name());
				if(amount < 0) {
					error = "Amount is less than zero.";
				}
			}
			
			if(error != null) {
				return GuiIcon.createItemStack(Material.BARRIER, "&4ERROR&c: This should not appear!", "&7" + error);
			}
			ItemStack icon = new ItemStack(item.icon, 1);
			ItemMeta meta = icon.getItemMeta();
			
			meta.setDisplayName(FormatUtil.color("&ax" + amount + " &7" + item.name));
			List<String> lore = new ArrayList<String>();
			lore.add("&8Survivor Item");
			lore.add("");
			for(String line : item.desc.split("\n")) {
				lore.add("&7" + line);
			}
			if(user.equippedItem != null && user.equippedItem.equals(item.name())) {
				shiny(meta);
				lore.add("");
				lore.add("&a&lEQUIPPED");
			}else {
				lore.add("");
				lore.add("&7Click to equip");
			}
			meta.setLore(lore);
			icon.setItemMeta(meta);
			return icon;
		}
	}
	
	DLUser user;
	KitGui gui;
	
	public ItemGui(DLUser user, KitGui gui) {
		super("Your Inventory", 4);
		this.user = user;
		this.gui = gui;
	}
	
	public void construct() {
		for(int i = 27; i < 36; i++) {
			setItem(i, GuiItem.blank(Material.GREEN_STAINED_GLASS_PANE, "&0"));
		}
		setBackArrow(27, gui);
		DLInventory inv = user.getItemInventory();
		if(inv.getKeys() == null || inv.getKeys().length == 0) {
			setItem(13, GuiItem.toGuiItem(null, Material.BARRIER, "&cYou don't have any items!", "&7Collect items by fishing", "&7or through the Mystery shop!"));
		}else {
			int i = 0;
			List<String> items = Lists.newArrayList(inv.getKeys()).stream().sorted().collect(Collectors.toList());
			for(String itemName : items) {
				Items item = Items.get(itemName);
				if(item == null) continue;
				setItem(i++, new InvItem(item));
			}
		}
	}
	
}
