package net.blixate.deadlight.kit.type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;

import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.KillerData;
import net.blixate.deadlight.util.HeadUtils;
import net.md_5.bungee.api.ChatColor;

public class ChangeAbilityGui extends GuiInventory {
	DLUser user;
	AbilityGui gui;
	
	public ChangeAbilityGui(DLUser user, AbilityGui gui) {
		super("Killer Types", 5);
		this.gui = gui;
		this.user = user;
	}
	
	public void construct() {
		setBackArrow(36, gui);
		for(int i = 37; i < 45; i++) {
			setItem(i, GuiItem.blank(Material.RED_STAINED_GLASS_PANE, "&0"));
		}
		this.setItem(10, item(KillerTypes.SCRUBBER));
		this.setItem(12, item(KillerTypes.HUNTER));
		this.setItem(14, item(KillerTypes.CLOAKER));
		this.setItem(16, item(KillerTypes.WARPER));
		this.setItem(20, item(KillerTypes.JACK));
		this.setItem(22, item(KillerTypes.VISITOR));
		this.setItem(24, item(KillerTypes.NECROMANCER));
	}
	
	public GuiItem nokiller() {
		return new GuiItem() {

			@Override
			public void click(DLUser user, GuiInventory inventory) {
			}

			@Override
			public ItemStack item() {
				ItemStack item = new ItemStack(Material.BARRIER);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName("&cComing Soon...");
				item.setItemMeta(meta);
				return item;
			}
			
		};
	}
	
	public GuiItem item(KillerTypes type) {
		return item(type, null);
	}
	
	public GuiItem item(KillerTypes type, Material icon) {
		return new GuiItem() {

			@Override
			public void click(DLUser user, GuiInventory inventory) {
				if(user.killerType.equals(type)) {
					user.send("already_selected", type.getName());
				}else {
					user.send("selected", type.getName());
				}
				user.killerType = type;
				gui.update();
				user.openInventory(new AbilityGui(user, gui.prev));
			}

			@Override
			public ItemStack item() {
				try {
					ConfigurationSection section = type.getData();
					KillerData data = user.killerData.get(type.name());
					if(data == null) {
						user.killerData.put(type.name(), new KillerData(new JSONObject()));
					}
					ItemStack item = null;
					ItemMeta meta = null;
					
					if(!section.contains("head")) {
						item = new ItemStack(icon);
						meta = item.getItemMeta();
					}else {
						item = HeadUtils.createStackablePlayerHead(section.getString("head"));
						if(data.inDarkCommand) {
							item = HeadUtils.createStackablePlayerHead(section.getString("skins.DARK_COMMAND.head"));
						}
						meta = (SkullMeta)item.getItemMeta();
					}
					meta.setDisplayName(ChatColor.GREEN + "Lv. " + data.level + " " + ChatColor.RED + type.getName());
					List<String> lore = new ArrayList<>();
					lore.add("&8Killer Type");
					lore.add("");
					lore.add("&7Difficulty: " + KillerTypeDifficulty.valueOf(section.getString("difficulty").toUpperCase()).toString());
					lore.add("");
					lore.add("&6&n&lAbility");
					for(String line : section.getString("ability").split("\n")) {
						lore.add(ChatColor.YELLOW + line);
					}
					lore.add("");
					if(user.killerType.equals(type)) {
						lore.add(ChatColor.GREEN + "Selected.");
					}else {
						lore.add(ChatColor.GRAY + "Left-click to select.");
					}
					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				} catch(Throwable t) {
					return GuiItem.errorItem(t);
				}
			}
			
		};
	}
}
