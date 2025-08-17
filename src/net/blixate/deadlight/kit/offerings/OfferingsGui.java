package net.blixate.deadlight.kit.offerings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.gui.ConfirmationGui;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.gui.GuiListener;
import net.blixate.deadlight.kit.KitGui;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.ParticlesUtil;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class OfferingsGui extends GuiInventory {
	
	public static final int BURN_SOUL_COST = 15;
	
	private static class OfferingItem implements GuiItem {
		
		Offering offering;
		long amount;
		
		public OfferingItem(Entry<String, Integer> data) {
			this.offering = Offering.valueOf(data.getKey());
			this.amount = data.getValue();
		}
		
		public void customClick(DLUser user, GuiInventory inventory, ClickType click) {
			ConfirmationGui confirm  = null;
			GuiListener decline = (_u, inv) -> {
				user.openInventory(inventory);
			};
			if(click.isLeftClick() && click.isShiftClick()) {
				if(offering.getDuration() == -1) {
					return;
				}
				confirm = new ConfirmationGui("Activate Maximum Offerings?", (_u, inv) -> {
					int offeringCount = user.offerings.get(offering.name());
					int count = offering.limit - OfferingManager.isOfferingActive(offering);
					
					if(offeringCount <= count) {
						count = offeringCount;
					}
					
					if(offeringCount > 0) {
						if(user.getSouls() >= BURN_SOUL_COST) {
							user.souls = user.souls.subtract(BigInteger.valueOf(BURN_SOUL_COST).multiply(BigInteger.valueOf(count)));
						}else {
							user.getPlayer().closeInventory();
							user.send("no_funds");
							return;
						}
						if(OfferingManager.isCountMax(offering)) {
							user.send("offering_maxed");
							user.getPlayer().closeInventory();
							return;
						}
						for(int i = 0; i < count; i++) {
							OfferingManager.addOffering(offering, user, offering.getDuration());
						}
						user.offerings.put(offering.name(), offeringCount-count);
						if(offeringCount - count <= 0) {
							user.offerings.remove(offering.name());
						}
						// send message
						String hoverText = "&6" + offering.getName() + "\n&7" + offering.getDescription();
						TextComponent text = new TextComponent(Deadlight.msg("offering_burnt", new String[] { user.getName(), count + "x " + offering.getName() }));
						text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(FormatUtil.color(hoverText))));
						Location particleLoc = user.getLocation().add(0,1,0);
						ParticlesUtil.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, BURN_SOUL_COST, 0.05, 0,0,0);
						ParticlesUtil.spawnParticle(Particle.FLAME, particleLoc, 50-BURN_SOUL_COST, 0.05, 0,0,0);
						for(DLUser player : PlayerManager.getPlayers()) {
							// even if they are in a match, notify of the offering burn
							player.getPlayer().spigot().sendMessage(text);
							player.getPlayer().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 0.5f);
						}
					}else {
						user.send("no_resources");
					}
					
					user.getPlayer().closeInventory();
				}, decline, true);
				user.openInventory(confirm);
			} else if(click.isLeftClick() && !click.isShiftClick()) {
				confirm = new ConfirmationGui("Activate Offering?", (_u, inv) -> {
					int offeringCount = user.offerings.get(offering.name());
					if(offeringCount > 0) {
						if(user.getSouls() >= BURN_SOUL_COST) {
							user.souls = user.souls.subtract(BigInteger.valueOf(BURN_SOUL_COST));
						}else {
							user.getPlayer().closeInventory();
							user.send("no_funds");
							return;
						}
						// remove the offering
						long duration = offering.getDuration();
						if(duration == -1) {
							offering.use(user);
						}
						else {
							if(OfferingManager.isCountMax(offering)) {
								user.send("offering_maxed");
								user.getPlayer().closeInventory();
								return;
							}
							OfferingManager.addOffering(offering, user, offering.getDuration());
						}
						user.offerings.put(offering.name(), offeringCount-1);
						if(offeringCount - 1 <= 0) {
							user.offerings.remove(offering.name());
						}
						// send message
						String hoverText = "&6" + offering.getName() + "\n&7" + offering.getDescription();
						TextComponent text = new TextComponent(Deadlight.msg("offering_burnt", new String[] { user.getName(), offering.getName() }));
						text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(FormatUtil.color(hoverText))));
						Location particleLoc = user.getLocation().add(0,1,0);
						ParticlesUtil.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, BURN_SOUL_COST, 0.05, 0,0,0);
						ParticlesUtil.spawnParticle(Particle.FLAME, particleLoc, 50-BURN_SOUL_COST, 0.05, 0,0,0);
						for(DLUser player : PlayerManager.getPlayers()) {
							// even if they are in a match, notify of the offering burn
							player.getPlayer().spigot().sendMessage(text);
							player.getPlayer().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 0.5f);
						}
					}else {
						user.send("no_resources");
					}
					
					user.getPlayer().closeInventory();
				}, decline, true);
			}else {
				return;
			}
			user.openInventory(confirm);
		}

		@Override
		public ItemStack item() {
			ItemStack item = new ItemStack(Material.FIRE_CHARGE);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("&ax" + amount + " &6" + offering.getName());
			List<String> lore = new ArrayList<String>();
			lore.add("&8Offering");
			if(offering.getDuration() == -1) {
				lore.add("&8One-time use");
			}
			lore.add("");
			for(String line : offering.getDescription().split("\n")) {
				lore.add("&7" + line);
			}
			lore.add("");
			if(offering.getDuration() != -1) {
				lore.add("&7Total Active: &e" + OfferingManager.isOfferingActive(offering) + "&7/" + offering.limit);
				lore.add("");
			}
			lore.add("&7Click to burn this offering");
			if(offering.getDuration() != -1)
				lore.add("&7Shift left-click to burn max offerings.");
			lore.add("&7Costs &e"+BURN_SOUL_COST+" Soul&7 to burn.");
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		}

		@Override
		public void click(DLUser user, GuiInventory inventory) {
			
		}
	}
	
	KitGui gui;
	DLUser user;
	
	public OfferingsGui(DLUser user, KitGui gui) {
		super("Offerings", 4);
		this.gui = gui;
		this.user = user;
	}
	
	public void construct() {
		for(int i = 27; i < 36; i++) {
			setItem(i, GuiItem.blank(Material.BLACK_STAINED_GLASS_PANE, "&0"));
		}
		if(user.offerings == null || user.offerings.isEmpty()) {
			setItem(13, GuiItem.blank(Material.BARRIER, "&cYou don't have any offerings!"));
		}else {
			int i = 0;
			for(Entry<String, Integer> entry : user.offerings.entrySet()) {
				try {
					setItem(i, new OfferingItem(entry));
				}catch(Throwable t) {
					setItem(i, GuiItem.blank(Material.BARRIER, "&lERROR &7Invalid Offering"));
					t.printStackTrace();
				}
				i++;
			}
		}
		setBackArrow(27, gui);
	}

}
