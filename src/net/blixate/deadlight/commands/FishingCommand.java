package net.blixate.deadlight.commands;

import net.blixate.deadlight.fishing.FishingGUI;

public class FishingCommand extends DLCmd {
	
	public void execute() {
		if(!user.hasReadTutorial) {
			user.send("user_read_tutorial_vague");
			return;
		}
		user.openInventory(new FishingGUI(user));
	}
}
