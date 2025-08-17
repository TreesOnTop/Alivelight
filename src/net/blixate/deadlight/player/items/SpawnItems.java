package net.blixate.deadlight.player.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.util.FormatUtil;
/** Registers what items the player will have at spawn. */
public class SpawnItems {
	
	public static class SpawnItem {
		private String name;
		private Material mat;
		
		public SpawnItem(Material mat, String name) {
			this.name = FormatUtil.color(name);
			this.mat = mat;
		}
		
		public ItemStack get() {
			ItemStack item = new ItemStack(mat, 1);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			item.setItemMeta(meta);
			return item;
		}
		
		public boolean is(ItemStack item) {
			return item.getItemMeta().getDisplayName().equals(name) && item.getType().equals(mat);
		}
	}

	public static SpawnItem leaveMatch = new SpawnItem(Material.BARRIER, "&dLeave Match &7(Right-click)");
	public static SpawnItem kit = new SpawnItem(Material.SLIME_BALL, "&dCustomize Kit &7(Right-click)");
	public static SpawnItem joinQueue = new SpawnItem(Material.GRAY_DYE, "&dJoin Queue &7(Right-click)");
	public static SpawnItem leaveQueue = new SpawnItem(Material.LIME_DYE, "&dView Queue &7(Right-click)");
	public static SpawnItem buycraft = new SpawnItem(Material.GOLD_INGOT, "&dBuycraft &7(Right-click)");
	public static SpawnItem daily = new SpawnItem(Material.BOOK, "&dDaily Challenges &7(Right-click)");
}
