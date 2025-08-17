package net.blixate.deadlight.fishing;

import java.math.BigInteger;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.blixate.deadlight.gui.ConfirmationGui;
import net.blixate.deadlight.gui.GuiIcon;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.gui.GuiListener;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.file.SongManager;

public class FishingGUI extends GuiInventory {
	public static class RodItem implements GuiItem {
		FishingRods rod;
		int index;
		public RodItem(FishingRods rod, int index) {
			this.rod = rod;
			this.index = index;
		}
		
		public void click(DLUser user, GuiInventory inventory) {
			if(user.fishingRodType != null) {
				user.send("rod_already_equipped");
				return;
			}
			long cost = rod.cost(index);
			if(user.getSouls() >= cost) {
				user.souls = user.souls.subtract(BigInteger.valueOf(cost));
				user.fishingRodType = rod;
				user.fishingRodUsesLeft = FishingRods.uses[index];
				user.send("rod_purchased", rod.getName(), FormatUtil.formatLong(cost) + " Souls");
				if(user.getLobby() == null) {
					user.getInventory().setItem(4, rod.get());
				}
				user.getPlayer().closeInventory();
				SongManager.playSong(user.getPlayer(), SongManager.getSong("danger_incoming"));
			} else {
				user.send("no_funds");
			}
		}
		
		public ItemStack item() { return rod.icon(index); }
	}
	
	public static class RefreshItem implements GuiItem {
		
		DLUser user;
		int numberOfUses;
		
		public RefreshItem(DLUser user, int numberOfUses) {
			this.user = user;
			this.numberOfUses = numberOfUses;
		}
		
		@Override
		public void click(DLUser user, GuiInventory inventory) {
			if(user.getSouls() >= getCost()) {
				user.souls = user.souls.subtract(BigInteger.valueOf(getCost()));
				user.fishingRodUsesLeft += numberOfUses;
				inventory.update();
				user.send("rod_purchased", numberOfUses + " " + user.fishingRodType.getName() + " Fishing Rod Bait", getCost() + " Souls");
				user.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1);
			}
		}

		@Override
		public ItemStack item() {
			ItemStack item = new ItemStack(Material.EMERALD, 1);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("&aBuy " + numberOfUses + " Bait");
			meta.setLore(Lists.newArrayList(
				"&7Allows you to use this Fishing Rod for longer.",
				"&7No discount for purchasing additional bait.",
				"&7",
				"&7Click to buy for &e" + FormatUtil.formatLong(getCost()) + " Souls"
			));
			item.setItemMeta(meta);
			return item;
		}
		
		public long getCost() {
			return (long) (user.fishingRodType.costPerUse * numberOfUses);
		}
		
	}
	
	DLUser viewer;
	
	public FishingGUI(DLUser user) {
		super("Fishing", 5);
		this.viewer = user;
	}
	
	public void construct() {
		if(viewer.fishingRodType == null) {
			for(int i = 0; i < 3; i++) {
				final int index = i;
				int offset = 10;
				setItem(offset + (i * 9), new RodItem(FishingRods.BASIC, index));
				offset += 3;
				setItem(offset + (i * 9), new RodItem(FishingRods.ELITE, index));
				offset += 3;
				setItem(offset + (i * 9), new RodItem(FishingRods.LEGEND, index));
			}
		}else {
			setItem(19, new GuiListener() {
				public void click(DLUser u, GuiInventory i) {
					u.openInventory(new ConfirmationGui("Remove your Fishing Rod?", /*onAccept*/(user, inventory) -> {
						user.fishingRodUsesLeft = -1;
						user.fishingRodType = null;
						if(user.getLobby() == null) {
							if(user.getInventory().getItem(4) != null) {
								if(user.getInventory().getItem(4).getType() == Material.FISHING_ROD) {
									user.getInventory().clear(4);
								}
							}
						}
						user.send("rod_unequipped");
						user.getPlayer().closeInventory();
					}, (user, inventory) -> {
						user.openInventory(new FishingGUI(user));
					}));
				}
			}.toGuiItem(GuiIcon.createItemStack(Material.BARRIER, "&cRemove Rod", "&7This will not refund you.")));
			
			setItem(22, new GuiIcon() {
				public ItemStack item() {
					ItemStack item = new ItemStack(Material.FISHING_ROD, 1);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(viewer.fishingRodType.getName());
					meta.setLore(Lists.newArrayList(
						"&7Bait Left: &e" + FormatUtil.formatLong(viewer.fishingRodUsesLeft)
					));
					item.setItemMeta(meta);
					return item;
				}
			});
			setItem(30, new RefreshItem(viewer, 25));
			setItem(31, new RefreshItem(viewer, 50));
			setItem(32, new RefreshItem(viewer, 100));
		}
	}
}
