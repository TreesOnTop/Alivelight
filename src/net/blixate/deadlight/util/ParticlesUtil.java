package net.blixate.deadlight.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticlesUtil {
	
	public static void spawnParticle(Particle p, Location loc, int amount, double speed, double deltaX, double deltaY, double deltaZ) {
		World w = loc.getWorld();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		w.spawnParticle(p, x, y, z, amount, deltaX, deltaY, deltaZ, speed);
	}
	
	public static void spawnRedstone(Location loc, Color color, int size, int amount) {
		// this player is injured!
		World w = loc.getWorld();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		Particle.DustOptions options = new Particle.DustOptions(color, size);
		w.spawnParticle(Particle.REDSTONE, x, y, z, amount, options);
	}

	public static void spawnBlock(Location loc, Material block, int amount) {
		World w = loc.getWorld();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		BlockData data = block.createBlockData();
		w.spawnParticle(Particle.BLOCK_CRACK, x, y, z, amount, 0, 0, 0, data);
	}
	private static Vector getDirectionBetweenLocations(Location start, Location end) {
        Vector from = start.toVector();
        Vector to = end.toVector();
        return to.subtract(from);
    }
	public static void drawLine(Particle particle, Location loc1, Location loc2) {
		drawLine(particle, loc1, loc2, 0.5);
	}
	
	public static void drawLine(Particle particle, Location loc1, Location loc2, double spacing) {
		Vector vector = getDirectionBetweenLocations(loc1, loc2);
        for (double i = 1; i <= loc1.distance(loc2); i += spacing) {
            vector.multiply(i);
            loc1.add(vector);
            spawnParticle(particle, loc1, 1, 0, 0, 0, 0);
            loc1.subtract(vector);
            vector.normalize();
        }
	}
	
	public static void drawLineForPlayers(Particle particle, Location loc1, Location loc2, double spacing, Player...players) {
		Vector vector = getDirectionBetweenLocations(loc1, loc2);
        for (double i = 1; i <= loc1.distance(loc2); i += spacing) {
            vector.multiply(i);
            loc1.add(vector);
    		double x = loc1.getX();
    		double y = loc1.getY();
    		double z = loc1.getZ();
    		for(Player player : players) {
    			player.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
    		}
            loc1.subtract(vector);
            vector.normalize();
        }
	}

	public static void createFakeExplosion(Location loc) {
		World w = loc.getWorld();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		w.spawnParticle(Particle.EXPLOSION_LARGE, x, y, z, 10, 1, 1, 1, 1);
		w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 4, 1);
	}
	
}
