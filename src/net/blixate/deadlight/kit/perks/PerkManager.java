package net.blixate.deadlight.kit.perks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.blixate.config.ConfigFile;
import net.blixate.config.ConfigSection;
import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.player.Alignment;
import net.blixate.deadlight.player.DLUser;

public class PerkManager {
	
	private ArrayList<PerkRegistry> killerPerks = new ArrayList<>();
	private ArrayList<PerkRegistry> survivorPerks = new ArrayList<>();
	
	/** Load the registry files from disk.
	 * @see PerkRegistry
	 * @see Perk
	 */
	public void createRegistry() {
		createSurvivorRegistry();
		createKillerRegistry();
	}
	
	private void createSurvivorRegistry() {
		survivorPerks = new ArrayList<>();
		ConfigFile file = new ConfigFile(new File(Deadlight.dataFolder, "survivor_perk_registry.bcfg"));
		try {
			file.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(ConfigSection section : file.getSections()) {
			if(section.getName().equals("Global")) {
				continue;
			}
			registerSurvivorPerk(section);
		}
		Deadlight.debug("Created survivor perk registry");
	}
	
	private void createKillerRegistry() {
		killerPerks = new ArrayList<>();
		ConfigFile file = new ConfigFile(new File(Deadlight.dataFolder, "killer_perk_registry.bcfg"));
		try {
			file.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(ConfigSection section : file.getSections()) {
			if(section.getName().equals("Global")) {
				continue;
			}
			registerKillerPerk(section);
		}
		Deadlight.debug("Created killer perk registry");
	}
	
	public void registerSurvivorPerk(ConfigSection perkSection) {
		PerkRegistry reg = createRegistry(perkSection, Alignment.SURVIVOR);
		survivorPerks.add(reg);
	}
	
	public void registerKillerPerk(ConfigSection perkSection) {
		PerkRegistry reg = createRegistry(perkSection, Alignment.KILLER);
		killerPerks.add(reg);
	}
	
	private PerkRegistry createRegistry(ConfigSection section, Alignment align) {
		PerkRegistry regPerk = (PerkRegistry)new PerkRegistry(section.getName(), align, section);
		return regPerk;
	}
	
	/* This could decrease performance. */
	public PerkRegistry getRegistry(String id) {
		PerkRegistry killer = getKillerPerk(id);
		if(killer != null) {
			return killer;
		}
		return getSurvivorPerk(id);
	}
	
	/**
	 * Call a {@link PerkEventType} for this all players in a lobby.
	 */
	public void callEvent(Lobby lobby, PerkEventType e, Object...params) {
		for(DLUser user : lobby.getPlayers()) {
			callEvent(user, e, params);
		}
	}
	
	/**
	 * Call a {@link PerkEventType} for this player.
	 */
	public void callEvent(DLUser user, PerkEventType e, Object...params) {
		if(user == null) {
			return;
		}
		if(user.isSpectating()) { return; }
		if(user.isKiller() && user.killerTypeInstance != null) {
			user.killerTypeInstance.perkEvent(new PerkEvent(e, params));
		}
		if(user.perks == null) {
			user.perks = new ArrayList<>();
		}
		for(Perk p : user.perks) {
			p.callEvent(e, params);
		}
	}
	
	/**
	 * Return the {@link PerkRegistry} of a Survivor perk.
	 * @param id Name of perk class.
	 * @return PerkRegistry for the provided id. <b>null</b> if it doesn't exist. */
	public PerkRegistry getSurvivorPerk(String id) {
		for(PerkRegistry p : survivorPerks) {
			if(p.id.equals(id)) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Return the {@link PerkRegistry} of a Killer perk.
	 * @param id Name of perk class.
	 * @return PerkRegistry for the provided id. <b>null</b> if it doesn't exist. */
	public PerkRegistry getKillerPerk(String id) {
		for(PerkRegistry p : killerPerks) {
			if(p.id.equals(id)) {
				return p;
			}
		}
		return null;
	}

	public List<PerkRegistry> getKillerPerks() {
		return killerPerks;
	}
	
	public List<PerkRegistry> getSurvivorPerks() {
		return survivorPerks;
	}
}
