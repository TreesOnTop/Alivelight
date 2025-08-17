package net.blixate.deadlight.util.nms;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;

import net.blixate.deadlight.Deadlight;

public class ServerDebugPacketListener extends PacketAdapter {

	public ServerDebugPacketListener(Plugin plugin, ListenerPriority listenerPriority, PacketType[] types) {
		super(Deadlight.inst, ListenerPriority.MONITOR);
	}

}
