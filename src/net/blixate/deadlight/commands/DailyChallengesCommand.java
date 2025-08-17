package net.blixate.deadlight.commands;

import net.blixate.deadlight.challenges.DailyChallengesGUI;

public class DailyChallengesCommand extends DLCmd {

	@Override
	public void execute() {
		if(!user.hasReadTutorial) {
			user.send("user_read_tutorial_vague");
			return;
		}
		//user.send("not_implemented");
		user.openInventory(new DailyChallengesGUI(user, null));
	}

}
