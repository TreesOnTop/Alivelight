package net.blixate.deadlight.kit.cosmetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.ParticlesUtil;

public enum KillEffect {
	NONE(Material.BARRIER),
	POOF(Material.SNOWBALL, 15000),
	FLAME(Material.BLAZE_POWDER, 15000),
	SOUL(Material.SOUL_SAND, 20000),
	SMITE(Material.BLAZE_ROD, 25000),
	EXPLOSION(Material.TNT, 25000),
	JAWS_OF_FEAR(Material.SHEARS, 25000),
	MIDAS(Material.GOLD_BLOCK, 100000),
	DERAILED(Material.RAIL, 100000);
	
	Material icon;
	int cost;
	
	KillEffect(Material icon) {
		this.icon = icon;
		this.cost = 0;
	}
	
	KillEffect(Material icon, int cost) {
		this.icon = icon;
		this.cost = cost;
	}
	
	public ItemStack getItem(DLUser user) {
		ItemStack item = new ItemStack(icon, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(FormatUtil.color("&e" + properName()));
		List<String> lore = new ArrayList<String>();
		lore.add("&8Kill Effect");
		lore.add("");
		if(!user.purchasedKillEffects.contains(this.name()) && cost > 0) {
			lore.add(FormatUtil.color("&7Click to purchase for &e" + FormatUtil.formatLong(cost) + " Blood&7."));
		}
		else {
			if(user.killEffect == this) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				lore.add(FormatUtil.color("&aSelected"));
			}
			else {
				lore.add(FormatUtil.color("&7Click to select"));
			}
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public String properName() {
		return properCase(this.name().toLowerCase());
	}
	
	static String properCase(String s) {
		String[] split = s.split("_");
		for(int i = 0; i < split.length; i++) {
			split[i] = split[i].toUpperCase().charAt(0) + split[i].substring(1, split[i].length());
		}
		return String.join(" ", split);
	}
	
	public void play(Location location) {
		World w = location.getWorld();
		// put kill effect animations here
		switch(this) {
		case NONE:
			break;
		case JAWS_OF_FEAR:
			w.spawn(location, EvokerFangs.class);
			break;
		case SMITE:
			w.strikeLightningEffect(location);
			break;
		case FLAME:
			w.spawnParticle(Particle.FLAME, location.add(new Vector(0, 1, 0)), 50, 0.1, 0.1, 0.1, 0.1);
			break;
		case POOF:
			w.spawnParticle(Particle.CLOUD, location.add(new Vector(0, 1, 0)), 50, 0.1, 0.1, 0.1, 0.1);
			break;
		case SOUL:
			w.spawnParticle(Particle.SOUL, location.add(new Vector(0, 1, 0)), 50, 0.1, 0.1, 0.1, 0.1);
			break;
		case MIDAS:
			spawnItems(new Material[] { Material.GOLD_NUGGET, Material.GOLD_BLOCK, Material.GOLD_INGOT }, w, location);
			break;
		case DERAILED:
			spawnItems(new Material[] { Material.RAIL, Material.ACTIVATOR_RAIL, Material.DETECTOR_RAIL }, w, location);
			break;
		case EXPLOSION:
			ParticlesUtil.createFakeExplosion(location.add(new Vector(0, 1, 0)));
			break;
		default:
			break;
		}
	}
	
	public void spawnItems(Material[] mat, World w, Location location) {
		Random rng = new Random();
		for(int i = 0; i < 20; i++) {
			ItemStack item = new ItemStack(mat[rng.nextInt(mat.length)]);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("" + rng.nextInt());
			item.setItemMeta(meta);
			Item e = w.dropItem(location.add(new Vector(rng.nextFloat() - 0.5, 1, rng.nextFloat() - 0.5)), item);
			e.setPickupDelay(1000);
			e.setVelocity(new Vector(rng.nextFloat() - 0.5, rng.nextFloat() - 0.5, rng.nextFloat() - 0.5));
			e.setTicksLived(20*60*5 - 150);
		}
	}
}
