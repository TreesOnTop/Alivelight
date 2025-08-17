package net.blixate.deadlight.lobby;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.kit.items.Items;
import net.blixate.deadlight.kit.items.flowers.FlowerTotem;
import net.blixate.deadlight.kit.perks.PerkEventType;
import net.blixate.deadlight.kit.type.JackOLanternType;
import net.blixate.deadlight.kit.type.NecromancerType;
import net.blixate.deadlight.lobby.score.ScoreEvent;
import net.blixate.deadlight.lobby.score.ScoreType;
import net.blixate.deadlight.maps.environment.ExitPortal;
import net.blixate.deadlight.maps.environment.PlayerSoul;
import net.blixate.deadlight.maps.environment.PortalFragment;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.player.effects.BrokenEffect;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.player.effects.KnockedEffect;
import net.blixate.deadlight.player.effects.MangledEffect;
import net.blixate.deadlight.player.stats.DeadlightStat;
import net.blixate.deadlight.util.ParticlesUtil;

/* Listen for events that would be relevant in a game */
public class GameListener implements Listener {
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if(!PlayerManager.exists(e.getPlayer())) return;
		DLUser user = PlayerManager.getUser(e.getPlayer().getUniqueId());
		if(user.getLobby() == null) return;
		if(user.getPlayer().getGameMode() != GameMode.CREATIVE) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onSwapHands(PlayerSwapHandItemsEvent e) {
		if(!PlayerManager.exists(e.getPlayer())) return;
		e.setCancelled(true);
		DLUser user = PlayerManager.getUser(e.getPlayer().getUniqueId());
		if(user.getLobby() == null) return;
		if(user.hasEffect(KnockedEffect.class)) {
			user.removeEffect(KnockedEffect.class);
			user.getLobby().lobbyDamage(1, user.getLobby().getKiller(), user, AttackType.TRAP);
			user.send("knocked_self_pickup");
		}else {
			Deadlight.getPerkManager().callEvent(user, PerkEventType.PRESS_F);
		}
	}
	
	@EventHandler
	public void onCrouch(PlayerToggleSneakEvent e) {
		if(!PlayerManager.exists(e.getPlayer())) return;
		DLUser user = PlayerManager.getUser(e.getPlayer());
		if(user.getLobby() == null) return;
		if(e.getPlayer().isSneaking())
			Deadlight.getPerkManager().callEvent(user, PerkEventType.CROUCH);
	}
	
	@EventHandler
	public void onDismount(EntityDismountEvent e) {
		Deadlight.debug("Dismounted " + e.getDismounted().toString());
		if(e.getEntity() instanceof Player) {
			DLUser user = PlayerManager.getUser(e.getEntity().getUniqueId());
			if(user.isInMatch()) {
				if(user.isKiller()) {
					if(user.getKillerType() instanceof JackOLanternType) {
						JackOLanternType jack = (JackOLanternType)user.getKillerType();
						jack.dismount(e.getDismounted());
					}
				}else {
					if(e.getDismounted().getType() == EntityType.ARMOR_STAND) {
						e.setCancelled(true);
						// TODO: Maybe allow dismounting to pick you up?
						// You'd take a hit and not receive Determined
					}
				}
				if(!e.isCancelled()) {
					if(e.getDismounted().getType() == EntityType.ARMOR_STAND) {
						e.getDismounted().remove();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onCrouch(PlayerToggleSprintEvent e) {
		if(!PlayerManager.exists(e.getPlayer())) return;
		DLUser user = PlayerManager.getUser(e.getPlayer());
		if(user.getLobby() == null) return;
		Deadlight.getPerkManager().callEvent(user, PerkEventType.SPRINT);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent e) {
		if(!PlayerManager.exists(e.getPlayer())) return;
		DLUser user = PlayerManager.getUser(e.getPlayer());
		if(user.getLobby() == null) return;
		if(user.isSpectating()) { e.setCancelled(true); return; }
		if(!(e.getRightClicked() instanceof Player)) { return; }
		DLUser rightClicked = PlayerManager.getUser(e.getRightClicked());
		if(rightClicked == null) return;
		if(rightClicked.isSpectating()) { e.setCancelled(true); return; }
		if(!user.hasEffect(InfectedEffect.class)) {
			if(user.isHoldingItem(Items.MINTY_FRESH)) {
				if(rightClicked.isKiller()) {
					rightClicked.stun(user, 5);
					user.useItem();
				} else {
					rightClicked.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 1, true, true, true));
					user.sendActionbar("actionbar_stunned_survivor", rightClicked.getName());
					user.useItem();
				}
			}
		}else {
			user.sendActionbar("actionbar_blocked_infected");
		}
		
		
		if(rightClicked.isKiller()) {
			// Another player right-clicked the killer
			Deadlight.getPerkManager().callEvent(user, PerkEventType.KILLER_RIGHT_CLICK, rightClicked);
		} else if(user.isKiller()) {
			// The killer right clicked another player
			Deadlight.getPerkManager().callEvent(user, PerkEventType.KILLER_RIGHT_CLICK_SURVIVOR, rightClicked);
		} else {
			user.getPlayerLoop().afkTicks = 0;
			if(rightClicked.hasEffect(KnockedEffect.class)) {
				rightClicked.removeEffect(KnockedEffect.class);
				user.addScoreEvent(new ScoreEvent("knocked_pickup", ScoreType.BLOOD));
				rightClicked.send("knocked_pickup", user.getName());
				return;
			}
			if(rightClicked.getPlayer().isSprinting() || rightClicked.isMaxHealth() || !user.getPlayer().isSneaking()) {
				return;
			}
			if(rightClicked.hasEffect(BrokenEffect.class)) {
				user.sendActionbar("actionbar_cannot_heal", rightClicked.getName());
				return;
			}
			if(user.hasEffect(InfectedEffect.class)) {
				user.sendActionbar("actionbar_blocked_infected");
				return;
			}
			if(user.genCooldown.isDone()) {
				double speed = user.getHealingSpeed();
				if(rightClicked.hasEffect(MangledEffect.class)) {
					speed = speed * 0.85;
				}
				user.sendProgress(speed, rightClicked.getMatchData().survivorHealingProgress);
				rightClicked.sendActionbar("actionbar_getting_healing", user.getName(), ((int)rightClicked.getMatchData().survivorHealingProgress) + "");
				rightClicked.getPlayerLoop().afkTicks = 0;
				user.addExp(5);
				user.addBloodSilent(1);
				rightClicked.getMatchData().survivorHealingProgress += speed;
				if(rightClicked.getMatchData().survivorHealingProgress >= 100) {
					rightClicked.addHealth(2);
					rightClicked.getMatchData().survivorHealingProgress = 0;
					rightClicked.getStatTracker().incrementStat(DeadlightStat.HEALS_RECEIVED);
					user.getStatTracker().incrementStat(DeadlightStat.HEALS_GIVEN);
					user.addScoreEvent(new ScoreEvent("healed", ScoreType.BLOOD));
					user.addExp(50);
					user.sendActionbar("actionbar_healed_survivor", rightClicked.getName());
					rightClicked.removeEffect(MangledEffect.class);
					Deadlight.getPerkManager().callEvent(user, PerkEventType.SURVIVOR_HEAL, rightClicked);
				}
				user.genCooldown.start(user.getLobby().settings.healingCooldown);
			}
		}
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractAtEntityEvent event) {
		if(!PlayerManager.exists(event.getPlayer())) return;
		DLUser user = PlayerManager.getUser(event.getPlayer());
		if(user == null) {
			return;
		}
		if(user.getLobby() == null) {
			return;
		}
		if(user.isSpectating()) {
			return;
		}
		if(user.isKiller() && !Deadlight.debug) {
			return;
		}
		if(event.getRightClicked().getType().equals(EntityType.ARMOR_STAND)) {
			if(user.hasEffect(InfectedEffect.class)) {
				user.sendActionbar("actionbar_blocked_infected");
				return;
			}
			Entity entity = event.getRightClicked();
			PlayerSoul soul = user.getLobby().getPlayerSoul(entity);
			if(soul == null) {
				return;
			}
			if(user.getInventory().contains(Material.PLAYER_HEAD)) {
				user.sendActionbar("player_soul_inventory_full");
				return;
			}
			user.send("player_soul_pickup", soul.getPlayerName());
			user.addScoreEvent(new ScoreEvent("pickup_soul", ScoreType.BLOOD));
			user.getLobby().removePlayerSoul((ArmorStand)entity);
			user.getInventory().addItem(soul.getItemStack());
			Deadlight.getPerkManager().callEvent(user, PerkEventType.SOUL_PICKUP, user);
			Deadlight.getPerkManager().callEvent(user.getLobby().getKiller(), PerkEventType.SOUL_PICKUP, user);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(!PlayerManager.exists(e.getPlayer())) return;
		DLUser user = PlayerManager.getUser(e.getPlayer().getUniqueId());
		if(user.getLobby() == null) return;
		if(user.isSpectating()) { e.setCancelled(true); return; }
		Lobby lobby = user.getLobby();
		if(lobby.isKiller(user)) {
			ItemStack item = user.getInventory().getItemInMainHand();
			if(item != null) {
				user.getKillerType().clickItem(item, (e.hasBlock() ? e.getClickedBlock() : null));
				if(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
					user.getKillerType().leftClickItem(item, (e.hasBlock() ? e.getClickedBlock() : null), e.getBlockFace());
				}
				if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					user.getKillerType().rightClickItem(item, (e.hasBlock() ? e.getClickedBlock() : null), e.getBlockFace());
				}
			}
		}else {
			if(!user.isInMatch()) return;
			// move this up above item event
			if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					if(user.isHoldingItem(Items.FLOWER_DAISY) || user.isHoldingItem(Items.FLOWER_LILY) || user.isHoldingItem(Items.FLOWER_TULIP) || user.isHoldingItem(Items.FLOWER_WITHER)) {
						if(e.getBlockFace() == BlockFace.UP) {
							Location flowerLocation = e.getClickedBlock().getLocation().add(0, 1, 0);
							if(flowerLocation.getBlock().getType().equals(Material.AIR)) {
								lobby.placeFlowerTotem(flowerLocation, user);
								user.useItem();
							}else {
								e.setCancelled(true);
								user.send("flower_cant_place");
							}
						}
					}
				}
				if(!user.hasEffect(InfectedEffect.class)) {
					if(user.isHoldingItem(Items.FLASH)) {
						user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 0, false, false, false));
						user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 5, 0, false, false, false));
						user.sendActionbar("actionbar_flash_powder");
						ParticlesUtil.spawnParticle(Particle.FLAME, user.getLocation().add(0, 1, 0), 100, 0.1, .5, .5, .5);
						user.useItem();
					}
					if(user.isHoldingItem(Items.BANDAGE)) {
						if(user.hasEffect(BrokenEffect.class)) {
							user.sendActionbar("actionbar_cannot_heal_self");
						}
						else if(!user.isMaxHealth() && !user.hasEffect(MangledEffect.class)) {
							user.sendActionbar("actionbar_healed_self");
							user.addHealth(2);
							user.useItem();
						}
					}
					if(user.isHoldingItem(Items.AIR_BAG)) {
						user.push(user.getDirection(), 2d);
						user.useItem();
					}
					/*if(user.isHoldingItem(Items.GAMBLERS_DICE)) {
						int random = Deadlight.RNG.nextInt(6);
						switch(random) {
						case 0:
							user.addHealth(2);
							break;
						case 1:
							user.applyEffect(new SpeedEffect(0.25f, 5000));
							break;
						case 2:
							lobby.lobbyDamage(2, null, user, AttackType.TRAP);
							break;
						case 3:
							user.getPlayer().teleport(lobby.getKiller().getLocation());
							break;
						case 4:
							user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1, false, false, false));
							break;
						case 5:
						default: break;
						}
					}*/
					if(user.isHoldingItem(Items.SHREAK)) {
						e.setCancelled(true);
						DLUser player = lobby.getKiller();
						player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 0));
						Vector p1 = user.getLocation().toVector();
						Vector p2 = player.getLocation().toVector();
						float angle = p2.angle(p1);
						Location newLoc = player.getLocation();
						newLoc.setYaw(angle);
						player.getPlayer().teleport(newLoc);
						user.useItem();
					}
					if(user.isHoldingItem(Items.ZOOM)) {
						e.setCancelled(true);
						Location originalLocation = user.getLocation();
						Location targetLocation = user.getTargetLocation(16);
						if(targetLocation == null) {
							return;
						}
						ParticlesUtil.spawnParticle(Particle.REVERSE_PORTAL, originalLocation.add(0,1,0), 20, 0.2, 0, 1, 0);
						targetLocation.setYaw(user.getLocation().getYaw());
						targetLocation.setPitch(user.getLocation().getPitch());
						user.getPlayer().teleport(targetLocation);
						user.useItem();
					}
				}else {
					user.sendActionbar("actionbar_blocked_infected");
				}
			}
		}
		
		// gens and exit portal
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(!lobby.isKiller(user) || Deadlight.debug) {
				user.getPlayerLoop().afkTicks = 0;
				Block b = e.getClickedBlock();
				if(b.getType().equals(Material.SOUL_SAND)) {
					user.getPlayerLoop().afkTicks = 0;
					e.setUseItemInHand(Result.DENY);
					PortalFragment gen = lobby.getGenerator(b.getLocation());
					if(gen != null) {
						if(user.genCooldown.isDone()) {
							if(gen.activate(user)) {
								Vector half = new Vector(0.5, 0.5, 0.5);
								ParticlesUtil.spawnParticle(Particle.FIREWORKS_SPARK, b.getLocation().add(half), 50, 0, 1, 1, 1);
								gen.finish(lobby, false);
								for(DLUser player : lobby.getPlayers()) {
									if(player.isKiller()) {
										player.playSound(Sound.BLOCK_GLASS_BREAK, .5);
									}else {
										player.playSound(Sound.BLOCK_NOTE_BLOCK_BELL, .5);
										if(!player.isSpectating())
											player.getStatTracker().incrementStat(DeadlightStat.FRAGMENTS_COMPLETED);
									}
								}
								Deadlight.getPerkManager().callEvent(lobby, PerkEventType.OBJECTIVE_COMPLETE);
								if(lobby.gensDone >= lobby.getRequiredGens()) {
									Deadlight.getPerkManager().callEvent(lobby, PerkEventType.ALL_OBJECTIVES_COMPLETED);
									
									lobby.startEndGame(); // We are done!
									Deadlight.debug("Last objective was completed");
								}
							}
						}
					}
				}
				if(b.getType().equals(Material.END_PORTAL_FRAME)) {
					if(user.hasEffect(InfectedEffect.class)) {
						user.sendActionbar("actionbar_blocked_infected");
						return;
					}
					PortalFragment gen = lobby.getGenerator(b.getLocation());
					if(gen != null) {
						if(user.isHoldingSoul()) {
							if(gen.isReviving()) {
								return;
							}
							String uuid = PlayerSoul.getOwnerUUID(user.getInventory().getItemInMainHand());
							user.send("player_soul_dropoff", PlayerSoul.getPlayerName(uuid));
							user.getInventory().setItem(user.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR));
							gen.setReviving(uuid, user.getLobby());
							user.addExp(200);
							user.addScoreEvent(new ScoreEvent("placed_soul", ScoreType.BLOOD));
							user.getStatTracker().incrementStat(DeadlightStat.REVIVES_GIVEN);
							if(lobby.isEndGame()) {
								user.getStatTracker().incrementStat(DeadlightStat.END_GAME_REVIVES_GIVEN);
							}
							Deadlight.getPerkManager().callEvent(lobby, PerkEventType.SOUL_PLACE);
							DLUser reviving = PlayerManager.getUser(UUID.fromString(uuid));
							reviving.sendTitle("player_soul_being_revived_title", "player_soul_being_revived_subtitle");
						}
					}
				}
				if(b.getType().equals(Material.REDSTONE_LAMP)) {
					ExitPortal gate = lobby.getPortal(b.getLocation());
					if(gate != null) {
						Lightable lit = (Lightable)b.getBlockData();
						if(lit.isLit()) {
							if(gate.cool.isDone()) {
								if(lobby.getSoulsCarrying(user).size() != 0) {
									user.send("player_soul_while_escaping");
									return;
								}else {
									lobby.survivorEscape(user);
								}
							}
						} else {
							if(gate.activate(user)) {
								Vector half = new Vector(0.5, 0.5, 0.5);
								ParticlesUtil.spawnParticle(Particle.FIREWORKS_SPARK, b.getLocation().add(half), 50, 0, 1, 1, 1);
								lit.setLit(true);
								b.setBlockData(lit);
								user.addExp(100);
								lobby.portalsOpened++;
								Deadlight.getPerkManager().callEvent(lobby, PerkEventType.EXIT_PORTAL_OPENED);
								for(DLUser player : lobby.getPlayers()) {
									player.playSound(Sound.BLOCK_BEACON_ACTIVATE, 2);
									player.addExp(50);
								}
							}
						}
					}
				}
			}
			e.setCancelled(true);
			e.setUseInteractedBlock(Result.DENY);
			e.setUseItemInHand(Result.DENY);
		}else if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
			if(user.isKiller()) {
				Block b = e.getClickedBlock();
				if(b.getType().equals(Material.END_PORTAL_FRAME)) {
					e.setCancelled(true);
					PortalFragment gen = lobby.getGenerator(b.getLocation());
					if(gen != null) {
						if(gen.display != null) {
							DLUser victim = PlayerManager.getUser(UUID.fromString(gen.reviving));
							for(DLUser player : lobby.getPlayers()) {
								player.send("player_soul_destroyed", victim.getName());
								player.playSound(Sound.BLOCK_BELL_USE, 2);
							}
							gen.display.remove();
							gen.reviving = null;
							user.addExp(150);
							user.addScoreEvent(new ScoreEvent("soul_destroyed", ScoreType.BLOOD));
							user.getStatTracker().incrementStat(DeadlightStat.SOULS_DESTROYED);
							victim.clearBloodBuffer();
							lobby.checkState();
							ParticlesUtil.spawnParticle(Particle.FLAME, b.getLocation().add(.5,.5,.5), 100, 0, 0.5, 0.5, 0.5);
							Deadlight.getPerkManager().callEvent(lobby, PerkEventType.SOUL_DESTROY);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onTarget(EntityTargetEvent e) {
		if(e.getEntityType().equals(EntityType.ZOMBIE)) {
			if(e.getTarget() instanceof Player) {
				DLUser user = PlayerManager.getUser(e.getTarget());
				if(user.isKiller() || user.isSpectating()) {
					e.setCancelled(true);
				}else {
					user.playSound(Sound.ENTITY_SKELETON_AMBIENT, 0);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if(e.getEntityType().equals(EntityType.ZOMBIE)) {
			Entity entity = e.getEntity();
			PersistentDataContainer container = entity.getPersistentDataContainer();
			if(container.has(NecromancerType.MINION_OWNER, PersistentDataType.STRING)) {
				String ownerId = container.get(NecromancerType.MINION_OWNER, PersistentDataType.STRING);
				DLUser owner = PlayerManager.getUser(UUID.fromString(ownerId));
				NecromancerType necromancer = (NecromancerType) owner.getKillerType();
				necromancer.removeZombie(entity);
				owner.send("necromancer_minion_died", entity.getCustomName());
				owner.playSound(Sound.ENTITY_WITHER_SKELETON_HURT, 2);
				e.getDrops().clear();
				e.setDroppedExp(0);
			}
		}
	}
	
	@EventHandler
	public void onPickup(EntityPickupItemEvent e) {
		if(!(e.getEntity() instanceof Player)) {
			e.setCancelled(true);
			return;
		}
		if(!PlayerManager.exists((Player)e.getEntity())) return;
		DLUser user = PlayerManager.getUser(e.getEntity().getUniqueId());
		if(user.getPlayer().getGameMode() == GameMode.CREATIVE) {
			e.setCancelled(false);
			return;
		}
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		e.setDropItems(false);
		if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			e.setCancelled(true);
		}
		if(!PlayerManager.exists(e.getPlayer())) { 
			e.setCancelled(true);
			return;
		}
		DLUser user = PlayerManager.getUser(e.getPlayer().getUniqueId());
		if(user.getLobby() == null) {
			return;
		}
		if(!user.isKiller()) {
			return;
		}
		// gens breaking
		Block b = e.getBlock();
		Material mat = b.getType();
		if(mat.equals(Material.SOUL_SAND)) {
			PortalFragment gen = (PortalFragment)user.getLobby().getGenerator(b.getLocation());
			if(gen != null) {
				if(user.genCooldown.isDone()) {
					gen.destroy(user);
					user.getStatTracker().incrementStat(DeadlightStat.FRAGMENTS_SABOTAGED);
				}else{
					int seconds = (int)(user.getLobby().settings.killerBreakCooldown);
					user.sendActionbar("killer_gen_break_cooldown", ""+seconds);
				}
			}
		}
		else {
			FlowerTotem destroyedTotem = null;
			for(FlowerTotem flower : user.getLobby().getFlowers()) {
				if(flower.getLocation().equals(b.getLocation())) {
					destroyedTotem = flower;
					break;
				}
			}
			if(destroyedTotem != null) {
				destroyedTotem.destroy();
				user.getLobby().getFlowers().remove(destroyedTotem);
				user.getStatTracker().incrementStat(DeadlightStat.FLOWER_TOTEMS_DESTROYED);
				destroyedTotem.getOwner().send("flower_destroyed");
				destroyedTotem.getOwner().playSound(Sound.ENTITY_ENDERMAN_SCREAM, 0);
				World world = destroyedTotem.getLocation().getWorld();
				world.playSound(destroyedTotem.getLocation(), Sound.BLOCK_WET_GRASS_BREAK, 1, 0);
				world.playSound(destroyedTotem.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.2f, 2);
				e.setCancelled(false);
			}
		}
	}
	
	@EventHandler
	public void onItemFrameBreak(HangingBreakEvent e) {
		if(e.getEntity() instanceof ItemFrame) {
			if(e.getCause() == RemoveCause.PHYSICS || e.getCause() == RemoveCause.OBSTRUCTION) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(e.getCause() != DamageCause.ENTITY_ATTACK &&
				e.getCause() != DamageCause.CUSTOM && e.getCause() != null &&
				e.getCause() != DamageCause.VOID && e.getCause() != DamageCause.SUICIDE && e.getCause() != DamageCause.KILL) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onHorseDamage(EntityDamageEvent e) {
		if(e.getEntityType() == EntityType.SKELETON_HORSE) {
			if(e.getCause() == DamageCause.ENTITY_ATTACK) {
				Deadlight.debug("Entity "+e.getEntity()+" damaged by another entity");
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && !(e.getDamager() instanceof Player)) {
			if(!PlayerManager.exists((Player)e.getEntity())) return;
			e.setCancelled(true);
			DLUser victim = PlayerManager.getUser(e.getEntity());
			if(victim.isInMatch()) {
				DLUser killer = victim.getLobby().getKiller();
				killer.getKillerType().entityHitSurvivor(e.getDamager(), victim);
			}
			return;
		}
		if((e.getDamager() instanceof Player) && !(e.getEntity() instanceof Player)) {
			if(!PlayerManager.exists((Player)e.getDamager())) return;
			e.setCancelled(true);
			Entity entity = e.getEntity();
			DLUser user = PlayerManager.getUser(e.getDamager().getUniqueId());
			if(user.isKiller()) {
				user.getKillerType().hitEntity(entity);
			}else if(user.isInMatch() && !user.isSpectating()) {
				user.getLobby().getKiller().getKillerType().survivorHitEntity(entity, user);
			}
		}
		if(!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) {
			e.setCancelled(true);
			return;
		}
		
		if(!PlayerManager.exists((Player)e.getDamager())) return;
		if(!PlayerManager.exists((Player)e.getEntity())) return;
		DLUser user = PlayerManager.getUser(e.getDamager().getUniqueId());
		DLUser victim = PlayerManager.getUser(e.getEntity().getUniqueId());
		if(user == null || victim == null) return;
		Lobby lobby = user.getLobby();
		if(!user.isInMatch() || !victim.isInMatch()) {
			e.setCancelled(true);
			return;
		}else {
			e.setCancelled(true);
			if(victim.isSpectating()) {
				return;
			}
			if(user.isSpectating()) {
				return;
			}
			if(victim.isKiller()) {
				Deadlight.getPerkManager().callEvent(user, PerkEventType.KILLER_PUNCH);
				Deadlight.getPerkManager().callEvent(lobby.getKiller(), PerkEventType.KILLER_PUNCH, user);
				return;
			}
			// Check to see if the killer tried to hit a survivor
			Deadlight.getPerkManager().callEvent(user, PerkEventType.ATTEMPT_PLAYER_HIT, victim);
			if(!lobby.isKiller(user) || !user.hitCooldown.isDone() || !user.getKillerType().canAttack()) {
				return;
			}
			ParticlesUtil.spawnBlock(victim.getLocation(), Material.REDSTONE_BLOCK, 20);
			if(!victim.getMatchData().hasTakenDamage) {
				user.addExp(200);
				Deadlight.getPerkManager().callEvent(user, PerkEventType.SURVIVOR_FIRST_HIT, victim);
				Deadlight.getPerkManager().callEvent(victim, PerkEventType.SURVIVOR_FIRST_HIT);
				ParticlesUtil.spawnRedstone(victim.getLocation().add(0, 1, 0), Color.fromRGB(0x7f0000), 2, 10);
				victim.getMatchData().hasTakenDamage = true;
				boolean allSurvivorsHit = true;
				for(DLUser survivor : lobby.getSurvivors()) {
					if(!survivor.getMatchData().hasTakenDamage) {
						allSurvivorsHit = false;
						break;
					}
				}
				if(allSurvivorsHit) {
					user.getStatTracker().incrementStat(DeadlightStat.ALL_SURVIVORS_HIT);
				}
			}
			Deadlight.getPerkManager().callEvent(user, PerkEventType.SURVIVOR_HIT);
			Deadlight.getPerkManager().callEvent(victim, PerkEventType.SURVIVOR_HIT);
			if(victim.isObsession()) {
				user.addExp(50);
				Deadlight.getPerkManager().callEvent(user, PerkEventType.OBSESSION_HIT);
			}
			Deadlight.getPerkManager().callEvent(victim, PerkEventType.SURVIVOR_HEALTH_CHANGED);
			lobby.lobbyDamage(2, user, victim, AttackType.BASIC);
		}
	}
}
