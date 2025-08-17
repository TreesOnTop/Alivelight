package net.blixate.deadlight.maps.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.persistence.PersistentDataType;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.kit.items.Items;
import net.blixate.deadlight.lobby.Lobby;
import net.blixate.deadlight.maps.environment.states.BlockedState;
import net.blixate.deadlight.maps.environment.states.ObjectiveState;
import net.blixate.deadlight.maps.environment.states.RegressingState;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.effects.InfectedEffect;
import net.blixate.deadlight.util.FormatUtil;
import net.blixate.deadlight.util.time.Cooldown;

public class SurvivorObjective extends MapObject {
	public enum ObjType {
		GENERATOR(Material.SOUL_SAND), PORTAL(Material.STONE);
		
		Material m;
		ObjType(Material mat) {
			m = mat;
		}
	}
	
	public TextDisplay progressText;
	
	protected Lobby lobby;
	private BlockData data;
	private ObjType type;
	
	public float progress;
	public Cooldown cool;
	public float objectiveSpeedMultiplier = 1f;
	
	// Attached entities
	public Shulker highlightShulker;
	
	ArrayList<ObjectiveState> states;
	
	public SurvivorObjective(Lobby lobby, MapPosition pos) {
		this(lobby, pos, ObjType.GENERATOR);
	}
	
	public SurvivorObjective(Lobby lobby, MapPosition pos, ObjType type) {
		super(pos, lobby.getMapLocation());
		this.lobby = lobby;
		this.progress = 0;
		this.type = type;
		this.states = new ArrayList<>();
		if(this.type == ObjType.PORTAL) {
			cool = new Cooldown();
		}
	}
	
	public float getProgress() {
		return progress;
	}
	
	public boolean hasState(Class<? extends ObjectiveState> stateClass) {
		if(states.isEmpty()) {
			return false;
		}
		for(ObjectiveState state : states) {
			if(state.getClass().equals(stateClass)) {
				return true;
			}
		}
		return false;
	}
	
	public void removeAllStatesOfClass(Class<? extends ObjectiveState> stateClass) {
		states.removeIf(new Predicate<>() {
			@Override
			public boolean test(ObjectiveState t) {
				return t.getClass().equals(stateClass);
			}
		});
		updateText();
	}
	
	public void addState(ObjectiveState state) {
		states.add(state);
		updateText();
	}
	
	@SuppressWarnings("deprecation")
	public void createHighlightShulker(Lobby lobby) {
		Location loc = getPosition().getLocation(lobby.getMapLocation());
		World world = loc.getWorld();
		highlightShulker = world.spawn(loc, Shulker.class, (entity) -> {
			entity.setAI(false);
			entity.setRemoveWhenFarAway(false);
			entity.setPersistent(true);
			entity.setSilent(true);
			entity.setInvisible(true);
			entity.setGlowing(true);
			entity.setPeek(0);
			entity.setCustomName(this.getClass().getSimpleName() + Deadlight.RNG.nextInt());
			entity.setCustomNameVisible(false);
			entity.getPersistentDataContainer().set(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER, lobby.randomEntityLobbyId);
		});
		lobby.fragmentTeam.addEntry(highlightShulker.getUniqueId().toString());
		highlightShulker.setPeek(0);
		// Create text display
		progressText = loc.getWorld().spawn(loc.add(0.5, 1.25, 0.5), TextDisplay.class, (entity) -> {
			entity.setDefaultBackground(false);
			entity.setSeeThrough(false);
			entity.setShadowed(true);
			entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
			entity.setAlignment(TextAlignment.CENTER);
			entity.setBillboard(Billboard.CENTER);
			entity.getPersistentDataContainer().set(Lobby.ENTITY_IDENTIFIER, PersistentDataType.INTEGER, lobby.randomEntityLobbyId);
		});
		
		updateText();
	}
	
	public void removeHighlightShulker() {
		//Deadlight.debug("Removing highlight shulker " + this.highlightShulker.getCustomName());
		if(this.highlightShulker != null) {
			highlightShulker.remove();
		}
		if(this.progressText != null) {
			progressText.remove();
			progressText = null;
		}
	}
	
	public Shulker getHighlighter() {
		return highlightShulker;
	}
	
	// separate tick method for calculating states
	public void tickStates(Lobby lobby) {
		// remove the first state
		if(states == null || states.isEmpty()) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<ObjectiveState> statesCopy = (List<ObjectiveState>) states.clone();
		for(ObjectiveState state : statesCopy) {
			if(state.hasExpired()) {
				this.states.remove(state);
			}
		}
	}
	
	public void tick(Lobby lobby) {}
	public void destroy(DLUser player) {}
	public void activateObjective(DLUser user) {}
	
	public boolean activate(DLUser user) {
		if(user.hasEffect(InfectedEffect.class)) {
			user.sendActionbar("actionbar_blocked_infected");
			return false;
		}
		double speed = objectiveSpeedMultiplier;
		if(type == ObjType.GENERATOR) {
			if(user.isHoldingItem(Items.GEN_TRAP)) {
				this.removeAllStatesOfClass(BlockedState.class);
				progress += 20;
				user.sendActionbar("actionbar_unblocked_objective");
				user.useItem();
			}
			else if(this.hasState(BlockedState.class)) {
				user.sendActionbar("actionbar_blocked");
				return false;
			}
			if(!user.genCooldown.isDone()) {
				return progress >= 100;
			}
			speed *= user.getUncursingSpeed();
		}else if(type == ObjType.PORTAL) {
			if(this.hasState(BlockedState.class)) {
				user.sendActionbar("actionbar_blocked");
				return false;
			}
			if(!cool.isDone()) {
				return progress >= 100;
			}
			speed *= user.getPoweringSpeed();
			if(progress >= 100 && this.type == ObjType.PORTAL) {
				progressText.remove();
				progressText = null;
			}
		} else {
			return progress >= 100;
		}
		
		activateObjective(user);
		
		progress += speed;
		user.sendProgress(speed, progress);
		updateText();
		
		return progress >= 100;
	}
	
	public void updateText() {
		if(progressText == null) {
			return;
		}
		String text = "&9" + (int)(progress) + "%";
		if(this.hasState(BlockedState.class)) {
			text = "&e⚠";
		}
		if(this.hasState(RegressingState.class)) {
			text = "&c" + ((int)progress) + "%";
		}
		if(progressText.getText().equals(FormatUtil.color(text))) {
			return;
		}
		
		progressText.setText(FormatUtil.color(text));
	}
	
	public void generate() {
		position.getBlock(offset).setType(type.m);
		if(data != null) {
			position.getBlock(offset).setBlockData(data);
		}
	}
	
	public BlockData getBlockData() {
		if(data == null) {
			return position.getBlock(offset).getBlockData();
		}
		return this.data;
	}
	
	public void setType(Material m) {
		position.getBlock(offset).setType(m);
	}
	
	public Material getType() {
		return position.getBlock(offset).getType();
	}
	
	public void setBlockData(BlockData data) {
		this.data = data;
	}
}
