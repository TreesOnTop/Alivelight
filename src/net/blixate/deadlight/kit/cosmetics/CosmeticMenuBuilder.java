package net.blixate.deadlight.kit.cosmetics;

import java.math.BigInteger;

import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import net.blixate.deadlight.gui.GuiBuilder;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.kit.KitGui;
import net.blixate.deadlight.player.Alignment;
import net.blixate.deadlight.player.DLUser;

public class CosmeticMenuBuilder {
	
	public static void createKillEffectsInventory(DLUser user) {
		GuiBuilder builder = new GuiBuilder("Select Kill Effect", 3);
		int i = 0;
		for(KillEffect effect : KillEffect.values()) {
			builder.setGuiItem(i, new GuiItem() {
				public void click(DLUser user, GuiInventory inventory) {
					if(!user.purchasedKillEffects.contains(effect.name()) && effect.cost > 0) {
						long cost = effect.cost;
						if(user.getBlood() >= cost) {
							user.purchasedKillEffects.add(effect.name());
							user.blood = user.blood.subtract(BigInteger.valueOf(cost));
							user.send("purchased", "Kill Effect", effect.properName());
							inventory.update();
							user.playSound(Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1);
						}else {
							user.send("no_funds");
						}
						return;
					}
					if(user.killEffect == effect) {
						user.send("already_selected", effect.properName());
						return;
					}
					user.killEffect = effect;
					user.send("selected", effect.properName());
					inventory.update();
				}
				public ItemStack item() {return effect.getItem(user);}
			});
			i++;
		}
		builder.addBackArrow(new CosmeticGUI(new KitGui(user), Alignment.KILLER));
		user.openInventory(builder.build());
	}
	
	public static void createKillerItemsInventory(DLUser user) {
		GuiBuilder builder = new GuiBuilder("Select Killer Item", 5);
		int i = 0;
		for(KillerItem effect : KillerItem.values()) {
			builder.setGuiItem(i, new GuiItem() {
				public void click(DLUser user, GuiInventory inventory) {
					if(!user.purchasedKillerItems.contains(effect.name()) && effect.cost > 0) {
						long cost = effect.cost;
						if(user.getBlood() >= cost) {
							user.purchasedKillerItems.add(effect.name());
							user.blood = user.blood.subtract(BigInteger.valueOf(cost));
							user.send("purchased", "Weapon", effect.name);
							inventory.update();
							user.playSound(Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1);
						}else {
							user.send("no_funds");
						}
						return;
					}
					if(user.killerItem == effect) {
						user.send("already_selected", effect.name);
						return;
					}
					user.killerItem = effect;
					user.send("selected", effect.name);
					inventory.update();
				}
				public ItemStack item() {return effect.getItem(user);}
			});
			i++;
		}
		builder.addBackArrow(new CosmeticGUI(new KitGui(user), Alignment.KILLER));
		user.openInventory(builder.build());
	}
	
	public static void createSurvivorColorInventory(DLUser user) {
		GuiBuilder builder = new GuiBuilder("Select Blood Color", 3);
		int i = 0;
		for(BloodColor color : BloodColor.values()) {
			builder.setGuiItem(i, new GuiItem() {
				@Override
				public void click(DLUser user, GuiInventory inventory) {
					if(!user.purchasedBloodColors.contains(color.name()) && color.cost > 0) {
						long cost = color.cost;
						if(user.getBlood() >= cost) {
							user.purchasedBloodColors.add(color.name());
							user.blood = user.blood.subtract(BigInteger.valueOf(cost));
							user.send("purchased", "Blood Trail", color.properName());
							inventory.update();
							user.playSound(Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1);
						}else {
							user.send("no_funds");
						}
						return;
					}
					if(user.bloodColor == color) {
						user.send("already_selected", color.properName());
						return;
					}
					user.bloodColor = color;
					user.send("selected", color.properName());
					inventory.update();
				}

				@Override
				public ItemStack item() {
					return color.getItem(user);
				}
			});
			i++;
		}
		builder.addBackArrow(new CosmeticGUI(new KitGui(user), Alignment.SURVIVOR));
		user.openInventory(builder.build());
	}
}
