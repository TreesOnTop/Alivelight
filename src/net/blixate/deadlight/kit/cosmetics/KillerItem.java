package net.blixate.deadlight.kit.cosmetics;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;

public enum KillerItem {
	DEFAULT(Material.WOODEN_SWORD, "Crude Knife", "A blade with a crude edge."),
	MACHETE(Material.NETHERITE_SWORD, "Machete", "Seems to be designed for cutting!", 5000, "$1 was sliced by The Killer."),
	SCYTHE(Material.NETHERITE_HOE, "Reaper's Scythe", "All the fallen souls can be safe now.", 5500),
	GRAVEDIGGER(Material.NETHERITE_SHOVEL, "Gravedigger", "Their bodies will be buried.", 5000, "$1 was buried by The Killer."),
	SHIELD(Material.SHIELD, "Best Offence", "Eat grass!", 7500),
	JERRY(Material.SALMON, "jerry", "hello i am jerry. im pretty cool.", 30000, "jerry impressed $1 to death."),
	BOB(Material.PUFFERFISH, "bob", "hey! have you seen jerry? hes really cool!", 30000, "bob exploded all over $1."),
	LARRY(Material.COD, "larry", "i'm not cool at all. sorry if your bummed out.", 1, "larry disappointed $1 to death."),
	VALENTINE(Material.NETHERITE_PICKAXE, "My Bloody Valentine", "1981", 5500, "$1's heart was split in two."),
	EXCALIBUR(Material.GOLDEN_SWORD, "Excalibur", "The Holy Sword", 7500, "$1 has been struck down by the holy sword EXCALIBUR!"),
	LE_BONK(Material.STICK, "Le Bonk", "no!!! you get bonk!!", 6000, "$1 was bonked by The Killer."),
	MATILDA(Material.IRON_AXE, "Matilda", null, 6000, "$1 was chopped in half."),
	HEAVY_BOOK(Material.BOOK, "Heavy Book", "This book includes the answer to all your questions.\nUnfortunately, someone glued the pages together.", 6500, "$1 was schooled by The Killer."),
	BRICK(Material.BRICK, "Normal Average Brick", "this brick seems average", 4000, "$1 was smacked by a normal average brick."),
	EVIL_BRICK(Material.NETHERITE_INGOT, "Evil Brick", "this brick seems weird", 7500, "$1 was smacked by an evil brick."),
	BURNT_BRICK(Material.NETHER_BRICK, "Burnt Brick", "this brick is burnt", 7500, "$1 was smacked by a burnt brick."),
	SHINY_BRICK(Material.IRON_INGOT, "Shiny Brick", "this brick seems shiny", 7500, "$1 was smacked by a shiny brick."),
	FANCY_BRICK(Material.GOLD_INGOT, "Fancy Brick", "this brick seems- ooo fancy!", 10000, "$1 was smacked by a fancy brick."),
	BAMBOO_STICK(Material.BAMBOO, "Revenge of the Cut", null, 5500, "$1 was sent to Gensokyo."),
	FEMUR(Material.BONE, "Someone's Femur", "Who does this belong to?", 5500, "$1 was bamboozled by someone's femur."),
	FEATHER(Material.FEATHER, "The Pen", "This is useless without the ink.", 6500, "$1 found out the pen is mightier than the sword."),
	INK(Material.INK_SAC, "The Ink", "This is useless without the pen.", 6500, "$1 saw what was mightier than the bow."),
	MIRAGE_BLADE(Material.DIAMOND_SWORD, "Mirage Blade", "I am the storm that is approaching.", 20000, "$1 was given a taste of true power."),
	HOT_ROD(Material.BLAZE_ROD, "Hot Rod", "Fresh outta the oven", 4000, "$1 was burnt by The Killer."),
	CIGAR(Material.TORCH, "Lit Cigar", "Bad for your lungs, but full of class.", 2000),
	HONESTY(Material.GOLDEN_AXE, "Honesty", "Mecury and the Woodman", 5000, "$1 wasn't an honest Woodman."),
	SHOW_SHOVEL(Material.IRON_SHOVEL, "Snow Shovel", "Snow more messing around!", 4500),
	PIE(Material.PUMPKIN_PIE, "What flavor?", "Pie flavor.", 1234, "$1 was pied by The Killer."),
	CRESCENT(Material.IRON_HOE, "Crescent Moon", "Those who aim for the truth, and fight to achieve it,\nare the harbingers of the change that will\nbefall this world; Justice shall change the tide of\nour future, in one way or another.", 10000),
	RIZZLER(Material.GOLDEN_HOE, "The Rizzler", "You got that rizz yk?", 100000, "$1 got rizzed up by The Killer."),
	ILY(Material.POPPY, "Love", "I love you <3", 459520, "$1 got smoothered with love."),
	CANE(Material.WOODEN_HOE, "Axel's Cane", "This cane makes you feel like a baller.", 500000, "$1 was caught lackin'."),
	BOOT(Material.NETHERITE_BOOTS, "Dark Greaves", "Destroy them, stomp out the last embers from their souls...", 12500, "$1 was kicked to death by The Killer!");
	
	public Material material;
	public String name;
	public String description = null;
	public String deathMessage;
	public int cost;
	
	KillerItem(Material mat, String name) {
		this(mat, name, 0);
	}
	
	KillerItem(Material mat, String name, String desc) {
		this(mat, name, desc, 0);
	}
	
	KillerItem(Material mat, String name, int cost) {
		material = mat;
		this.name = name;
		this.cost = cost;
		this.deathMessage = null;
	}
	
	KillerItem(Material mat, String name, String desc, int cost) {
		this(mat, name, cost);
		this.description = desc;
	}
	
	KillerItem(Material mat, String name, String desc, int cost, String customDeathMessage) {
		this(mat, name, desc, cost);
		this.deathMessage = customDeathMessage;
	}
	
	public ItemStack getItem(DLUser user) {
		if(material == null) {
			material = Material.STICK;
		}
		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(FormatUtil.color("&6" + name));
		List<String> lore = Lists.newArrayList();
		lore.add("&8Killer Weapon");
		lore.add("");
		if(description != null) {
			String[] desc = description.split("\n");
			for(String s : desc) lore.add("&7" + s);
		}
		if(!user.purchasedKillerItems.contains(this.name()) && cost != 0) {
			lore.add(FormatUtil.color("&7Click to purchase for &e" + FormatUtil.formatLong(cost) + " Blood&7."));
		}
		else {
			if(user.killerItem == this) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				lore.add(FormatUtil.color("&aSelected"));
			}
			else {
				lore.add(FormatUtil.color("&7Click to select"));
			}
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
}
