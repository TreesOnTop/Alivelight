package net.blixate.deadlight.util.maps;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class InteractiveMapRenderer extends MapRenderer {

	@Override
	public void render(MapView view, MapCanvas canvas, Player player) {
		drawRect(canvas, 32, 5, 5, 20, 20);
	}
	
	public void drawRect(MapCanvas canvas, int color, int x, int y, int width, int height) {
		int x1 = x;
		int y1 = y;
		while(y1 < height) {
			while(x1 < width) {
				canvas.setPixel(x1, y1, (byte)color);
				x1++;
			}
			x1 = x;
			y1++;
		}
	}

}
