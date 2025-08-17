package net.blixate.deadlight.kit;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.gui.GuiListener;
import net.blixate.deadlight.kit.cosmetics.CosmeticGUI;
import net.blixate.deadlight.kit.items.ItemGui;
import net.blixate.deadlight.kit.items.Items;
import net.blixate.deadlight.kit.offerings.OfferingManager;
import net.blixate.deadlight.kit.offerings.OfferingsGui;
import net.blixate.deadlight.kit.perks.PerkGui;
import net.blixate.deadlight.kit.type.AbilityGui;
import net.blixate.deadlight.player.Alignment;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;

public class KitGui extends GuiInventory {
	
	private static final Material PERK_MATERIAL = Material.NETHER_STAR;
	private static final Material ABILITY_MATERIAL = Material.IRON_SWORD;
	private static final Material INVENTORY_MATERIAL = Material.CHEST;
	private static final Material COSMETIC_MATERIAL = Material.ENDER_CHEST;
	
	private static final String NO_PERKS_EQUIPPED = "&cNo perks equipped.";
	private static final String EQUIPPED_PERKS = "&aSelected:\n";
	private static final String SELECTED = "&aSelected: &e";
	
	private static final int COSMETICS_LEVEL_REQUIREMENT = 20;
	
	private DLUser user;
	
	public KitGui(DLUser user) {
		super("Prepare your loadout", 3);
		this.user = user;
	}
	
	public void construct() {		
		items.put(1, new GuiListener.OpenInventory(new ItemGui(user, this)).toGuiItem(chestItem()));
		items.put(10, new GuiListener.OpenInventory(new PerkGui(user, Alignment.SURVIVOR, this)).toGuiItem(survivorPerk("&dSurvivor Perks")));
		if(user.getPrestige() == 0 && user.getLevel() < COSMETICS_LEVEL_REQUIREMENT) {
			items.put(19, cosmeticsNotUnlocked("&6Survivor Cosmetics"));
		}else {
			items.put(19, new GuiListener.OpenInventory(new CosmeticGUI(this, Alignment.SURVIVOR)).toGuiItem(cosmetics("&6Survivor Cosmetics")));
		}
		items.put(7, new GuiListener.OpenInventory(new AbilityGui(user, this)).toGuiItem(typeItem("&dKiller Type")));
		items.put(16, new GuiListener.OpenInventory(new PerkGui(user, Alignment.KILLER, this)).toGuiItem(killerPerk("&dKiller Perks")));
		
		if(user.getPrestige() == 0 && user.getLevel() < COSMETICS_LEVEL_REQUIREMENT) {
			items.put(25, cosmeticsNotUnlocked("&6Killer Cosmetics"));
		}else {
			items.put(25, new GuiListener.OpenInventory(new CosmeticGUI(this, Alignment.KILLER)).toGuiItem(cosmetics("&6Killer Cosmetics")));
		}
		
		for(int i = 2; i < 9 * 3; i += 9)
			items.put(i, GuiItem.blank(Material.GREEN_STAINED_GLASS_PANE, "&aSurvivor"));
		
		for(int i = 6; i < 9 * 3; i += 9)
			items.put(i, GuiItem.blank(Material.RED_STAINED_GLASS_PANE, "&cKiller"));
		items.put(13, new GuiListener.OpenInventory(new OfferingsGui(user, this)).toGuiItem(offeringsItem()));
	}
	
	private ItemStack chestItem() {
		if(user.getEquippedItem() != null) {
			return item(INVENTORY_MATERIAL, "&dSurvivor Inventory", SELECTED + Items.valueOf(user.getEquippedItem()).name + "\n\n&7Click to manage your inventory.");
		}else {
			return item(INVENTORY_MATERIAL, "&dSurvivor Inventory", "&7Click to manage your inventory.");
		}
	}
	
	private ItemStack typeItem(String title) {
		return item(ABILITY_MATERIAL, title, SELECTED + user.killerType.getName() + "\n\n&7Click to customize your Killer.");
	}
	
	private ItemStack cosmetics(String title) {
		return item(COSMETIC_MATERIAL, title, "&7Click to change your cosmetics.");
	}
	
	private GuiItem cosmeticsNotUnlocked(String title) {
		return GuiItem.toGuiItem((user, inv) -> {
			user.send("cosmetics_locked", COSMETICS_LEVEL_REQUIREMENT + "");
		}, COSMETIC_MATERIAL, title, "&cReach level " + COSMETICS_LEVEL_REQUIREMENT + " to unlock!");
	}
	
	public ItemStack killerPerk(String title) {
		String lore = "";
		if(user.getKillerPerks().isEmpty()) {
			lore = NO_PERKS_EQUIPPED;
		}else {
			lore = EQUIPPED_PERKS;
			for(String perk : user.getKillerPerks()) {
				int tier = user.getPerkTier(perk);
				lore += "&e ● " + Deadlight.getPerkManager().getKillerPerk(perk).name + " &6&l" + "I".repeat(tier) + "\n";
			}
		}
		lore += "\n&7Click to change your perks.";
		return item(PERK_MATERIAL, title, lore);
	}
	
	public ItemStack survivorPerk(String title) {
		String lore = "";
		if(user.getSurvivorPerks().isEmpty()) {
			lore = NO_PERKS_EQUIPPED;
		}else {
			lore = EQUIPPED_PERKS;
			for(String perk : user.getSurvivorPerks()) {
				int tier = user.getPerkTier(perk);
				lore += "&e ● " + Deadlight.getPerkManager().getSurvivorPerk(perk).name + " &6&l" + "I".repeat(tier) + "\n";
			}
		}
		lore += "\n&7Click to change your perks";
		return item(PERK_MATERIAL, title, lore);
	}
	
	public ItemStack offeringsItem() {
		String lore = "";
		lore += "&7Offerings are global boosts that\n&7affect everyone on the server!\n\n";
		lore += "&aBlood Boost &ex" + OfferingManager.getBloodBoost() + "\n";
		lore += "&aXP Boost &ex" + OfferingManager.getXPBoost() + "\n\n";
		lore += "&7Click to select an Offering to burn.";
		return item(Material.FIRE_CHARGE, "&6Offerings", lore);
	}

	public static ItemStack item(Material mat, String title, String lore) {
		ItemStack item = new ItemStack(mat, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(FormatUtil.color(title));
		ArrayList<String> loreList = new ArrayList<>();
		String[] loreSplit = lore.split("\n");
		for(String loreLine : loreSplit) {
			loreList.add(FormatUtil.color(loreLine));
		}
		meta.setLore(loreList);
		item.setItemMeta(meta);
		return item;
	}
}
