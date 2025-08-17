package net.blixate.deadlight.util.maps;

import java.io.File;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapView;

public class MapEventListener implements Listener {
	@EventHandler
	public void onMapInitalize(MapInitializeEvent e) {
		if(MapManager.hasImage(e.getMap().getId())) {
			MapView view = e.getMap();
			view.getRenderers().clear();
			String url = MapManager.getMapUrl(view.getId());
			if(url.startsWith("https://") || url.startsWith("http://")) {
				view.addRenderer(new ImageRenderer(url));
			}else {
				view.addRenderer(new ImageRenderer(new File(url)));
			}
			view.setScale(MapView.Scale.FARTHEST);
			view.setTrackingPosition(false);
		}
	}
}
