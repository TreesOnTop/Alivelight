package net.blixate.deadlight.kit;

import org.bukkit.Material;

import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;

public class WIPGui extends GuiInventory {

	public WIPGui(GuiInventory kit) {
		super("Work in Progress", 5);
		setBackArrow(36, kit);
		items.put(22, GuiItem.blank(Material.BARRIER, "&cNot implemented."));
	}
	
}
