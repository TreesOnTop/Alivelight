package net.blixate.deadlight.util.maps;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import net.blixate.deadlight.Deadlight;

public class ImageRenderer extends MapRenderer {
	
	public static File MAP_CACHE = new File(Deadlight.dataFolder, "mapcache");
	
	Image image;
	boolean done = false;
	String url = "";
	
	public ImageRenderer() {
	}
	
	public ImageRenderer(File file) {
		try {
			load(file.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public ImageRenderer(String url) {
		load(url);
	}
	
	public boolean load(String url) {
		BufferedImage image = null;
		
		try {
			image = ImageIO.read(new URL(url));
			image = MapPalette.resizeImage(image);
		} catch (IOException e) {
			return false;
		} 
		this.image = image;
		this.url = url;
		return true;
	}
	
	public String getURL() {
		return url;
	}
	
	@Override
	public void render(MapView view, MapCanvas canvas, Player player) {
		if(done)
			return;
		if(image == null) 
			return;
		canvas.drawImage(0, 0, image);
		this.done = true;
	}

}
