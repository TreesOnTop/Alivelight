package net.blixate.deadlight.kit.type;

import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import net.blixate.deadlight.maps.environment.MapObject;
import net.blixate.deadlight.maps.environment.PortalFragment;
import net.blixate.deadlight.maps.environment.states.BlockedState;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.util.time.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class VisitorType extends KillerType {

	enum Mode {
		NEUTRAL, SEEKING;
	}
	
	ItemStack normalHead;
	ItemStack seekingHead;
	String seekingHeadString = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODUyZWVhZjg5OGQ0YzczNTk3NTEwOTljZTc2ZDI0NjYyYmVmYWE4OTEwM2UzYjJiZjAyN2MwYWE4NTFkOSJ9fX0=";
	
	Mode mode;
	
	Cooldown modeSwitchCooldown;
	Cooldown visitCooldown;
	
	private double range = 40;
	private int visitCooldownTime = 20;
	
	@Override
	public void equipKiller(PlayerInventory inv) {
		quickEquip(user, inv);
		modeSwitchCooldown = new Cooldown();
		visitCooldown = new Cooldown();
		setMode(Mode.NEUTRAL);
		// cache these
		if(getKillerData().inDarkCommand) {
			normalHead = getHead(KillerTypes.VISITOR.getData().getString("skins.DARK_COMMAND.head"), "Visitor");
		}else {
			normalHead = getHead("Visitor");
		}
		inv.setHelmet(normalHead);
		seekingHead = getHead(seekingHeadString, "Seeking");
		
		if(hasAddon("hivemind")) {
			range += 16;
			visitCooldownTime += 10;
		}
		if(hasAddon("eye_of_recharge")) {
			visitCooldownTime -= 5;
		}
	}
	
	@Override
	public void rightClickItem(ItemStack item, Block clickedBlock, BlockFace face) {
		if(item.getType().equals(Material.NETHER_WART)) {
			if(!modeSwitchCooldown.isDone()) {
				return;
			}
			setMode(Mode.SEEKING);
		}else if(item.getType().equals(Material.RED_DYE)) {
			if(!modeSwitchCooldown.isDone()) {
				return;
			}
			setMode(Mode.NEUTRAL);
		}else if(item.getType().equals(Material.FIREWORK_STAR)) {
			if(!visitCooldown.isDone()) {
				user.sendActionbar("ability_cooldown", visitCooldown.secondsLeft());
				return;
			}
			// ray cast
			Player player = user.getPlayer();
			Vector direction = user.getDirection();
			World world = user.getLocation().getWorld();
			RayTraceResult result = world.rayTraceEntities(player.getEyeLocation().add(direction.multiply(1.5)), direction, range, new Predicate<Entity>() {
				public boolean test(Entity hitEntity) {
					if(hitEntity == null || !(hitEntity instanceof Shulker))
						return false;
					// Check this is a fragment
					Shulker shulker = (Shulker)hitEntity;
					if(shulker.isGlowing()) {
						return true;
					}
					return false;
				}
			});
			if(result == null) {
				return;
			}
			if(hasAddon("f_strain")) {
				MapObject object = user.getLobby().getGenerator(result.getHitEntity().getLocation());
				if(object != null) {
					PortalFragment frag = (PortalFragment)object;
					frag.destroy(null); // This is a ability destroy, not an player destroy. Don't send feedback.
				}
			}
			Location originalLocation = user.getLocation().clone();
			Shulker shulker = (Shulker) result.getHitEntity();
			Location loc = shulker.getLocation().clone();
			player.teleport(loc.add(0, 1.1, 0));
			if(!hasAddon("intruder")) {
				world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 2, 0);
			}
			visitCooldown.start(visitCooldownTime);
			modeSwitchCooldown.start(5f);
			setMode(Mode.NEUTRAL);
			user.hitCooldown.start(2f);
			if(!hasAddon("eye_of_fear")) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false, false));
			}
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 0, false, false, false));
			if(hasAddon("hivemind")) {
				MapObject object = user.getLobby().getGenerator(result.getHitEntity().getLocation());
				if(object != null) {
					PortalFragment frag = (PortalFragment)object;
					frag.addState(new BlockedState(5000));
				}
				if(originalLocation.distance(loc) < 16) {
					for(DLUser surv: user.getLobby().getSurvivors()) {
						if(surv.getLocation().distance(user.getLocation()) < 5) {
							surv.applyEffect(new InfectedEffect(10000));
						}
					}
				}
			}
		}
	}
	
	public void setMode(Mode mode) {
		if(this.mode == mode) {
			// Changed modes
			return;
		}
		this.mode = mode;
		ItemStack item;
		PlayerInventory inv = user.getInventory();
		if(mode == Mode.NEUTRAL) {
			user.getPlayer().removePotionEffect(PotionEffectType.SPEED);
			user.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
			item = new ItemStack(Material.NETHER_WART);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Seek" + INPUT_TYPE_RIGHT_CLICK);
			item.setItemMeta(meta);
			inv.setItem(2, null); // Can't visit while in Neutral mode!
			inv.setHelmet(normalHead);
		}else {
			if(!hasAddon("manifest")) {
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false, false));
				user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false, false));
				user.getPlayer().setSprinting(false);
			}
			item = new ItemStack(Material.RED_DYE);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Return" + INPUT_TYPE_RIGHT_CLICK);
			item.setItemMeta(meta);
			// show visit item
			ItemStack visitItem = new ItemStack(Material.FIREWORK_STAR);
			meta = visitItem.getItemMeta();
			meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Visit" + INPUT_TYPE_RIGHT_CLICK);
			visitItem.setItemMeta(meta);
			inv.setItem(2, visitItem);
			inv.setHelmet(seekingHead);
		}
		inv.setItem(1, item);
		modeSwitchCooldown.start(.75f);
	}
	public void tick() {
		if(mode == Mode.SEEKING) {
			Location closest = null;
			for(DLUser surv : user.getLobby().getSurvivors()) {
				Location loc = surv.getLocation();
				if(closest == null) {
					closest = loc;
					continue;
				}else {
					if(loc.distance(user.getLocation()) < closest.distance(user.getLocation())) {
						closest = loc;
					}
				}
			}
			user.getPlayer().playSound(closest, getTerrorSound(), hasAddon("eye_of_sorrow") ? 2.5f : 2, 1);
		}
	}
	
	public Sound getTerrorSound() {
		if(mode == Mode.SEEKING) {
			return Sound.BLOCK_NOTE_BLOCK_BASS;
		}
		return Sound.BLOCK_NOTE_BLOCK_BASEDRUM;
	}
	
	@Override
	public int getTerrorRadius() {
		if(mode == Mode.NEUTRAL) {
			return 24;
		}else {
			if(hasAddon("intruder")) {
				return 24;
			}else {
				return 32;
			}
		}
	}
}
