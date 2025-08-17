package net.blixate.deadlight.commands;

import java.math.BigInteger;
import java.util.UUID;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;
import net.blixate.deadlight.util.FormatUtil;

public class BloodTransferCommand extends DLCmd {

	public BloodTransferCommand() {
		super(null, "/bloodtransfer <player> <amount>");
	}

	@Override
	public void execute() {
		if(hasArg(2)) {
			UUID uuid = Deadlight.getUUID(args.get(0));
			if(user.getPlayer().getUniqueId().equals(uuid)) {
				user.send("cant_target_self");
				return;
			}
			DLUser target = PlayerManager.getUser(uuid);
			if(target == null) {
				user.send("player_not_online", args.get(0));
			}
			try {
				BigInteger amount = new BigInteger(args.get(1));
				if(user.getBlood() < amount.longValue() || amount.compareTo(BigInteger.ZERO) <= 0) {
					errorBadArgument("Invalid amount.");
					return;
				}
				amount = amount.abs();
				user.blood = user.blood.subtract(amount);
				target.blood = target.blood.add(amount);
				String formatted = FormatUtil.formatCurrency(amount);
				user.send("user_blood_transfer", formatted, target.getName());
				target.send("user_blood_transfer_receive", formatted, user.getName());
			}catch(NumberFormatException e) {
				errorBadArgument("Invalid Number.");
				return;
			}
		}else{
			errorUsage();
		}
	}
	
}
