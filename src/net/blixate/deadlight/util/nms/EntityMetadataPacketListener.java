package net.blixate.deadlight.util.nms;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataValue;

import net.blixate.deadlight.Deadlight;
import net.blixate.deadlight.player.DLUser;
import net.blixate.deadlight.player.PlayerManager;

public class EntityMetadataPacketListener extends PacketAdapter {
	
	public EntityMetadataPacketListener(Plugin plugin) {
		super(plugin, ListenerPriority.NORMAL, new PacketType[] {PacketType.Play.Server.ENTITY_METADATA});
	}
	
	@Override
	public void onPacketSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		try {
			DLUser viewer = PlayerManager.getUser(event.getPlayer());
			Entity glowingEntity = packet.getEntityModifier(event).read(0);
			StructureModifier<List<WrappedDataValue>> dataValueModifier = packet.getDataValueCollectionModifier();
			for(List<WrappedDataValue> entityState : dataValueModifier.getValues()) {
				for(WrappedDataValue state : entityState) {
					if(state.getIndex() == 0) {
						// True if this packet says the player should be glowing
						boolean glowing = ((byte)state.getRawValue() & 0x40) != 0;
						// Returns true if this entity should glow to the viewer.
						boolean shouldGlow = viewer.glowingUpdate(glowing, glowingEntity);
						
						if(glowing == shouldGlow) return;
						
						// change packet data here
						byte b = (byte)state.getRawValue();
						b = Byte.valueOf(shouldGlow ? (byte) (b | 0x40) : (byte) (b & ~0x40));
						state.setRawValue((Byte)b);
						entityState.set(0, state);
						packet.getDataValueCollectionModifier().write(0, entityState);
						Deadlight.debug(viewer.getName() + ": " + shouldGlow + ", " + glowing + ": " + (b & 0x40));
					} else {
						Deadlight.debug("Unknown Metadata Packet (" + viewer.getName() + "): " + glowingEntity.getName() + ", " + entityState);
					}
				}
			}
		} catch (Exception ignored) {
		}
	}
}
