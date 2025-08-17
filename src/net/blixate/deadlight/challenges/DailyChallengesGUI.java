package net.blixate.deadlight.challenges;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.MsgConfig;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLUser;
import net.citizensnpcs.api.npc.NPC;

public class DailyChallengesGUI extends GuiInventory {
	
	private class DailyItem implements GuiItem {
		
		AssignedChallenge challenge;
		int index;
		
		public DailyItem(AssignedChallenge challenge, int index) {
			this.challenge = challenge;
			this.index = index;
		}
		
		@Override
		public void click(DLUser user, GuiInventory inventory) {
			if(challenge.isCompleted()) {
				user.dailyChallenges.set(index, null);
				user.addScoreEvent(new ScoreEvent("challenge_completed", ScoreType.SOULS));
				if(user.hasCompletedAllDailyChallenges()) {
					user.addScoreEvent(new ScoreEvent("all_challenges_completed", ScoreType.SOULS));
				}
				inventory.update();
			}
		}
		
		@Override
		public void rightClick(DLUser user, GuiInventory inventory) {
			BigInteger cost = getRerollCost();
			if(user.rerolls > 0) {
				if(user.souls.compareTo(cost) >= 0) {
					user.souls = user.souls.subtract(cost);
					user.send("reroll_daily_cost", cost.toString());
				}else {
					user.send("no_funds");
					return;
				}
			}else {
				user.send("reroll_daily");
			}
			user.rerollChallenge(index);
			user.rerolls ++;
			if(npc != null) {
				user.getPlayer().playSound(npc.getEntity().getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1, 0.3f);
				user.send("npc_friend_dialog", "Charlegis", MsgConfig.getRandomMessage("npc_challenges_reroll"));
			}
			inventory.update();
		}

		@Override
		public ItemStack item() {
			JsonChallenge json = getChallenge();
			ItemStack item;
			if(challenge.isCompleted()) {
				item = new ItemStack(Material.CHEST_MINECART);
			}else {
				item = new ItemStack(Material.MINECART);
			}
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("&aDaily Challenge #" + (index+1));
			List<String> lore = new ArrayList<>();
			lore.add("&8"+challenge.category.toString()+" Challenge");
			lore.add("");
			lore.add("&7" + json.text.replace("$", "&e" + challenge.progress + "&8/&e" + challenge.requirement + "&7"));
			lore.add("");
			if(challenge.isCompleted()) {
				lore.add("&aClick to claim rewards!");
			}else {
				lore.add("&4Notice: &cChallenges require 4 or more players in a game.");
				if(getRerollCost().equals(BigInteger.ZERO)) {
					lore.add("&7Right-click to reroll for &eFree&7!");
				}else {
					lore.add("&7Right-click to reroll for &e"+getRerollCost().toString()+" Souls&7!");
				}
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		}
		
		private JsonChallenge getChallenge() {
			return Deadlight.getChallengeManager().getChallengeById(challenge.category, challenge.id);
		}
		
		public BigInteger getRerollCost() {
			return BigInteger.valueOf(user.rerolls * 30);
		}
	}
	
	public static class CompletedDailyItem implements GuiItem {

		@Override
		public void click(DLUser user, GuiInventory inventory) {
			
		}

		@Override
		public ItemStack item() {
			ItemStack item = new ItemStack(Material.BARRIER);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("&aRewards claimed!");
			List<String> lore = new ArrayList<>();
			lore.add("&7Come back tomorrow!");
			meta.setLore(lore);
			item.setItemMeta(meta);
			return item;
		}
		
	}
	
	DLUser user;
	NPC npc;
	
	public DailyChallengesGUI(DLUser user, NPC npc) {
		super("Daily Challenges", 3);
		this.user = user;
		this.npc = npc;
	}
	
	public void construct() {
		if(user.dailyChallenges == null || user.dailyChallenges.isEmpty()) {
			user.rollNewChallenges();
		}
		int[] slots = {10, 13, 16};
		for(int i = 0; i < 3; i++) {
			AssignedChallenge challenge = user.dailyChallenges.get(i);
			if(challenge == null) {
				this.setItem(slots[i], new CompletedDailyItem());
			}else {
				this.setItem(slots[i], new DailyItem(challenge, i));
			}
		}
	}

}
