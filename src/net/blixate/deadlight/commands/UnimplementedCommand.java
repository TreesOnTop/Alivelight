package net.blixate.deadlight.commands;

public class UnimplementedCommand extends DLCmd{

	@Override
	public void execute() {
		user.send("not_implemented");
	}

}
