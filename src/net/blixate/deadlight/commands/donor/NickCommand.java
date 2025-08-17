package net.blixate.deadlight.commands.donor;

import net.blixate.deadlight.commands.DLCmd;

public class NickCommand extends DLCmd {
	
	public NickCommand() {
		super("deadlight.nick", "/nickname [new nickname]");
	}
	
	public void execute() {
		if(hasNoArgs()) {
			// revert nickname
			user.nickname = null;
			user.send("nickname_reverted");
			return;
		}
		String argString = getStringArgs(0);
		if(argString.length() > 20) {
			user.send("nickname_too_long");
			return;
		}
		if(!argString.matches("[A-Za-z0-9_&]+")) {
			user.send("nickname_non_alphanumeric");
			return;
		}
		user.nickname = argString;
		if(user.checkPerm("deadlight.nick.format") || user.checkPerm("deadlight.nick.colored")) {
			user.send("changed_nickname_colored", user.nickname);
		}
		else {
			user.send("changed_nickname", user.nickname);
		}
	}
	
}
