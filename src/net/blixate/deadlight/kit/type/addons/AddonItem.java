package net.blixate.deadlight.kit.type.addons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.kit.offerings.Offering;
import net.blixate.deadlight.kit.offerings.OfferingManager;
import net.blixate.deadlight.kit.type.KillerTypes;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.KillerData;
import net.blixate.deadlight.util.FormatUtil;
import net.md_5.bungee.api.ChatColor;

public class AddonItem implements GuiItem {
	Addon addon;
	
	DLUser user;
	KillerTypes type;
	
	public AddonItem(DLUser user, KillerTypes type, ConfigurationSection section) {
		this.addon = new Addon(section);
		
		this.user = user;
		this.type = type;
	}

	@Override
	public void click(DLUser user, GuiInventory inventory) {
		KillerData data = user.killerData.get(type.name());
		if(data.level >= addon.rarity.level) {
			if(data.selectedAugments.contains(addon.id)) {
				data.selectedAugments.remove(addon.id);
			}else {
				if(data.selectedAugments.size() < 2) {
					data.selectedAugments.add(addon.id);
				}
			}
			inventory.update();
		}else {
			user.send("addon_too_low_level", addon.rarity.level + "");
		}
	}

	@Override
	public ItemStack item() {
		ItemStack item = new ItemStack(addon.rarity.icon);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(addon.rarity.color + addon.name);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.DARK_GRAY + addon.rarity.toString() + " Augment");
		lore.add("");
		for(String line : addon.desc.split("\\\\n")) {
			if(line.charAt(0) == '+') {
				lore.add("&a" + line);
			}else if(line.charAt(0) == '-') {
				lore.add("&c" + line);
			}else {
				lore.add("&7" + line);
			}
		}
		lore.add("");
		
		KillerData data = user.killerData.get(type.name());
		if(data.level >= addon.rarity.level) {
			long discount = OfferingManager.isOfferingActive(Offering.EXTERMINATOR_MOD_KIT) * 10;
			if(discount > 0) {
				lore.add("&7Costs &e" + FormatUtil.formatLong(addon.rarity.getCost()) + " Blood&7 to per match used. &d" + discount + "% OFF");
			}else {
				lore.add("&7Costs &e" + FormatUtil.formatLong(addon.rarity.getCost()) + " Blood&7 to per match used.");
			}
			
			lore.add("");
			if(data.selectedAugments.contains(addon.id)) {
				lore.add("&cClick to remove");
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}else {
				lore.add("&7Click to equip");
			}
		}else {
			lore.add("&cKiller &e&nLevel " + addon.rarity.level + "&c required.");
		}
		
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
}
