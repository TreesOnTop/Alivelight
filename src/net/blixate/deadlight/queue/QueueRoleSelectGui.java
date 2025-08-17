package net.blixate.deadlight.queue;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.player.Alignment;
import net.blixate.deadlight.player.DLUser;

public class QueueRoleSelectGui extends GuiInventory {
	
	class RoleSelectItem implements GuiItem {
		
		Alignment alignment;
		String name;
		
		public RoleSelectItem(Alignment alignment, String name) {
			this.alignment = alignment;
			this.name = name;
		}
		
		@Override
		public void click(DLUser user, GuiInventory inventory) {
			user.preferredRole = alignment;
			user.send("set_preferred_role", name);
			inventory.update();
		}

		@Override
		public ItemStack item() {
			String color = (alignment == Alignment.KILLER ? "&c" : "&a");
			ItemStack item = new ItemStack(alignment == Alignment.KILLER ? Material.IRON_SWORD : Material.LEATHER_HELMET);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("&aPlay as " + color + name);
			List<String> lore = new ArrayList<>();
			lore.add("&8Role");
			lore.add("");
			lore.add("&7Sets your preferred role to " + color + name);
			lore.add("&7This does not guarentee you will get that role.");
			lore.add("");
			if(user.preferredRole == alignment) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 0, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				lore.add("&aSelected.");
			}else {
				lore.add("&7Click to select.");
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		}
		
	}
	
	DLUser user;
	
	public QueueRoleSelectGui(DLUser user) {
		super("Select Preferred Role", 3);
		this.user = user;
	}
	
	public void construct() {
		this.setItem(11, new RoleSelectItem(Alignment.SURVIVOR, "Survivor"));
		this.setItem(13, new GuiItem() {
			@Override
			public void click(DLUser user, GuiInventory inventory) {
				user.preferredRole = null;
				inventory.update();
			}
			
			@Override
			public ItemStack item() {
				ItemStack item = new ItemStack(Material.BARRIER);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName("&aPlay as &fAny");
				List<String> lore = new ArrayList<>();
				lore.add("&8Role");
				lore.add("");
				lore.add("&7Removes your preferred role.");
				lore.add("&7You will get whatever role is available.");
				lore.add("");
				if(user.preferredRole == null) {
					meta.addEnchant(Enchantment.ARROW_INFINITE, 0, true);
					meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					lore.add("&aSelected.");
				}else {
					lore.add("&7Click to select.");
				}
				meta.setLore(lore);
				item.setItemMeta(meta);
				return item;
			}
		});
		this.setItem(15, new RoleSelectItem(Alignment.KILLER, "Killer"));
	}

}
