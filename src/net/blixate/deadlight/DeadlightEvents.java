package net.blixate.deadlight;

import java.io.File;
import java.math.BigInteger;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

import net.blixate.deadlight.advertising.MinehutRank;
import net.blixate.deadlight.commands.staff.StaffCommand;
import net.blixate.deadlight.discord.DiscordManager;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.kit.KitGui;
import net.blixate.deadlight.player.DLData;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.player.items.SpawnItems;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.ParticlesUtil;
import net.blixate.deadlight.util.WebUtils;
import net.blixate.deadlight.util.random.DeadlightLootTable.DropResult;
import net.blixate.deadlight.util.time.TimeParser;
import net.md_5.bungee.api.ChatColor;

public class DeadlightEvents implements Listener {
	public static final String messageRegex = "[!-~ ]+";
	
	public boolean createShortcut(AsyncPlayerChatEvent e, String text, String permission, Runnable runnable) {
		if(e.getMessage().startsWith(text) && e.getPlayer().hasPermission(permission)) {
			runnable.run();
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerListPing(ServerListPingEvent e) {
		try {
			String message = Deadlight.msg("message_of_the_day");
			e.setMotd(message);
			if(TimeKeeper.isBloodBathEvent()) {
				e.setServerIcon(Bukkit.loadServerIcon(new File("server-icon-bloodbath.png")));
			}else {
				e.setServerIcon(Bukkit.loadServerIcon(new File("server-icon.png")));
			}
			Deadlight.debug("Server List Ping: " + e.getAddress().getHostAddress());
		}catch(Throwable t) {
			e.setMotd(ChatColor.translateAlternateColorCodes('&', "&9Dead Light &4Ping Error."));
		}
		
	}
	
	@EventHandler
	public void onTransform(EntityTransformEvent e) {
		if(e.getTransformReason() == TransformReason.DROWNED) {
			e.setCancelled(true); // fixes bug with necromancer minions
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		try {
			Player p = e.getPlayer();
			DLUser user = PlayerManager.createUser(p);
			// login reward
			long timeSinceLoginReward = (System.currentTimeMillis() - user.lastLoginReward);
			final long FULL_DAY = DLData.DAILY_REWARD_TIME;
			if(timeSinceLoginReward > FULL_DAY * 2) {
				user.loginReward = 1;
			}
			if(timeSinceLoginReward >= FULL_DAY) {
				user.souls = user.souls.add(BigInteger.valueOf(user.loginReward));
				user.send("login_reward", ""+user.loginReward);
				user.lastLoginReward = System.currentTimeMillis();
				user.loginReward++;
				user.rollNewChallenges();
				user.rerolls = 0;
			}
			else if(user.dailyChallenges == null || user.dailyChallenges.isEmpty()) {
				user.rollNewChallenges();
			}
			user.teleportToSpawn();
			String joinMessage = "join_message";
			if(!p.hasPlayedBefore()) {
				joinMessage = "first_time_join";
			}
			if(p.hasPermission("deadlight.staff")) {
				joinMessage = "staff_join";
			}
			e.setJoinMessage(Deadlight.msg(joinMessage, new String[] { p.getName() }));
			user.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);
			if(user.getPrestige() >= 1) {
				Deadlight.leaderboard.setEligible(user.getUUIDString());
			}
			if(user.getSouls() < 0) {
				user.souls = BigInteger.valueOf(0);
			}
			if(TimeKeeper.isBloodBathEvent()) {
				user.send("event_bloodbath_text");
			}
			if(TimeKeeper.isFishingFrenzyEvent()) {
				user.send("event_fishing_text");
			}
			// Only works on Minehut, but will fail gracefully
			Bukkit.getScheduler().runTaskLater(Deadlight.inst, () -> {
				try {
					MinehutRank rank = Deadlight.getMinehutRankManager().getRank(getRank(user.getUUID()));
					user.minehutRankName = rank.id;
					System.out.println("Rank: " + rank);
				}catch(Throwable t) {
				}
			}, 3);
		}catch(Throwable t) {
			t.printStackTrace();
			e.getPlayer().sendMessage("Player data failed to load. (No registry)");
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		// get user
		DLUser user = getPlayer(e);
		if(user.getLobby() != null)
			user.getLobby().disconnect(user);
		user.delete();
		e.setQuitMessage(Deadlight.msg("quit_message", new String[] { user.getPlayer().getName() }));
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player p = event.getPlayer();
		if(p.hasPermission("deadlight.command.bypass") ) {
			return;
		}
		String msg = event.getMessage();
		String commandLabel = msg.split(" ")[0];
		if(commandLabel.startsWith("/minecraft:")) {
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "This command is disabled.");
			return;
		}
		switch(commandLabel.substring(1)) {
		case "me":
		case "w":
		case "whisper":
		case "tell":
		case "teammsg":
		case "tm":
			event.setCancelled(true);
			p.sendMessage(ChatColor.RED + "This command is disabled.");
			break;
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) {
			return;
		}
		DLUser user = PlayerManager.getUser(e.getEntity().getUniqueId());
		if(user == null) {
			return;
		}
		if(user.getLobby() == null) {
			e.setCancelled(true);
			return;
		}
		if(user.isKiller()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player) {
			Player damager = (Player)e.getDamager();
			if(damager.getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
		DLUser victim = PlayerManager.getUser(e.getEntity().getUniqueId());
		if(victim == null) return;
		if(victim.getLobby() == null) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(final AsyncPlayerChatEvent e) {
		final DLUser user = getPlayer(e);
		if(user == null) {
			e.getPlayer().sendMessage("You are not registered, something went wrong. Try rejoining!");
			e.setCancelled(true);
			return;
		}
		e.setCancelled(true);
		if(!user.getPlayer().hasPermission("deadlight.chatdelay.bypass")) {
			if(!user.chatCooldown.isDone()) {
				user.send("chat_cooldown");
				return;
			}
			user.chatCooldown.start(1f);
		}
		
		if(createShortcut(e, "# ", "deadlight.staffchat", () -> StaffCommand.staffChat(e.getPlayer(), e.getMessage().substring(2).stripLeading()))) return;
		
		if(Deadlight.muteChat && !user.checkPerm("deadlight.mutechat.bypass")) {
			user.send("chat_muted");
			return;
		}
		
		if(user.muteDuration > 0) {
			if(user.muteDuration < System.currentTimeMillis()) {
				user.muteDuration = -1;
			}
		}
		if(user.muteDuration != -1) {
			if(user.muteDuration == Long.MAX_VALUE) {
				if(user.muteReason == null) {
					user.send("mute_no_reason_warning");
				}else {
					user.send("mute_warning", user.muteReason);
				}
			}else {
				long timeRemaining = user.muteDuration - System.currentTimeMillis();
				/* Unmute the player if there time has passed!
				 * We don't care about milliseconds
				 */ 
				if(timeRemaining <= 1000) {
					user.muteDuration = -1;
					user.muteReason = null;
				}else {
					if(user.muteReason == null) {
						user.send("tempmute_no_reason_warning", TimeParser.toFancyTime(timeRemaining));
					} else {
						user.send("tempmute_warning", user.muteReason, TimeParser.toFancyTime(timeRemaining));
					}
				}
			}
			return;
		}
		
		// Chat filters go here.
		
		if(!user.checkPerm("deadlight.chat.unicode") && !Deadlight.debug) {
			if(!e.getMessage().matches(messageRegex)) {
				Deadlight.staffMsg(FormatUtil.color("&8&lBLOCKED � &f") + user.getName() + ": " + e.getMessage());
				user.send("chat_no_unicode");
				return;
			}
		}
		/* If execution gets to this point, we assume this player can talk. */
		if(DiscordManager.isLoaded()) {
			// send before any modifications are made to the original message
			DiscordManager.sendChatMessage(user, e.getMessage());
		}
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(e.getMessage().startsWith(p.getName())) {
				e.setMessage(e.getMessage().replace(p.getName(), FormatUtil.color("&c@" + p.getName() + Deadlight.getChatColor())));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
			}
		}
		
		final int pres = user.getPrestige();
		final String prestigeColor = pres >= Deadlight.prestigeColors.length ? ChatColor.BLACK+"" :  Deadlight.prestigeColors[pres];
		final String[] format = {
				user.getFormattedName(),
				""+user.getLevel(),
				prestigeColor,
				e.getMessage()
		};
		final String chatMessage = Deadlight.msg(user, user.checkPerm("deadlight.chatcolor") ? "chat_message_colored" : "chat_message", format);
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(chatMessage);
		}
		Bukkit.getConsoleSender().sendMessage(chatMessage);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSwapHands(PlayerSwapHandItemsEvent e) {
		DLUser user = PlayerManager.getUser(e.getPlayer().getUniqueId());
		if(user.getLobby() == null && user.checkPerm("deadlight.fly") && user.launchCooldown.isDone() && user.isOnGround()) {
			Vector direction = user.getDirection();
			user.push(direction.normalize(), 2d);
			user.launchCooldown.start(.5f);
		}
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		// Code to handle GUI events
		DLUser player = get(e.getWhoClicked());
		if(!e.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) {
			e.setCancelled(true);
		}
		if(player == null) return;
		if(player.getGui() == null) {
			return;
		}
		if(e.getClickedInventory() instanceof PlayerInventory) {
			return;
		}
		int slot = e.getSlot();
		GuiItem item = player.getGui().getSlot(slot);
		if(item == null) {
			return;
		}
		if(e.getClick() == ClickType.RIGHT) {
			item.rightClick(player, player.getGui());
		}else {
			item.click(player, player.getGui());
		}
		item.customClick(player, player.getGui(), e.getClick());
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onSignEdit(SignChangeEvent e) {
		String[] lines = e.getLines();
		if(e.getPlayer().hasPermission("deadlight.sign")) {
			if(e.getLine(0).equals("[code]")) {
				e.setLine(0, "");
				e.setLine(1, FormatUtil.color("&dEnter Code"));
				e.setLine(2, "");
			}
		}
		for(int i = 0; i < lines.length; i++) {
			e.setLine(i, FormatUtil.color(lines[i]));
		}
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		Entity entity = e.getRightClicked();
		if(entity instanceof ItemFrame || entity.getType().equals(EntityType.ITEM_FRAME)) {
			if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onSpawnItemInteract(PlayerInteractEvent e) {
		// Spawn item interaction
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			DLUser user = getPlayer(e);
			if(user == null) {
				return;
			}
			if(!user.isInMatch() && (!user.getPlayer().hasPermission("deadlight.staff"))) {
				e.setUseInteractedBlock(Result.DENY);
			}
			PlayerInventory inv = user.getInventory();
			ItemStack item = inv.getItem(inv.getHeldItemSlot());
			if(item == null) {
				return;
			}
			if(SpawnItems.kit.is(item)) {
				if(user.getLobby() == null)
					user.openInventory(new KitGui(user));
				e.setCancelled(true);
			}
			else if(SpawnItems.joinQueue.is(item)) {
				if(user.getLobby() == null)
					user.queue();
				e.setCancelled(true);
			}
			else if(SpawnItems.leaveQueue.is(item)) {
				if(user.getLobby() == null)
					user.showQueueInventory();
				e.setCancelled(true);
			}else if(SpawnItems.leaveMatch.is(item)) {
				if(user.getLobby() != null) {
					user.getLobby().disconnect(user);
					user.teleportToSpawn();
				}
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		e.setKeepInventory(true);
		e.setKeepLevel(true);
		e.setDroppedExp(0);
		e.setDeathMessage("");
		e.getDrops().clear();
		Bukkit.getScheduler().runTaskLater(Deadlight.inst, () -> e.getEntity().spigot().respawn(), 1L);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		DLUser player = PlayerManager.getUser(e.getPlayer().getUniqueId());
		if(player == null) return;
		player.gui = null;
	}
	
	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent e) {
		e.setCancelled(true);
		e.setFoodLevel(20);
	}
	
	@EventHandler
	public void onFish(PlayerFishEvent e) {
		DLUser user = PlayerManager.getUser(e.getPlayer());
		switch(e.getState()) {
		case CAUGHT_FISH:
			e.getCaught().remove();
			e.setExpToDrop(0);
			DropResult itemCaught = user.getFishingRod().getTable().pick();
			user.addDropReward(itemCaught);
			user.send("fishing_caught", itemCaught.toString());
			// show random fishing thought
			if(Math.random() * 100 < 15) {
				user.getPlayer().sendMessage(FormatUtil.color(MsgConfig.getRandomMessage("fishing_random_thoughts")));
				user.playSound(Sound.ENTITY_ITEM_PICKUP, .5f);
				user.addExp(TimeKeeper.isFishingFrenzyEvent() ? 50 : 25);
			}else {
				user.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f);
				user.addExp(TimeKeeper.isFishingFrenzyEvent() ? 30 : 15);
			}
			Location loc = e.getHook().getLocation();
			ParticlesUtil.spawnParticle(Particle.VILLAGER_HAPPY, loc, 15, 0, 1, 1, 1);
			user.useFishingRod();
			break;
		case FAILED_ATTEMPT:
			user.playSound(Sound.UI_STONECUTTER_TAKE_RESULT, 1.6);
			setGlowing(e.getHook(), false);
			user.useFishingRod();
			break;
		// unimportant events
		case CAUGHT_ENTITY:
			if(Math.random() * 100 < 40)
				user.send("fishing_entity");
		case BITE:
			setGlowing(e.getHook(), true);
			break;
		case FISHING:
			if(TimeKeeper.isFishingFrenzyEvent()) {
				e.getHook().setMaxWaitTime(150);
				e.getHook().setMinWaitTime(0);
			}
			setGlowing(e.getHook(), false);
			break;
		case IN_GROUND:
			break;
		case REEL_IN:
			break;
		default:
			break;
		}
	}
	
	public static String getRank(UUID uuid) {
		JSONObject json = WebUtils.getJSON(WebUtils.MINEHUT_COSMETICS_URL.replace("%uuid", uuid.toString()));
		@SuppressWarnings("unchecked")
		String rankId = (String)json.getOrDefault("rank", "DEFAULT");
		return rankId;
	}
	
	private static DLUser getPlayer(PlayerEvent e) {
		return get(e.getPlayer());
	}
	
	private static DLUser get(Entity p) {
		return PlayerManager.getUser(p.getUniqueId());
	}
	
	public void setGlowing(FishHook hook, boolean glowing) {
		hook.setGlowing(glowing);
	}
}
