package net.blixate.deadlight.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class TPUtil {
	private TPUtil() {}
	
	public static RayTraceResult raytraceBlocks(Player player, double range) {
		RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getLocation().getDirection(), range);
		return result;
	}
	
	public Location getTargetLocation(Player player, double range) {
		RayTraceResult result = raytraceBlocks(player, range);
		if(result != null && result.getHitBlock() != null) {
			Vector pos = result.getHitPosition();
			return pos.toLocation(player.getWorld());
		}
		return null;
	}
}
