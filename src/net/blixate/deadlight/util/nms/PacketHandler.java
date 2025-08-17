package net.blixate.deadlight.util.nms;

import io.netty.channel.ChannelDuplexHandler;

@Deprecated
public class PacketHandler extends ChannelDuplexHandler {
	/*boolean readError = false;
	boolean writeError = false;
	DLUser user;
	
	public PacketHandler(DLUser user) {
		this.user = user;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext chc, Object packet) throws Exception {
		try {
			boolean result = user.onPacketRead(chc, packet);
			if(!result) {
				return;
			}
		}
		catch (Exception e) {
			if(!readError) {
				e.printStackTrace();
				readError = true;
			}
		}
		super.channelRead(chc, packet);
	}
	
	@Override
	public void write(ChannelHandlerContext chc, Object packet, ChannelPromise promise) throws Exception {
		try {
			boolean result = user.onPacketWrite(chc, packet, promise);
			if(!result) {
				return;
			}
		}
		catch (Exception e) {
			if(!writeError) {
				e.printStackTrace();
				writeError = true;
			}
		}
		super.write(chc, packet, promise);
	}*/
}
