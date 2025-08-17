package net.blixate.deadlight.util.maps;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class AnimationRenderer extends MapRenderer{

	@Override
	public void render(MapView view, MapCanvas canvas, Player player) {
		for(int x = 0; x < 127; x ++) {
			for(int y = 0; y < 127; y++) {
				canvas.setPixel(x, y, (byte)Math.abs(((Math.random()) * 127)));
			}
		}
	}

}
