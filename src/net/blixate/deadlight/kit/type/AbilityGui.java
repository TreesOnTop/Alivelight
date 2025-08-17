package net.blixate.deadlight.kit.type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;

import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.kit.KitGui;
import net.blixate.deadlight.kit.type.addons.AddonManager;
import net.blixate.deadlight.kit.type.addons.AddonsGui;
import net.blixate.deadlight.kit.type.documents.KillerDocsGui;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.KillerData;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.HeadUtils;
import net.md_5.bungee.api.ChatColor;

public class AbilityGui extends GuiInventory {
	
	DLUser user;
	KillerTypes type;
	KitGui prev;
			
	public AbilityGui(DLUser user, KitGui prev) {
		super(user.killerType.getName(), 5);
		this.user = user;
		this.type = user.killerType;
		this.prev = prev;
	}
	
	public void construct() {
		setBackArrow(36, prev);
		for(int i = 37; i < 45; i++) {
			setItem(i, GuiItem.blank(Material.RED_STAINED_GLASS_PANE, "&0"));
		}
		this.setItem(4, (user, inv) -> {
			user.openInventory(new ChangeAbilityGui(user, this));
		}, head());
		if(getKillerData().level >= 5) {
			this.setItem(20, (user, inv) -> {
				user.openInventory(new AddonsGui(user, type, this));
			}, augments());
		}else {
			this.setItem(20, GuiItem.blank(Material.RED_STAINED_GLASS_PANE, "&cReach Killer Level 5 to unlock."));
		}
		
		if(getKillerData().level >= 20) {
			this.setItem(22, (user, inv) -> {
				user.openInventory(new KillerDocsGui(user, type, this));
			}, journal());
		}else {
			this.setItem(22, GuiItem.blank(Material.RED_STAINED_GLASS_PANE, "&cReach Killer Level 20 to unlock."));
		}
		
		
		if(getKillerData().level >= 50) {
			this.setItem(24, (user, inv) -> {
				user.killerData.get(type.name()).inDarkCommand = !user.killerData.get(type.name()).inDarkCommand;
				this.update();
			}, cosmetics());
		}else {
			this.setItem(24, GuiItem.blank(Material.RED_STAINED_GLASS_PANE, "&cReach Killer Level 50 to unlock."));
		}
	}
	
	private ItemStack journal() {
		ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("&aExterminator Documents");
		List<String> lore = new ArrayList<>();
		lore.add("&8Library");
		lore.add("");
		//lore.add("&7Unlocked (&e0&8/&716)");
		lore.add("&cUnavailable.");
		lore.add("");
		lore.add("&7Click to view.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack head() {
		try {
			ConfigurationSection section = type.getData();
			ItemStack item = HeadUtils.createStackablePlayerHead(section.getString("head"));
			if(getKillerData().inDarkCommand) {
				item = HeadUtils.createStackablePlayerHead(section.getString("skins.DARK_COMMAND.head"));
			}
			SkullMeta meta = (SkullMeta)item.getItemMeta();
			KillerData data = user.killerData.get(type.name());
			meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + type.getName());
			List<String> lore = new ArrayList<>();
			lore.add("&8Killer Type");
			lore.add("");
			lore.add("&eLevel "+data.level+" &8(&e"+
					FormatUtil.formatLong(data.xp)+"xp&7/&e"+FormatUtil.formatLong(data.getLevelUpExp()) + "xp&8)");
			lore.add("");
			lore.add("&7Difficulty: " + KillerTypeDifficulty.valueOf(section.getString("difficulty").toUpperCase()).toString());
			lore.add("");
			if(getKillerData().inDarkCommand) {
				for(String line : section.getString("skins.DARK_COMMAND.extra").split("\n")) {
					lore.add(ChatColor.GRAY + line);
				}
			}else {
				for(String line : section.getString("description").split("\n")) {
					lore.add(ChatColor.GRAY + line);
				}
			}
			lore.add("");
			lore.add("&6&n&lAbility");
			for(String line : section.getString("ability").split("\n")) {
				lore.add(ChatColor.YELLOW + line);
			}
			lore.add("");
			lore.add("&7Click to change.");
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		} catch(Throwable t) {
			return GuiItem.errorItem(t);
		}
	}
	
	private ItemStack augments() {
		try {
			ItemStack item = new ItemStack(Material.PRISMARINE_CRYSTALS);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("&bAugments");
			List<String> lore = new ArrayList<>();
			lore.add("&8Modification");
			lore.add("");
			List<String> addons = getKillerData().getAddons();
			if(addons == null) {
				addons = new ArrayList<String>();
			}
			for(int i = 0; i < 2; i++) {
				if(addons.size() > i) {
					lore.add("&7Slot " + i + ": &e" + AddonManager.getNameById(type, addons.get(i)));
				}else {
					lore.add("&7Slot " + i + ": &cNone");
				}
			}
			lore.add("");
			lore.add("&7Click to change.");
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		} catch(Throwable t) {
			return GuiItem.errorItem(t);
		}
	}
	
	private ItemStack cosmetics() {
		ItemStack item = new ItemStack(Material.NETHERITE_HELMET);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		meta.setDisplayName("&4&lThe Dark Command");
		lore.add("&8Cosmetic");
		lore.add("");
		lore.add("&eBy joining, you will get access");
		lore.add("&eto the Dark Command skin of this");
		lore.add("&ekiller. You can leave at any time.");
		lore.add("");
		if(getKillerData().inDarkCommand) {
			lore.add("&aThis killer is in the Dark Command!");
			lore.add("");
			lore.add("&7Click to leave.");
			meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}else {
			lore.add("&cThis killer is not in the Dark Command!");
			lore.add("");
			if(getKillerData().level >= 50) {
				lore.add("&7Click to join.");
			}else {
				lore.add("&cRequires &eKiller Level 50&c!");
			}
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	private KillerData getKillerData() {
		if(!user.killerData.containsKey(type.name())) {
			user.killerData.put(type.name(), new KillerData(new JSONObject()));
		}
		return user.killerData.get(type.name());
	}
}
