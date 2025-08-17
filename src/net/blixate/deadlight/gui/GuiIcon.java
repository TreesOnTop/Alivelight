package net.blixate.deadlight.gui;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.blixate.deadlight.player.DLUser;

public interface GuiIcon {
	/** The item which should be placed in this slot.
	 * @return {@link ItemStack} to place at this slot. */
	public ItemStack item();
	
	/** Assumes the GuiIcon does not do anything. */
	public default GuiItem toGuiItem() {
		return new GuiItem() {
			public void click(DLUser user, GuiInventory inventory) {}
			public ItemStack item() { return GuiIcon.this.item(); }
		};
	}
	
	public default void shiny(ItemMeta meta) {
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	}
	
	public static GuiIcon fromItemStack(ItemStack item) {
		return new GuiIcon() {
			public ItemStack item() {
				return item;
			}
		};
	}
	
	public static ItemStack createItemStack(Material mat, String name, String...lore) {
		ItemStack item = new ItemStack(mat, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		if(lore != null && lore.length > 0) {
			meta.setLore(Lists.newArrayList(lore));
		}
		item.setItemMeta(meta);
		return item;
	}
}
