package net.blixate.deadlight.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
/**
 * A wrapper for {@link CommandExecutor} that adds more essential functionality and removes
 * pointless features.
 * <p>
 * This is not <b>thread safe</b>! If any delay is added, {@link DLCmd#getCommand()} and {@link DLCmd#getLabel()} might fail.</p>
 */
public abstract class DLCmd implements CommandExecutor {
	/* Potential race condition?? */
	private String label;
	private Command cmd;
	protected DLUser user;
	protected CommandSender sender;
	protected ArrayList<String> args;
	private String permission;
	private String usage;
	private boolean allowConsole;
	
	public DLCmd() {}
	public DLCmd(String perm) {
		this.permission = perm;
	}
	
	public DLCmd(String permission, String usage) {
		this(permission, usage, false);
	}
	
	public DLCmd(String permission, String usage, boolean allowConsole) {
		this.permission = permission;
		this.usage = usage;
		this.allowConsole = allowConsole;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		this.sender = sender;
		if(!(sender instanceof Player) && !allowConsole) {
			sender.sendMessage("Only allowed for Players.");
			return true;
		}
		if(sender instanceof Player) {
			this.user = PlayerManager.getUser(((Player)sender).getUniqueId());
		}
		if(permission != null && !isConsole() && !user.checkPerm(permission)) {
			user.send("no_permission");
			return true;
		}
		
		this.label = label;
		this.cmd = cmd;
		
		if (args.length != 0) { this.args = Lists.newArrayList(args); }
		else { this.args = new ArrayList<>(); }
		try { this.execute(); }
		catch(CommandException e){}
		this.args.clear();
		return true;
	}
	
	public abstract void execute();
	
	protected String getLabel() {
		return label;
	}
	
	protected Command getCommand() {
		return cmd;
	}
	
	protected void error(String data) {
		user.send("command_error", data);
		throw new CommandException();
	}
	
	protected void errorBadArgument(String data) {
		user.send("bad_argument", data);
		throw new CommandException();
	}
	
	protected void errorUsage() {
		user.send("usage", usage);
		throw new CommandException();
	}
	
	protected void requireArgs(int amount) {
		if(args.size() < amount) {
			errorUsage();
			throw new CommandException();
		}
	}
	
	protected void send(String message) {
		this.sender.sendMessage(message);
	}
	
	protected String getArg(int i) {
		return args.get(i);
	}
	
	protected String getStringArgs(int i) {
		String[] msg = args.subList(i, args.size()).toArray(new String[0]);
		return String.join(" ", msg);
	}
	
	protected boolean checkArgEqual(int position, String v) {
		return (hasArg(position) && v.equals(args.get(position)));
	}
	protected boolean checkArgEqualIgnoreCase(int pos, String v) {
		return (hasArg(pos) && v.equalsIgnoreCase(args.get(pos)));
	}

	protected boolean hasArg(int arg) {
		return args.size() >= arg;
	}
	protected boolean hasNoArgs() {
		return args.size() == 0 || args.isEmpty();
	}
	
	protected boolean isConsole() {
		return user == null;
	}
	
	public Map<String, String> parseOptions(int startIndex) {
		String optionsString = getStringArgs(startIndex);
		Map<String,String> map = new HashMap<>();
		String[] options = optionsString.split(" ");
		for(int i = 0; i < options.length; i++) {
			if(options[i].startsWith("--")) {
				map.put(options[i].substring(2), null);
			}
			else if(options[i].startsWith("-")) {
				if(i+1 >= options.length) {
					map.put(options[i].substring(1), "");
				}
				map.put(options[i].substring(1), options[++i]);
			}
		}
		return map;
	}
}
