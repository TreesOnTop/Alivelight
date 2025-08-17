 package net.blixate.deadlight.commands;

import java.math.BigInteger;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.FormatUtil;

public class CurrencyCommand extends DLCmd {
	
	public void handleCmds(DLUser target) {
		if(hasArg(3)) {
			String action = args.get(1);
			BigInteger amount = new BigInteger(args.get(2));
			switch(action) {
			case "add":
			case "give":
				target.blood = target.blood.add(amount);
				user.getPlayer().sendMessage("Added " + amount + " blood to " + target.getName());
				break;
			case "take":
			case "remove":
			case "sub":
			case "subtract":
				target.blood = target.blood.subtract(amount);
				user.getPlayer().sendMessage("Removed " + amount + " blood from " + target.getName());
				break;
			case "set":
				target.blood = amount;
				user.getPlayer().sendMessage("Set " + target.getName() + "'s Blood to " + amount);
				break;
			}
		}else if(hasArg(1)) {
			user.send("user_view_other_blood", target.getName(), FormatUtil.formatCurrency(target.blood));
		}
	}
	
	public void handleCmdsSouls(DLUser target) {
		if(hasArg(3)) {
			String action = args.get(1);
			BigInteger amount = new BigInteger(args.get(2));
			switch(action) {
			case "add":
			case "give":
				target.souls = target.souls.add(amount);
				user.getPlayer().sendMessage("Added " + amount + " souls to " + target.getName());
				break;
			case "take":
			case "remove":
			case "sub":
			case "subtract":
				target.souls = target.souls.subtract(amount);
				user.getPlayer().sendMessage("Removed " + amount + " souls from " + target.getName());
				break;
			case "set":
				target.souls = amount;
				user.getPlayer().sendMessage("Set " + target.getName() + "'s souls to " + amount);
				break;
			}
		}else if(hasArg(1)) {
			user.send("user_view_other_souls", target.getName(), FormatUtil.formatCurrency(target.souls));
		}
	}

	@Override
	public void execute() {
		if(getLabel().endsWith("blood")) {
			if(hasNoArgs() || !user.getPlayer().hasPermission("deadlight.blood.modify")) {
				user.send("user_view_blood", FormatUtil.formatCurrency(user.blood));
			}else{
				DLUser target = PlayerManager.getUser(Deadlight.getUUID(args.get(0)));
				if(target == null) {
					user.send("player_not_online", args.get(0));
				}
				handleCmds(target);
			}
		}
		else if(getLabel().endsWith("souls")) {
			if(hasNoArgs() || !user.getPlayer().hasPermission("deadlight.souls.modify")) {
				user.send("user_view_souls", FormatUtil.formatLong(user.getSouls()));
			}else{
				DLUser target = PlayerManager.getUser(Deadlight.getUUID(args.get(0)));
				if(target == null) {
					user.send("player_not_online", args.get(0));
				}
				handleCmdsSouls(target);
			}
		}
		else if(getLabel().endsWith("level") || getLabel().endsWith("xp") || getLabel().endsWith("exp")) {
			if(hasNoArgs()) {
				user.send("user_view_level",
						""+user.getLevel(),
						""+user.getExp(),
						""+user.getPrestige(),
						FormatUtil.formatLong(user.getExp()),
						FormatUtil.formatLong(user.getLevelUpExp())
				);
			}
		}
	}

}
