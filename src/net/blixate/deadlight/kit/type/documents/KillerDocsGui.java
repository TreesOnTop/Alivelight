package net.blixate.deadlight.kit.type.documents;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.kit.type.AbilityGui;
import net.blixate.deadlight.kit.type.KillerTypes;
import net.blixate.deadlight.player.DLUser;

public class KillerDocsGui extends GuiInventory {
	
	DLUser user;
	AbilityGui prev;
	KillerTypes types;
	
	int page;
	
	boolean isEmpty = false;
	
	public KillerDocsGui(DLUser user, KillerTypes killer, AbilityGui prev) {
		super(killer.getName() + " > Files", 5);
		this.prev = prev;
		this.user = user;
		this.types = killer;
	}
	
	public void construct() {
		this.fill(GuiItem.blank(Material.BLACK_STAINED_GLASS_PANE, "&0"));
		if(!isEmpty) {
			for(int row = 1; row < 4; row++) {
				for(int i = 0; i < 7; i++) {
					int bookIndex = 1 + (i + (row-1) * 7) + (page * 21);
					this.setItem(i+(row*9)+1, GuiItem.blank(Material.BOOK, "&aKiller Document #" + bookIndex));
				}
			}
			this.setItem(46, new GuiItem() {
				@Override
				public void click(DLUser user, GuiInventory inventory) {
					page++;
					update();
					user.playSound(Sound.ENTITY_BAT_TAKEOFF, 1);
				}
				@Override
				public ItemStack item() {
					ItemStack item = new ItemStack(Material.ARROW);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName("&aNext Page");
					item.setItemMeta(meta);
					return item;
				}
			});
		}
		else {
			this.setItem(22, GuiItem.blank(Material.BARRIER, "&cLibrary is empty."));
		}
		this.setBackArrow(36, prev);
	}
}
