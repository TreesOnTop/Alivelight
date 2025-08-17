package net.blixate.deadlight.util.nms;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

import net.blixate.deadlight.Deadlight;
import net.minecraft.server.MinecraftServer;

public class NMSHandler {
	private static Class<?> minecraftServerClass;
	
	static {
		try {
			minecraftServerClass = MinecraftServer.class;
		} catch(SecurityException e) {
			Deadlight.error(e);
		}
	}
	
	public static String getNMSVersion() {
		String version = Bukkit.getServer().getClass().getPackage().getName();
		version = version.substring(version.lastIndexOf('.') + 1);
		return version;
	}
	
	public static String getRoundedTPS() {
		double[] tps = getTPS();
		double recentTps = tps[0];
		return roundTps(recentTps);
	}
	
	public static String roundTps(double ticks) {
		double tps = Math.min(ticks, 20);
		final String string = (double)((double)Math.round(tps * 100) / 100.0d) + "";
		return tps >= 19.99 ? "20.0" : string;
	}
	
	public static double[] getTPS() {
		try {
			Method m = minecraftServerClass.getMethod("getServer");
			Object server = m.invoke(null);
			Field recentTps = server.getClass().getField("recentTps");
			return (double[]) recentTps.get(server);
		} catch (NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
}
