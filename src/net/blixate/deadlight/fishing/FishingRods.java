package net.blixate.deadlight.fishing;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.TimeKeeper;
import net.blixate.deadlight.kit.offerings.Offering;
import net.blixate.deadlight.kit.offerings.OfferingManager;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.random.DeadlightLootTable;
import net.md_5.bungee.api.ChatColor;

public enum FishingRods {
	BASIC("&aBasic", "More likely to get Blood, Souls and XP.", 0.5f, "basic"),
	ELITE("&6Elite", "More likely to drop Items.", 1, "elite"),
	LEGEND("&5Legend", "More likely to drop Offerings.", 3, "legend");
	
	public static int[] uses = {50, 100, 250};
	public static double[] discount = { 1f, 0.88f, 0.75f };
	public static double[] discountFrenzy = { 0.9f, 0.8f, 0.7f };
	  
	String name;
	String lore;
	float costPerUse;
	String table;
	
	FishingRods(String name, String desc, float cpm, String table) {
		this.name = name;
		this.lore = desc;
		this.costPerUse = cpm;
		this.table = table;
	}
	
	/** Get the drop table for this rod. */
	public DeadlightLootTable getTable() {
		return Deadlight.fishingDrops.getTable(this);
	}
	
	public String getName() {
		return name;
	}
	
	public ItemStack get() {
		ItemStack item = new ItemStack(Material.FISHING_ROD, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(FormatUtil.color(name));
		meta.setLore(Lists.newArrayList(ChatColor.YELLOW + lore));
		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		item.setItemMeta(meta);
		return item;
	}
	
	public ItemStack icon(int i) {
		ItemStack item = new ItemStack(Material.FISHING_ROD, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(FormatUtil.color(name));
		List<String> lore = new ArrayList<>();
		lore.add("&8Fishing Rod");
		lore.add("");
		lore.add("&7"+uses[i]+" Bait");
		lore.add("&7Rent for &e"+FormatUtil.formatLong(cost(i))+" Souls&7"+(discount(i)!=1f?" &d"+(int)((1f-discount(i))*100)+"% OFF!":""));
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public long cost(int i) {
		return (long) (costPerUse * discount(i) * uses[i]);
	}
	
	public double discount(int i) {
		double offeringDiscount = 0;
		if(OfferingManager.isOfferingActive(Offering.BIG_BASS) != 0) {
			offeringDiscount = 0.05 * OfferingManager.isOfferingActive(Offering.BIG_BASS);
		}
		double totalDiscount = 0;
		if(TimeKeeper.isFishingFrenzyEvent()) {
			totalDiscount = discountFrenzy[i] - offeringDiscount;
		}
		totalDiscount = discount[i] - offeringDiscount;
		return totalDiscount >= 1 ? 1 : totalDiscount;
	}
}
