package net.blixate.deadlight.kit.cosmetics;

import org.bukkit.Material;

import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.gui.GuiListener;
import net.blixate.deadlight.kit.KitGui;
import net.blixate.deadlight.player.Alignment;
import net.blixate.deadlight.player.DLUser;

public class CosmeticGUI extends GuiInventory {
	
	private Alignment alignment;
	private KitGui gui;
	
	public CosmeticGUI(KitGui gui, Alignment alignment) {
		super("Your Customization", 3);
		this.alignment = alignment;
		this.gui = gui;
	}
	
	public void construct() {
		if(alignment == Alignment.KILLER) {
			setItem(12, GuiItem.toGuiItem(new GuiListener() {
				public void click(DLUser user, GuiInventory inventory) {
					CosmeticMenuBuilder.createKillEffectsInventory(user);
				}
			}, Material.BLAZE_ROD, "&6Kill Effects", "&7Select a &eKill Effect"));
			setItem(14, GuiItem.toGuiItem(new GuiListener() {
				public void click(DLUser user, GuiInventory inventory) {
					CosmeticMenuBuilder.createKillerItemsInventory(user);
				}
			}, Material.STONE_SWORD, "&6Weapon", "&7Select a &eKiller Weapon"));
		}else if(alignment == Alignment.SURVIVOR) {
			setItem(13, GuiItem.toGuiItem(new GuiListener() {

				@Override
				public void click(DLUser user, GuiInventory inventory) {
					CosmeticMenuBuilder.createSurvivorColorInventory(user);
				}}, Material.RED_WOOL, "&6Blood Trails", "&7Select a &eBlood Trail"));
		}
		setBackArrow(18, gui);
	}

}
