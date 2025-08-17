package net.blixate.deadlight.gui;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import net.blixate.deadlight.player.DLUser;

public interface GuiListener {
	
	/**
	 * Small utility class for simply opening another (static) inventory after a click.
	 */
	public class OpenInventory implements GuiListener {
		GuiInventory nextInventory;
		public OpenInventory(GuiInventory inv) {
			this.nextInventory = inv;
		}
		
		@Override
		public void click(DLUser user, GuiInventory inventory) {
			nextInventory.update();
			user.openInventory(this.nextInventory);
		}
	}
	
	public class DoNothing implements GuiListener {
		@Override
		public void click(DLUser user, GuiInventory inventory) {
			
		}
	}
	
	/** Called when the user clicks the inventory (only for left-click)
	 * @param user who clicked the inventory
	 * @param inventory which was clicked, equivalent to {@link DLUser#getGui()} */
	public void click(DLUser user, GuiInventory inventory);
	
	/** Called when the user right-clicks the inventory (as opposed to left click)
	 * @param user who clicked the inventory
	 * @param inventory which was clicked, equivalent to {@link DLUser#getGui()}*/
	public default void rightClick(DLUser user, GuiInventory inventory) {}
	
	/** Called whenever the inventory is clicked, and returns the {@link ClickType}
	 * @param user who clicked the inventory
	 * @param inventory which was clicked, equivalent to {@link DLUser#getGui()}
	 * @param click the type of click.
	 * @see ClickType */
	public default void customClick(DLUser user, GuiInventory inventory, ClickType click) {}
	
	
	public default GuiItem toGuiItem(ItemStack item) {
		return new GuiItem() {

			@Override
			public void click(DLUser user, GuiInventory inventory) {
				GuiListener.this.click(user, inventory);
			}

			@Override
			public ItemStack item() {
				return item;
			}
			
		};
	}
}
