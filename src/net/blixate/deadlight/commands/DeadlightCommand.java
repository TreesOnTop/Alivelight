package net.blixate.deadlight.commands;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.TimeKeeper;
import net.blixate.deadlight.advertising.MinehutRank;
import net.blixate.deadlight.gui.GuiBuilder;
import net.blixate.deadlight.gui.GuiInventory;
import net.blixate.deadlight.gui.GuiItem;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.lobby.LobbyManager;
import net.blixate.deadlight.maps.MapLoader;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.player.effects.KnockedEffect;
import net.blixate.deadlight.queue.QueueRoleSelectGui;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.HeadUtils;
import net.blixate.deadlight.util.time.TimeParser;
import net.blixate.deadlight.utils.holograms.HoloManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class DeadlightCommand implements CommandExecutor {
	private static class Cmd {
		String name, description, args;
		public Cmd(String name, String desc) {
			this.name = name; this.description = desc;
		}
		public Cmd(String name, String desc, String args) {
			this(name, desc); this.args = args;
		}
	}
	
	private static Cmd cmd(String name, String description) {
		return new Cmd(name, description);
	}
	
	private static Cmd cmd(String name, String description, String args) {
		return new Cmd(name, description, args);
	}
	
	private static final Cmd[] commands = {
		cmd("help", "Help command for this command."),
		cmd("reload", "Reload configuration files"),
		cmd("debug", "Toggle debug mode"),
		cmd("start", "Start the current queue into a lobby."),
		cmd("save-all", "Save all player's data."),
		cmd("players", "List all registered players"),
		cmd("serverinfo", "Display a GUI with all server info"),
		cmd("head", "Provide a skull texture to receive the head.", "[base64 texture]"),
		cmd("lock", "Disable the queue system."),
		cmd("unlock", "Re-enable the queue system."),
		cmd("wipe", "Deletes player data", "[player]"),
	};
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0 && sender.hasPermission("deadlight.deadlight")) {
			deadlightCommand(args, sender);
			return true;
		}else{
			sender.sendMessage(FormatUtil.color("&9Deadlight &av" + Deadlight.getVersion()));
		}
		return true;
	}
	
	public static GuiInventory createServerInfoGui() throws UnknownHostException {
		File playerfolder = new File(Deadlight.dataFolder, "data");
		final Material categoryMaterial = Material.OAK_SIGN;
		return new GuiBuilder("Deadlight - Server Info", 3)
		.setItem(0, categoryMaterial, "&2&lServer Info")
		.setGuiItem(1, guiValue("Server IP", InetAddress.getLocalHost().getHostAddress() + ":" + Bukkit.getPort()))
		.setGuiItem(2, guiValue("System Time", TimeParser.toFancyTime(System.currentTimeMillis())))
		.setGuiItem(3, guiValue("Memory Usage", getMemoryUsage()))
		.setGuiItem(4, guiValue("Cores", ""+Runtime.getRuntime().availableProcessors()))
		.setGuiItem(5, guiValue("Operating System", System.getProperty("os.name") + " v" + System.getProperty("os.version")))
		// Versions
		.setItem(9, categoryMaterial, "&2&lVersions")
		.setGuiItem(10, guiValue("Bukkit/Spigot Version", Bukkit.getBukkitVersion() + " / " + Bukkit.getVersion()))
		.setGuiItem(11, guiValue("Deadlight Version", Deadlight.getVersion()))
		.setGuiItem(12, guiValue("Java Version", System.getProperty("java.version")))
		// Players
		.setItem(18, categoryMaterial, "&2&lPlayers")
		.setGuiItem(19, guiValue("Total Joins", "" + Bukkit.getOfflinePlayers().length))
		.setGuiItem(20, guiValue("Current Player Count", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers()))
		.setGuiItem(21, guiValue("Total Opped Players", ""+Bukkit.getOperators().size()))
		.setGuiItem(22, guiValue("Total Player File Size (bytes)",""+Deadlight.sizeOfDirectory(playerfolder)))
		.setGuiItem(23, guiValue("Total Player File Count", ""+playerfolder.listFiles().length))
		.build();
	}
	
	private static GuiItem guiValue(String title, String lore) {
		return GuiItem.toGuiItem(null, Material.BIRCH_SIGN, ChatColor.GREEN + title, (lore.split("\n")));
	}
	
	private static String getMemoryUsage() {
		return Math.round((Runtime.getRuntime().freeMemory()/1000)/1000) + "MB free\n" + ChatColor.GRAY +
				((getMemoryUsed()/1000)/1000) + "MB/" + Math.round((Runtime.getRuntime().totalMemory()/1000)/1000) + "MB";
	}
	
	private static long getMemoryUsed() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}
	
	public static void deadlightCommand(String[] args, CommandSender sender) {
		// LOG ALL USE OF THIS COMMAND.
		Deadlight.staffMsg(ChatColor.DARK_GRAY + "[LOG /DL] "+sender.getName() + ": " +ChatColor.GRAY+ String.join(" ", args));
		switch(args[0].toLowerCase()) {
		case "reload":
			Deadlight.inst.reload();
			sender.sendMessage("Reloaded configuration.");
			break;
		case "debug":
			Deadlight.debug = !Deadlight.debug;
			sender.sendMessage("Toggled debug mode to " + Deadlight.debug);
			break;
		case "start":
		case "forcestart": {
			Deadlight.queue.start();
		}
			break;
		case "knock": {
			DLUser user = PlayerManager.getUser(sender);
			if(user.isInMatch()) {
				user.applyEffect(new KnockedEffect(10000));
			}
		}
			break;
		case "testgui": {
			if(args.length > 1) {
				int size = Integer.parseInt(args[1]);
				Inventory inv = Bukkit.createInventory(null, size * 9, "Test Inventory with " + size + " rows");
				Player player = (Player)sender;
				player.openInventory(inv);
			}
		}
			break;
		case "testhoppergui": {
			Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, "Test Inventory with Hopper");
			Player player = (Player)sender;
			player.openInventory(inv);
		}
			break;
		case "testdroppergui": {
			Inventory inv = Bukkit.createInventory(null, InventoryType.DROPPER, "Test Inventory with Dropper");
			Player player = (Player)sender;
			player.openInventory(inv);
		}
			break;
		case "head": {
			if(args.length > 1) {
				if(!(sender instanceof Player)) {
					return;
				}
				ItemStack head = HeadUtils.createPlayerHead(args[1]);
				((Player)sender).getInventory().addItem(head);
			}
		} break;
		case "fastforward": {
			DLUser user = PlayerManager.getUser(sender);
			long time = TimeParser.parseTime(args[1]);
			user.lastLoginReward -= time;
		}
			break;
		case "setpreferred": {
			DLUser user = PlayerManager.getUser(sender);
			user.openInventory(new QueueRoleSelectGui(user));
		}
			break;
		case "specs": {
			if(args.length == 2) {
				int idx;
				try{
					idx = Integer.parseInt(args[1]);
				}catch(NumberFormatException e) {
					sender.sendMessage("Invalid integer.");
					return;
				}
				Lobby lobby = LobbyManager.getInstance().getLobby(idx);
				sender.sendMessage("Ext Specs: " + lobby.externalSpectators.toString());
				sender.sendMessage("Players: " + Lists.newArrayList(lobby.getPlayers()));
				sender.sendMessage("Killer: " + lobby.getKiller());
				sender.sendMessage("Survivors: " + lobby.getSurvivors());
			}
		}
			break;
		case "forcemapload":
		{
			if(args.length == 3) {
				int idx;
				try{
					idx = Integer.parseInt(args[1]);
				}catch(NumberFormatException e) {
					sender.sendMessage("Invalid integer.");
					return;
				}
				String mapName = args[2];
				try {
					MapLoader.loadMap(mapName, idx);
				} catch (IOException e) {
					sender.sendMessage(ChatColor.RED + "Something went wrong!");
					e.printStackTrace();
				}
			}
		}
			break;
		case "events":
		{
			sender.sendMessage("" + TimeKeeper.getDayOfTheWeek() + ", " + TimeKeeper.getDayByName("bloodbath"));
			sender.sendMessage("Bloodbath: " + TimeKeeper.isBloodBathEvent());
			sender.sendMessage("Fishing: " + TimeKeeper.isFishingFrenzyEvent());
			sender.sendMessage("Surge: " + TimeKeeper.isSurgeEvent());
			break;
		}
		case "wipe":
		{
			if(args.length > 1) {
				UUID uuid = null;
				if(Bukkit.getPlayer(args[1]) != null) {
					uuid = Bukkit.getPlayer(args[1]).getUniqueId();
				}else {
					@SuppressWarnings("deprecation")
					OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
					uuid = player.getUniqueId();
				}
				if(PlayerManager.getUser(uuid) != null) {
					PlayerManager.getUser(uuid).delete();
				}
				File playerFile = PlayerManager.getPlayerFile(uuid);
				sender.sendMessage("Deleting " + playerFile.getPath());
				try {
					if(playerFile.exists()) {
						playerFile.delete();
					}else {
						sender.sendMessage("Player file does not exist.");
						return;
					}
				}catch(Throwable t) {
					sender.sendMessage("An error occurred while trying to delete this player's file.");
					return;
				}
				if(PlayerManager.getUser(uuid) != null) {
					sender.sendMessage("Reloading player...");
					PlayerManager.removeUser(Bukkit.getPlayer(uuid));
					PlayerManager.createUser(Bukkit.getPlayer(uuid));
				}
				sender.sendMessage("Successfully deleted this player's file!");
			}else {
				sender.sendMessage("Player argument required.");
			}
			
		}
			break;
		case "ranks": {
			DLUser user = PlayerManager.getUser(sender);
			String adString = "DEAD BY DAYLIGHT IN MINECRAFT";
			for(MinehutRank rank : Deadlight.getMinehutRankManager().getRanks()) {
				String preview = "&d[AD] " + rank.formatPlayer(user.getName()) + rank.chatColor + ": /ad Deadlight " + adString;
				TextComponent text = new TextComponent("► " + rank.name + " (" + rank.id + ")");
				text.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "/ad Deadlight " + adString));
				text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(FormatUtil.color("&7Preview:\n" + preview + "\n&9Click here to copy to your clipboard"))));
				text.setColor(ChatColor.GREEN);
				user.getPlayer().spigot().sendMessage(text);
			}
		}
			break;
		case "setrank": {
			if(args.length >= 2) {
				DLUser user = PlayerManager.getUser(sender);
				user.minehutRankName = args[1].toUpperCase();
			}
			else {
				sender.sendMessage("Usage: /dl setrank (rank)");
			}
		}
			break;
		case "clearholograms": {
			HoloManager.removeAllHolograms();
		}
			break;
		case "serverinfo":
		{
			if(sender instanceof ConsoleCommandSender) {
				try{
					sender.sendMessage("Server IP: " + InetAddress.getLocalHost().getHostAddress() + ":" + Bukkit.getPort());
				}catch(Exception e) {
					e.printStackTrace();
				}
				break;
			}else {
				try {
					PlayerManager.getUser((Player)sender).openInventory(createServerInfoGui());
				} catch (UnknownHostException e) {
					sender.sendMessage(ChatColor.RED + "Couldn't open server info GUI.");
				}
			}
		}
			break;
		case "reroll": {
			DLUser user = PlayerManager.getUser(sender);
			user.rollNewChallenges();
		}
			break;
		case "lock":
		{
			Deadlight.locked = true;
			sender.sendMessage("Lobbies are now locked.");
		} break;
		case "unlock":
		{
			Deadlight.locked = false;
			sender.sendMessage("Lobbies are now unlocked.");
		} break;
		case "save-all": // Save all player data
			Deadlight.inst.savePlayers();
			sender.sendMessage("Saved all player data!");
			break;
		case "players":
			sender.sendMessage("Registered Players: " + PlayerManager.getPlayers().toString());
			break;
		case "help":
			StringBuilder sb = new StringBuilder("Commands:\n");
			for(Cmd c : commands) {
				sb.append("/dl " + c.name + (c.args == null ? "" : " " + c.args) + " - " + c.description + '\n');
			}
			sender.sendMessage(sb.toString());
			break;
		case "dir": {
			DLUser user = PlayerManager.getUser(sender);
			double x = user.getDirection().getX();
			double y = user.getDirection().getY();
			double z = user.getDirection().getZ();
			user.getPlayer().sendMessage("Direction " + String.format("% ,.2f % ,.2f % ,.2f", x, y, z));
		}
			break;
		default:
			sender.sendMessage("Unknown argument.");
			break;
		}
	}
}
