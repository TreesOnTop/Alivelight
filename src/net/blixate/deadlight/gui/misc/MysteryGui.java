package net.blixate.deadlight.gui.misc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.kit.items.Items;
import net.blixate.deadlight.kit.perks.PerkRegistry;
import net.blixate.deadlight.player.DLInventory;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;

public class MysteryGui extends GuiInventory {
	
	class PerkItem implements GuiItem {

		PerkRegistry perk;
		
		public PerkItem(PerkRegistry registry) {
			this.perk = registry;
		}
		
		@Override
		public void click(DLUser user, GuiInventory inventory) {
			
		}

		@Override
		public ItemStack item() {
			return null;
		}
		
	}
	
	DLUser user;
	
	public MysteryGui(DLUser user) {
		super("Mystery Market", 5);
		this.user = user;
	}
	
	@Override
	public void construct() {
		this.fill(GuiItem.blank(Material.BLACK_STAINED_GLASS_PANE, "&0"));
		
		int column = 0;
		int i = 10;
		for(Items item : Items.values()) {
			setItem(i, new GuiItem() {
				public void customClick(DLUser user, GuiInventory inventory, ClickType click) {
					if(click == ClickType.LEFT) {
						// buy the item
						if(user.getSouls() >= item.cost) {
							user.getItemInventory().addItem(item.name());
							user.souls = user.souls.subtract(BigInteger.valueOf(item.cost));
							user.send("purchased", "Item", item.name);
							inventory.update();
						}else {
							user.send("no_funds");
						}
					}
					else if(click == ClickType.RIGHT) {
						if(user.getItemInventory().getItem(item.name()) > 0) {
							user.getItemInventory().removeItem(item.name());
							user.souls = user.souls.add(BigInteger.valueOf(item.cost/4));
							user.send("sold", "Item", item.name);
							inventory.update();
						}else {
							user.send("no_resources");
						}
					}
					else if(click == ClickType.SHIFT_RIGHT) {
						int amount = user.getItemInventory().getItem(item.name());
						if(amount > 1) {
							user.getItemInventory().removeItem(item.name(), amount);
							user.souls = user.souls.add(BigInteger.valueOf((amount*item.cost)/4));
							user.send("sold", "All", item.name);
							inventory.update();
						}else {
							user.send("no_resources");
						}
					}
				}
				
				@Override
				public ItemStack item() {
					DLInventory itemInventory = user.getItemInventory();
					int amount = itemInventory.getItem(item.name());
					ItemStack stack = new ItemStack(item.icon);
					ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("&6" + item.name);
					List<String> lore = new ArrayList<String>();
					lore.add("&8Survivor Item");
					lore.add("");
					for(String line : item.desc.split("\n")) {
						lore.add("&7" + line);
					}
					if(amount > 0) {
						lore.add("");
						lore.add("&7You have &e" + amount + "&7 of this item!");
					}
					lore.add("");
					lore.add("&aLeft-click to buy for &b" + FormatUtil.formatLong(item.cost) + " Souls&a.");
					lore.add("&aRight-click to sell for &b" + FormatUtil.formatLong(item.cost/4) + " Souls&a.");
					if(user.getItemInventory().getItem(item.name()) > 1) {
						lore.add("&aShift Right-click to &lsell all&a for &b" + FormatUtil.formatLong((amount*item.cost)/4) + " Souls&a.");
					}
					meta.setLore(lore);
					stack.setItemMeta(meta);
					return stack;
				}

				@Override
				public void click(DLUser user, GuiInventory inventory) {}
				
			});
			i++;
			column ++;
			if(column > 6) {
				i += 2;
				column = 0;
			}
		}
	}
	
}
