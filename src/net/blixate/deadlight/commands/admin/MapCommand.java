package net.blixate.deadlight.commands.admin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.commands.DLCmd;
import net.blixate.deadlight.maps.GameMap;
import net.blixate.deadlight.maps.MapClearer;
import net.blixate.deadlight.maps.MapLoader;
import net.blixate.deadlight.maps.environment.MapPosition;
import net.blixate.deadlight.maps.structure.StructureFile;
import net.blixate.deadlight.util.FormatUtil;
import net.md_5.bungee.api.ChatColor;

public class MapCommand extends DLCmd {

	public MapCommand() {
		super("deadlight.map", "/map (create|delete|list|help) [options...]");
	}
	
	@Override
	public void execute() {
		if(hasNoArgs()) {
			errorUsage();
		}
		if(hasArg(0)) {
			if(checkArgEqual(0, "list")) {
				send("List of maps:");
				for(GameMap map : MapLoader.getMaps()) {
					send(" - " + map.getName() + (map.isBeta() ? ChatColor.GOLD + " (BETA)" : ""));
				}
				return;
			}
			else if(checkArgEqual(0, "create")) {
				if(hasArg(2)) {
					// options parser
					Map<String, String> options = parseOptions(2);
					
					Material fragmentMaterial = options.containsKey("frag") ? Material.valueOf(options.get("frag").toUpperCase()) : Material.PINK_WOOL;
					Material spawnMaterial = options.containsKey("spawn") ? Material.valueOf(options.get("spawn").toUpperCase()) : Material.LIGHT_BLUE_WOOL;
					Material portalMaterial = options.containsKey("portal") ? Material.valueOf(options.get("portal").toUpperCase()) : Material.PURPLE_WOOL;
					
					send(options.toString());
					/* You are about to see the least efficient and most messy map creation code
					 * you've possibly ever seen. There are too many places to count where this
					 * can be optimized to reduce File I/O calls, MC world accesses, and object
					 * creation. Did I optimize it? Nope. Am I going to? Maybe.*/
					Location offset = null;
					List<MapPosition> spawnLocations = null;
					try {
						errorIf(fragmentMaterial == null, "Unknown fragment material: " + options.get("frag"));
						errorIf(portalMaterial == null, "Unknown portal material: " + options.get("portal"));
						errorIf(spawnMaterial == null, "Unknown spawn material: " + options.get("spawn"));
						
						String mapName = getArg(1);
						send("Creating map " + mapName);
						
						/* Get the selected Region from WorldEdit */
						BukkitPlayer bPlayer = BukkitAdapter.adapt(user.getPlayer());
						Region region = null;
						try {
							region = WorldEdit.getInstance().getSessionManager().get(bPlayer).getSelection(bPlayer.getWorld());
						} catch (IncompleteRegionException e) {
							customError("Invalid region. Please select 2 points to select this map");
						}
						if(region == null) {
							customError("Region = null, no region was set?");
						}
						
						/* Check the size of the map to see if it fits within Map Size Limitations */
						if(!options.containsKey("bypass")) {
							int sizeXZ, sizeY;
							send("Width: " + region.getWidth() + ", Length: " + region.getLength());
							sizeXZ = region.getLength() > region.getWidth() ? region.getWidth() : region.getLength();
							sizeY = region.getHeight();
							errorIf(sizeXZ > MapLoader.MAX_SIZE, "Map too large. Please reduce it's width/length!");
							errorIf(sizeY > MapLoader.MAX_SIZE, "Map too tall. Please reduce it's height!");
						}else {
							send("Bypassing size checks...");
						}
						
						offset = vec2loc(region.getMinimumPoint());
						final Location mapOffset = offset;
						/* Get all of our spawn points ahead of time, because we need to
						 * remove the blue wool when we go to save the BLD file. We will
						 * replace all of this blue wool afterwards, to make sure this
						 * map can cleanly be saved again without replacing the wool. */
						if(!options.containsKey("nodata")) {
							final List<MapPosition> spawns = new ArrayList<>();
							region.forEach(vec -> {
								Location loc = vec2loc(vec);
								Block block = loc.getBlock();
								if(block.getType().equals(spawnMaterial)) {
									MapPosition pos = MapPosition.to(loc, mapOffset);
									spawns.add(pos);
									block.setType(Material.AIR);
								}
							});
							spawnLocations = spawns;
						}
						
						/* Create, raster, and save our BLD file */
						final StructureFile struct = new StructureFile(MapLoader.getStructureFile(mapName));
						try {
							struct.raster(offset, region.getWidth(), region.getHeight(), region.getLength());
							struct.save();
						} catch (IOException e) {
							customError("An error occurred while rastering the map. Try again or check console for details!");
							e.printStackTrace();
						}
						send("Saved BLD file to " + MapLoader.getStructureFile(mapName).getPath());
						
						/* Create a JSON file for this map, and tell the MapLoader to reload maps so it reads
						 * our newly created JSON file as a map. If there is already a JSON file there, we need
						 * to clear it's data so we can write new data to it. */
						if(!options.containsKey("nodata")) {
							MapLoader.createMapRegistry(mapName);
							final GameMap map;
							if(MapLoader.getMap(mapName) != null) {
								map = MapLoader.getMap(mapName);
								map.clearAllData();
								MapLoader.loadMaps();
							}
							else {
								// this is a brand new map!
								// Reload maps
								MapLoader.loadMaps();
								map = MapLoader.getMap(mapName);
							}
							map.setOffsetPosition(offset);
							
							// If we include the beta tag, we don't want this map to be
							// playable.
							map.setBeta(options.containsKey("beta"));
							
							/* Loop through all the blocks in the selected region and find
							 * Pink Wool and Purple Wool. Whenever we find either of these two
							 * blocks, we need to set either a Fragment position or a Portal position. */
							region.forEach(vec -> {
								
								// pink wool = generators
								// purple wool = portals
								// light blue wool = spawn point
								Location loc = vec2loc(vec);
								Block block = loc.getBlock();
								
								MapPosition pos = MapPosition.to(loc, mapOffset);
								
								if(block.getType().equals(fragmentMaterial)) {
									map.addGenerator(pos);
								}
								else if(block.getType().equals(portalMaterial)) {
									map.addExitPortal(pos);
								}
							});
							// error checking
							if(!options.containsKey("bypass")) {
								errorIf(map.getGenerators().length > 6, "Too many fragments! All "+fragmentMaterial.name()+" is marked as a fragment location, please put 6 on each map.");
								errorIf(map.getGenerators().length < 6, "Not enough fragments! There should be a total of 6 "+fragmentMaterial.name()+" on each map.");
								errorIf(map.getPortals().length > 2, "Too many portals! All "+portalMaterial.name()+" is marked as a fragment location, please put 2 on each map.");
								errorIf(map.getPortals().length < 2, "Not enough portals! There should be a total of 2 "+portalMaterial.name()+" on each map.");
								errorIf(spawnLocations.size() < 1, "No spawn points have been set. Please put "+spawnMaterial.name()+" to set them.");
							}else {
								send("Bypassing objective count checks...");
							}
							// add spawn locations
							for(MapPosition spawnPosition : spawnLocations) {
								map.addSpawn(spawnPosition);
								Block block = spawnPosition.getBlock(offset);
								block.setType(spawnMaterial);
							}
							spawnLocations = null;
							/* Save everything we just did into the JSON file so this map
							 * can be officially added. */
							send("Saving settings...");
							try {
								map.saveSettings();
							} catch (IOException e1) {
								customError("Error occured while saving settings. Check console for details!");
								e1.printStackTrace();
							}
							
						}else {
							send("Skipping data finding...");
						}
						// One final reload to load the new data.
						if(!options.containsKey("noreload")) {
							MapLoader.loadMaps();
						}
						send("Done! Created map " + mapName);
						Deadlight.staffMsg(FormatUtil.color("&a" + user.getName() + " just created the map &e" + mapName + "&a."));
					}catch(Exception e) {
						e.printStackTrace();
						if(e.getMessage() == null) {
							user.send("command_error", e.toString());
						}else {
							user.send("command_error", e.getMessage());
						}
						if(spawnLocations != null && offset != null) {
							// regenerate our spawn locations if we error while they were destroyed.
							for(MapPosition spawn : spawnLocations) {
								Location location = spawn.getLocation(offset);
								if(!location.getBlock().getType().equals(spawnMaterial)) {
									location.getBlock().setType(spawnMaterial);
								}
							}
						}
						
					}
				}else {
					
				}
			} else if(checkArgEqual(0, "clear")) {
				if(hasArg(1)) {
					int lobby;
					try {
						lobby = Integer.parseInt(args.get(1));
					}
					catch(NumberFormatException e) {
						sender.sendMessage("Unknown Lobby Slot! Please put in a number!");
						return;
					}
					MapClearer clearer = new MapClearer(lobby);
					clearer.run();
					send("Map is being cleared...");
				} else {
					user.send("usage", "/map clear <map slot>");
				}
			} else if(checkArgEqual(0, "delete")) {
				if(hasArg(2)) {
					String mapName = getArg(1);
					File mapFile = MapLoader.getMapFile(mapName);
					if(mapFile.exists()) {
						mapFile.delete();
						send("Successfully deleted map " + mapName);
					} else {
						user.send("error", "Map does not exist!");
						return;
					}
					Deadlight.staffMsg(FormatUtil.color("&a" + user.getName() + " just deleted &e" + mapName + "&a."));
				} else {
					user.send("usage", "/map delete <map name>");
				}
			} else if(checkArgEqual(0, "reload")) {
				MapLoader.loadMaps();
				send("Reloaded map list");
			} else if(checkArgEqual(0, "load")) {
				if(hasArg(2)) {
					String mapName = getArg(1);
					send("Loading map...");
					try {
						StructureFile file = new StructureFile(MapLoader.getStructureFile(mapName));
						Deadlight.debug("Located Structure File at " + MapLoader.getStructureFile(mapName).getPath());
						file.load();
						Deadlight.debug("Loaded "+ file.getSize()+" from file " + mapName);
						file.build(user.getLocation().subtract(0d, 1d, 0d));
						send ("Successfully built map from maps/" + mapName + ".bld");
					} catch (IOException e) {
						send("Unable to load map. Check console for logs.");
						e.printStackTrace();
					}
					Deadlight.staffMsg(FormatUtil.color("&a" + user.getName() + " just loaded map &e" + mapName + "&a at their location."));
				} else {
					user.send("usage", "/map load <map name>");
				}
			} else if(checkArgEqual(0, "beta")) {
				if(hasArg(2)) {
					GameMap map = MapLoader.getMap(getArg(1));
					if(map == null) {
						send("Map does not exist.");
					}else {
						if(hasArg(3)) {
							map.setBeta(getArg(2).equals("true"));
						}else {
							map.setBeta(!map.isBeta());
						}
						try {
							map.saveSettings();
							send("Beta tag of "+map.getName()+" is now " + map.isBeta());
						} catch (IOException e) {
							e.printStackTrace();
							send("Something went wrong! Couldn't save beta tag!");
						}
					}
				}else {
					send("Usage: /map beta <map name> [is beta?]");
				}
			} else if(checkArgEqual(0, "force")) {
				if(hasArg(1)) {
					GameMap map = MapLoader.getMap(getArg(1));
					if(map == null) {
						send("Map does not exist.");
					}else {
						MapLoader.setForcedMap(map);
						send("Next Map will be: " + map.getName());
					}
					
				}else {
					send("Usage: /map force <map name>");
				}
				
			} else if(checkArgEqual(0, "help")) {
				if(hasArg(2)) {
					send(ChatColor.YELLOW + "Viewing help info for " + ChatColor.GREEN + "/map " + getArg(1));
					switch(getArg(1)) {
					case "help": send("Use /map help for a list of commands.\nUse /map help <subcommand> for help for a specific command."); break;
					case "list": send("Use /map list to list the names of all available maps. Includes beta maps."); break;
					case "create": {
						String s = "";
						s+=("To create a map, simply select its dimensions using WorldEdit's WAND or //pos1 and //pos2 (not recommended)");
						s+=("\nAfter selecting, use /map create <The map name> [flags...] to create the map");
						s+=("\nAvailable flags:");
						s+=("\n--nodata: Saves the map without any associated map data, like spawn points or objective locations.");
						s+=("\n--bypass: Bypasses all error checking, although still throws errors when Exceptions are thrown.");
						s+=("\n--noreload: Skips the final reload that adds the map in rotation. You can do this manually with /map reload");
						s+=("\n--beta: Sets this map to \"beta\". It will not appear in rotation.");
						//s+=("\n-world [world name]: If you are in another world, enter the name of that world to use it instead of the default world.");
						s+=("\n-portal <material>: Select a block to use to represent Portal locations. (Default: purple_wool)");
						s+=("\n-frag <material>: Select a block to use to represent Fragment locations. (Default: pink_wool)");
						s+=("\n-spawn <material>: Select a block to use to represent Spawn locations. (Default: light_blue_wool)");
						send(s);
					}
						break;
					case "reload": send("Reload's the map list from disk. Do not run this often, especially mid-game if you removed a map."); break;
					case "load": send("This will load a map with the provided name at your current location, loading in the X+ Y+ Z+ direction. Make sure there is nothing in the way. You can't undo this, so be warned."); break;
					case "delete": send("This will delete the DATA file for a map, but leave the structure file in tact. You can't undo this, so be warned."); break;
					case "beta": send("This adds/removes the BETA flag. The flag is intended to remove maps from rotation if they just need to be tested."); break;
					case "force": send("The next map loaded is guarenteed to be the provided map. This is cleared after a lobby is loaded, so it only works once. This is for testing purposes, don't abuse it to play your favorite map."); break;
					case "clear": send("When a map loads, depending on it's lobby, it'll load in a predetermined position. Providing a lobby index will clear all blocks in that map's loading position. Use this if a map didn't unload properly."); break;
					}
					return;
				}
				String s = "";
				s+=("/map help [subcommand]: Access the help menu");
				s+=("\n/map list: Get a list of maps");
				s+=("\n/map create <name> [flags...]: Create a map using WorldEdit's selection tool.");
				s+=("\n/map beta <name>: Add or remove the BETA flag from a map.");
				s+=("\n/map force <name>: Forces the next map loaded to be the provided map.");
				s+=("\n/map reload: Reload the map list");
				s+=("\n/map load <name>: Load a map into the world at your location (NO UNDOS!)");
				s+=("\n/map delete <name>: Delete a map's data file to remove it from rotation (NO UNDOS!)");
				s+=("\n/map clear <index>: From the loading positions, clears a map in a lobby slot.");
				send(s);
			} else {
				errorUsage();
			}
		}
	}
	
	public Location vec2loc(BlockVector3 vec) {
		return new Location(Deadlight.getWorld(), vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
	}
	
	public void errorIf(boolean cond, String message) {
		if(cond) {
			throw new RuntimeException(message);
		}
	}
	
	public void customError(String message) {
		throw new RuntimeException(message);
	}
	
}
