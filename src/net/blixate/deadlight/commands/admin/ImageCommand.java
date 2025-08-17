package net.blixate.deadlight.commands.admin;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import net.blixate.deadlight.commands.DLCmd;
import net.blixate.deadlight.util.maps.ImageRenderer;
import net.blixate.deadlight.util.maps.MapManager;
import net.md_5.bungee.api.ChatColor;

public class ImageCommand extends DLCmd {
	
	public ImageCommand() {
		super("deadlight.image", "/image [(file|copy|<url>)] (args...)", false);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void execute() {
		this.requireArgs(1);
		Player player = user.getPlayer();
		if(args.get(0).equalsIgnoreCase("copy")) {
			this.requireArgs(2);
			int mapId = Integer.parseInt(args.get(1));
			ItemStack map = new ItemStack(Material.FILLED_MAP);
			MapMeta meta = (MapMeta)map.getItemMeta();
			meta.setMapView(Bukkit.getMap(mapId));
			map.setItemMeta(meta);
			user.getInventory().addItem(map);
			user.getPlayer().sendMessage(ChatColor.GOLD + "You now have a copy of Map #" + mapId + "!");
			return;
			
		}
		String url = args.get(0);
		MapRenderer renderer;
		if(url.equals("file")) {
			this.requireArgs(2);
			url = args.get(1);
			renderer = new ImageRenderer(new File(url));
		} else {
			renderer = new ImageRenderer();
			if(!((ImageRenderer) renderer).load(url)) {
				this.error("Couldn't load image from URL!");
			}
		}
		MapView view = MapManager.getOrCreateMap(renderer, url);
		player.sendMessage(ChatColor.GOLD + "Image created!");
		ItemStack map = new ItemStack(Material.FILLED_MAP);
		MapMeta meta = (MapMeta)map.getItemMeta();
		meta.setMapView(view);
		map.setItemMeta(meta);
		player.getInventory().addItem(map);
	}
}
