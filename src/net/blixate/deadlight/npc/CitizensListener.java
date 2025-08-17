package net.blixate.deadlight.npc;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.blixate.deadlight.MsgConfig;
import net.blixate.deadlight.challenges.DailyChallengesGUI;
import net.blixate.deadlight.fishing.FishingGUI;
import net.blixate.deadlight.gui.misc.LobbyListGui;
import net.blixate.deadlight.gui.misc.MysteryGui;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.utils.holograms.HoloManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;

public class CitizensListener implements Listener {

	@EventHandler
	public void onEnable(CitizensEnableEvent e) {
		CitizensAPI.getNPCRegistry().forEach((npc) -> {
			if(!npc.isSpawned()) {
				npc.spawn(npc.getStoredLocation());
			}
			System.out.println(npc.getName());
			InteractNPC interact = InteractNPC.get(npc.getName());
			if(interact != null) {
				Location loc = npc.getStoredLocation();
				if(interact.interactable) {
					HoloManager.spawnHologram(loc.add(0, 0.7, 0), ChatColor.GRAY + "" + ChatColor.ITALIC + "Click");
					HoloManager.spawnHologram(loc.add(0, 0.25, 0), interact.nameColor + interact.name);
				}
				
				if(interact.aboveNameTag != null) {
					HoloManager.spawnHologram(loc.add(0, 0.4, 0), ChatColor.GREEN + interact.aboveNameTag);
				}
			}
		});
	}
	
	@EventHandler
	public void onRightClick(NPCRightClickEvent e) {
		NPC npc = e.getNPC();
		DLUser user = PlayerManager.getUser(e.getClicker());
		if(!user.npcCooldown.isDone()) {
			return;
		}
		e.setCancelled(true);
		// check NPC id/uuid and open a menu
		InteractNPC interactNpc = InteractNPC.get(npc.getName());
		if(interactNpc == null) {
			return;
		}
		interact(user, npc, interactNpc);
	}
	@EventHandler
	public void onLeftClick(NPCLeftClickEvent e) {
		NPC npc = e.getNPC();
		DLUser user = PlayerManager.getUser(e.getClicker());
		if(!user.npcCooldown.isDone()) {
			return;
		}
		e.setCancelled(true);
		// check NPC id/uuid and open a menu
		InteractNPC interactNpc = InteractNPC.get(npc.getName());
		if(interactNpc == null) {
			return;
		}
		interact(user, npc, interactNpc);
	}
	
	public void interact(DLUser user, NPC npc, InteractNPC interactNpc) {
		float volume = .2f;
		switch(interactNpc) {
		case TUTORIAL:
			user.showBook(MsgConfig.getMessageList("tutorial"));
			if(!user.hasReadTutorial) {
				user.hasReadTutorial = true;
				user.addScoreEvent(new ScoreEvent("starter_blood", ScoreType.BLOOD));
				user.savePlayerData();
			}
			break;
		case CHALLENGES:
			if(!user.hasReadTutorial) {
				user.send("user_read_tutorial_vague");
				return;
			}
			user.openInventory(new DailyChallengesGUI(user, npc));
			if(user.hasCompletedAllDailyChallenges()) {
				user.send("npc_friend_dialog", "Charlegis", MsgConfig.getRandomMessage("npc_challenges_completed"));
			} else {
				user.send("npc_friend_dialog", "Charlegis", MsgConfig.getRandomMessage("npc_challenges"));
			}
			user.getPlayer().playSound(npc.getEntity().getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, volume, 0.3f);
			break;
		case FISHER:
			if(!user.hasReadTutorial) {
				user.send("user_read_tutorial_vague");
				return;
			}
			user.openInventory(new FishingGUI(user));
			if(user.fishingRodType == null) {
				user.send("npc_dialog", "Sea Monster", MsgConfig.getRandomMessage("npc_sea_monster"));
			}
			user.getPlayer().playSound(npc.getEntity().getLocation(), Sound.ENTITY_ZOMBIE_HURT, volume, 0.5f);
			break;
		case AKURA:
			user.getPlayer().playSound(npc.getEntity().getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, volume, 1f);
			user.send("npc_friend_dialog", "Mystery Man", MsgConfig.getRandomMessage("npc_akura_shop"));
			user.openInventory(new MysteryGui(user));
			break;
		case DEMON:
			user.getPlayer().playSound(npc.getEntity().getLocation(), Sound.ENTITY_PIGLIN_ADMIRING_ITEM, volume, .5f);
			user.send("npc_enemy_dialog", "Plague Monster", MsgConfig.getRandomMessage("npc_demon"));
			break;
		case CRESCENT:
			user.getPlayer().playSound(npc.getEntity().getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, volume, 1.5f);
			user.send("npc_friend_dialog", "Ext. Crescent", MsgConfig.getRandomMessage("npc_crescent"));
			break;
		case KERMINSKI:
			user.teleportToSpawn();
			user.getPlayer().playSound(npc.getEntity().getLocation(), Sound.ENTITY_VINDICATOR_CELEBRATE, volume, 0f);
			user.send("npc_enemy_dialog", "Site Director", MsgConfig.getRandomMessage("npc_jkerm"));
			user.send("npc_jkerm_interact");
			break;
		case GAMES:
			user.openInventory(new LobbyListGui(user));
			break;
		default:
			break;
		}
		user.npcCooldown.start(1.5f);
	}
	
}
