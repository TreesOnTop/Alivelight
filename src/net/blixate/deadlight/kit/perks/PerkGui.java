package net.blixate.deadlight.kit.perks;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.gui.GuiListener;
import net.blixate.deadlight.player.Alignment;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;
import net.md_5.bungee.api.ChatColor;

public class PerkGui extends GuiInventory {
	public static Material[] PERK_TIER_MATERIAL = {
			Material.YELLOW_CONCRETE,
			Material.ORANGE_CONCRETE,
			Material.GREEN_CONCRETE
	};
	
	enum ListVisibility {
		ALL, OWNED, UNOWNED;
		
		public ItemStack item() {
			ItemStack item = new ItemStack(Material.OAK_SIGN);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("&6Toggle Visibility");
			List<String> lore = new ArrayList<>();
			for(ListVisibility vis : ListVisibility.values()) {
				if(this == vis) {
					lore.add(" &a✔ " + vis.name());
				}else {
					lore.add(" &7• " + vis.name());
				}
			}
			lore.add("");
			lore.add("&7Click to change");
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		}
		
		public boolean shouldShowPerk(DLUser user, PerkRegistry perk) {
			switch(this) {
			case ALL:
				return true;
			case OWNED:
				return user.hasPurchasedPerk(perk.id);
			case UNOWNED:
				return !user.hasPurchasedPerk(perk.id);
			default:
				return false;
			}
		}
		
		public ListVisibility next() {
			// definitely make this look better
			switch(this) {
			case ALL:
				return ListVisibility.OWNED;
			case OWNED:
				return ListVisibility.UNOWNED;
			case UNOWNED:
				return ListVisibility.ALL;
			}
			return null;
		}
	}
	
	/* These two names are confusing
	 *  PERK_UNPURCHASABLE -> The perk cannot be purchased (not enough funds)
	 *  PERK_UNPURCHASED -> The perk hasn't been purchased (but can be!)
	 */
	
	public static Material PERK_UNPURCHASABLE = Material.RED_CONCRETE_POWDER;
	public static Material PERK_UNPURCHASED = Material.GRAY_CONCRETE_POWDER;
	
	private static long[] PERK_TIER_COST = {
			10000L,
			15000L,
			20000L,
	};
	
	ListVisibility visibility = ListVisibility.ALL;
	
	class PerkItem implements GuiItem {
		PerkRegistry perk;
		DLUser user;

		public PerkItem(PerkRegistry perk, DLUser user) {
			this.perk = perk;
			this.user = user;
		}
		public void click(DLUser user, GuiInventory inventory) {
			if(perk.disabled) {
				user.send("perk_disabled", perk.name);
				return;
			}
			if(!perk.hasImplementation()) {
				user.send("perk_unimplemented", perk.name);
				return;
			}
			if(!user.hasPurchasedPerk(perk.id)) {
				long cost = PERK_TIER_COST[0];
				if(user.getBlood() >= cost) {
					// purchase perk
					user.setPerkTier(perk.id, 1);
					user.blood = user.blood.subtract(BigInteger.valueOf(cost));
					user.send("perk_purchased", FormatUtil.formatLong(cost));
					inventory.update();
					user.playSound(Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1);
					user.addExp(25);
				}else {
					user.send("perk_no_funds");
				}
				return;
			}
			if(user.hasPerk(perk)) {
				user.removePerk(perk);
			}else {
				user.addPerk(perk);
			}
			inventory.update();
		}
		
		@Override
		public void rightClick(DLUser user, GuiInventory inventory) {
			if(perk.disabled) {
				user.send("perk_disabled", perk.name);
				return;
			}
			
			if(user.hasPurchasedPerk(perk.id)) {
				int tier = user.getPerkTier(perk.id);
				if(tier < 3) {
					tier++;
					// buy the tier
					long bloodCost = getBloodCost(tier);
					if(user.getBlood() < bloodCost) {
						// not enough blood!
						user.send("perk_tier_no_funds");
						return;
					}
					user.blood = user.blood.subtract(BigInteger.valueOf(bloodCost));
					//user.souls = user.souls.subtract(BigInteger.valueOf(soulsCost));
					user.setPerkTier(perk.id, tier);
					user.send("perk_upgraded", ""+tier, FormatUtil.formatLong(bloodCost));//, FormatUtil.formatLong(soulsCost));
					user.playSound(Sound.BLOCK_ANVIL_USE, 1.2);
					// update inventory
					inventory.update();
					user.addExp(25);
				}else {
					// print error
					user.send("perk_max_tier");
				}
			}
		}
		
		public ItemStack item() {
			ItemStack item;
			int tier = user.getPerkTier(perk.id);
			var name = perk.name;
			boolean hasFunds = (user.getBlood() >= PERK_TIER_COST[0]);
			/* Is this perk unimplemented? (Does it's class exist?) */
			if(!perk.hasImplementation() || perk.disabled) {
				item = new ItemStack(Material.BARRIER, 1);
				name = ChatColor.RED + perk.name;
			}
			else if(user.hasPurchasedPerk(perk.id)) {
				item = new ItemStack(PERK_TIER_MATERIAL[tier-1], 1);
				name = ChatColor.GREEN + perk.name;
			}else {
				item = new ItemStack(hasFunds ? PERK_UNPURCHASED : PERK_UNPURCHASABLE, 1);
				name = ChatColor.GOLD + perk.name;
			}
			name += " &6&l" + "I".repeat(tier);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			// Add description
			List<String> lore = new ArrayList<String>();
			List<String> description = new ArrayList<>();
			lore.add("&8"+perk.alignment.toString()+" Perk");
			lore.add("");
			var desc = "";
			/* Default to tier 1 if the player hasn't purchased this perk */
			if(tier == 0) desc = perk.description.replace("$1", perk.getTierProperty(1) + "/" + perk.getTierProperty(2) + "/" + perk.getTierProperty(3));
			else desc = perk.description.replace("$1", perk.getTierProperty(tier));
			
			for(String s : desc.split("\n")) {
				description.add("&d" + s);
			}
			lore.addAll(description);
			lore.add("");
			if(perk.disabled) {
				lore.add("&cThis perk cannot be equipped.");
				lore.add("&7(Disabled)");
			}
			else if(!perk.hasImplementation()) {
				lore.add("&cThis perk cannot be equipped.");
				lore.add("&7(Not implemented)");
			}
			else if(!user.hasPurchasedPerk(perk.id)) {
				lore.add("&8&m• • • • • • • • • • • • • • • • • • • • • • • •");
				lore.add(((hasFunds)?"&7":"&c")+"Left-Click to buy for &e"+FormatUtil.formatLong(PERK_TIER_COST[0])+" Blood");
			} else {
				if(user.hasPerk(perk)) {
					shiny(meta);
					lore.add("&a&lEQUIPPED");
				}
				lore.add("&8&m• • • • • • • • • • • • • • • • • • • • • • • •");
				lore.add(user.hasPerk(perk) ? "&7Left-Click to &eunequip&7." : "&7Left-Click to &eequip&7.");
				if(tier < 3) {
					long bloodCost = getBloodCost(tier+1);
					if(user.getBlood() >= bloodCost) {// && user.getSouls() >= soulsCost) {
						// we have enough!
						lore.add("&7Right-Click to &eupgrade&7 to &eTier "+(tier+1)+"&7!");
						lore.add("&a ✔ &e"+FormatUtil.formatLong(bloodCost) + " &4Blood");
					}else {
						lore.add("&cTo upgrade, you need to:");
						if(user.getBlood() < bloodCost) {
							lore.add("&c ✖ &e"+FormatUtil.formatLong(bloodCost) + " &4Blood");
						}else {
							lore.add("&a ✔ &e"+FormatUtil.formatLong(bloodCost) + " &4Blood");
						}
					}
				}
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		}
		
		public long getBloodCost(int tier) {
			return PERK_TIER_COST[tier-1];
		}
	}
	
	Alignment perkAlignment;
	DLUser user;
	
	GuiInventory gui;
	
	public PerkGui(DLUser user, Alignment alignment, GuiInventory gui) {
		super("Your Perks", 5);
		this.user = user;
		this.perkAlignment = alignment;
		this.items = new HashMap<>();
		this.gui = gui;
		this.inventory = create();
	}
	
	public void construct() {
		// clear the menu
		this.items.clear();
		this.inventory.clear();
		for(int i = 36; i < 36+9; i++) {
			setItem(i, GuiItem.blank(Material.BLACK_STAINED_GLASS_PANE, "&0"));
		}
		setBackArrow(36, gui);
		int i = 0;
		if(perkAlignment == Alignment.SURVIVOR) {
			for(PerkRegistry perk : Deadlight.getPerkManager().getSurvivorPerks()) {
				if(!visibility.shouldShowPerk(user, perk)) {
					continue;
				}
				items.put(i++, new PerkItem(perk, user));
			}
			constructEquippedPerks(user.getSurvivorPerks());
		}else if(perkAlignment == Alignment.KILLER) {
			for(PerkRegistry perk : Deadlight.getPerkManager().getKillerPerks()) {
				if(!visibility.shouldShowPerk(user, perk)) {
					continue;
				}
				items.put(i++, new PerkItem(perk, user));
			}
			constructEquippedPerks(user.getKillerPerks());
		}
		this.setItem(36+7, new GuiListener() {

			@Override
			public void click(DLUser user, GuiInventory inventory) {
				PerkGui gui = (PerkGui)inventory;
				gui.visibility = gui.visibility.next();
				gui.update();
			}
			
		}.toGuiItem(visibility.item()));
		
	}
	
	public void constructEquippedPerks(List<String> perkList) {
		for(int i = 38; i < 38+4; i ++) {
			items.put(i, GuiItem.blank(Material.ORANGE_STAINED_GLASS_PANE, "&cEmpty Perk Slot"));
		}
		Material perkMaterial = (perkList.size() == 4) ? Material.LIME_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE;
		int i = 38;
		for(final String perk : perkList) {
			final PerkRegistry registry = Deadlight.getPerkManager().getRegistry(perk);
			
			items.put(i, new GuiItem() {
				public ItemStack item() {
					int tier = user.getPerkTier(perk);
					ItemStack stack = new ItemStack(perkMaterial);
					ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("&a&l" + registry.name + " &6&l" + "I".repeat(tier));
					List<String> lore = new ArrayList<String>();
					List<String> description = new ArrayList<>();
					lore.add("&8"+registry.alignment.toString()+" Perk");
					lore.add("");
					var desc = "";
					/* Default to tier 1 if the player hasn't purchased this perk */
					if(tier == 0) desc = registry.description.replace("$1", registry.getTierProperty(1) + "/" + registry.getTierProperty(2) + "/" + registry.getTierProperty(3));
					else desc = registry.description.replace("$1", registry.getTierProperty(tier));
					
					for(String s : desc.split("\n")) {
						description.add("&d" + s);
					}
					lore.addAll(description);
					lore.add("");
					lore.add("&eClick to remove from your kit.");
					meta.setLore(lore);
					stack.setItemMeta(meta);
					return stack;
				}

				@Override
				public void click(DLUser user, GuiInventory inventory) {
					if(user.hasPerk(registry)) {
						user.removePerk(registry);
					}
					inventory.update();
				}
			});
			i++;
		}
	}
	
}
