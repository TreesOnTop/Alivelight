package net.blixate.deadlight.commands;

import net.blixate.deadlight.gui.misc.LobbyListGui;

public class LobbiesCommand extends DLCmd {
	
	@Override
	public void execute() {
		user.openInventory(new LobbyListGui(user));
	}

}
