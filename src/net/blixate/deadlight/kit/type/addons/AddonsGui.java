package net.blixate.deadlight.kit.type.addons;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.kit.type.AbilityGui;
import net.blixate.deadlight.kit.type.KillerTypes;
import net.blixate.deadlight.player.DLUser;

public class AddonsGui extends GuiInventory {
	DLUser user;
	KillerTypes killer;
	AbilityGui prev;
	
	public AddonsGui(DLUser user, KillerTypes killer, AbilityGui prev) {
		super(killer.getName() + " > Augments", 5);
		this.user = user;
		this.killer = killer;
		this.prev = prev;
	}
	
	public void construct() {
		setBackArrow(36, prev);
		for(int i = 37; i < 45; i++) {
			setItem(i, GuiItem.blank(Material.RED_STAINED_GLASS_PANE, "&0"));
		}
		ConfigurationSection addons = killer.getData().getConfigurationSection("addons");
		if(addons != null) {
			int i = 0;
			for(String key : addons.getKeys(false)) {
				this.setItem(i, new AddonItem(user, killer, addons.getConfigurationSection(key)));
				i++;
			}
		}else {
			this.setItem(13+9, GuiItem.blank(Material.BARRIER, "&cNo Augments available."));
		}
	}
}
