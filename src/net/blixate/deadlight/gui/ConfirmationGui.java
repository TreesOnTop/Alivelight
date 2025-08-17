package net.blixate.deadlight.gui;

import org.bukkit.Material;

import net.blixate.deadlight.player.DLUser;

public class ConfirmationGui extends GuiInventory {
	
	GuiListener onAccept;
	GuiListener onDecline;
	
	boolean flip = false;
	
	public ConfirmationGui(String title, GuiListener onAccept) {
		this(title, onAccept, null);
	}
	
	public ConfirmationGui(String title, GuiListener onAccept, GuiListener onDecline, boolean flip) {
		super(title, 3);
		
		this.onAccept = onAccept;
		this.onDecline = onDecline;
		this.flip = flip;

		if(onDecline == null) {
			/* Use default decline method */
			this.onDecline = new GuiListener() {
				public void click(DLUser user, GuiInventory inventory) {
					user.getPlayer().closeInventory(); // close the player's inventory
				}
			};
		}
	}
	
	public ConfirmationGui(String title, GuiListener onAccept, GuiListener onDecline) {
		super(title, 3);
		
		this.onAccept = onAccept;
		this.onDecline = onDecline;
		
		if(onDecline == null) {
			/* Use default decline method */
			this.onDecline = new GuiListener() {
				public void click(DLUser user, GuiInventory inventory) {
					user.getPlayer().closeInventory();
				}
			};
		}
	}
	
	public void construct() {
		//this.fill(GuiItem.blank(Material.BLACK_STAINED_GLASS_PANE, "&0"));
		if(flip) {
			this.setItem(15, onDecline, GuiIcon.createItemStack(Material.RED_STAINED_GLASS_PANE, "&cDecline"));
			this.setItem(11, onAccept, GuiIcon.createItemStack(Material.GREEN_STAINED_GLASS_PANE, "&aAccept"));
		}else {
			this.setItem(11, onDecline, GuiIcon.createItemStack(Material.RED_STAINED_GLASS_PANE, "&cDecline"));
			this.setItem(15, onAccept, GuiIcon.createItemStack(Material.GREEN_STAINED_GLASS_PANE, "&aAccept"));
		}
	}
	
}
