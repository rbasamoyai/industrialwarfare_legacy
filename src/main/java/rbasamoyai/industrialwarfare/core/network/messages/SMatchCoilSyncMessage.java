package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.MatchCoilContainer;

public class SMatchCoilSyncMessage {

	private int cutLength;
	
	public SMatchCoilSyncMessage() {}
	
	public SMatchCoilSyncMessage(int cutLength) {
		this.cutLength = cutLength;
	}
	
	public static void encode(SMatchCoilSyncMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.cutLength);
	}
	
	public static SMatchCoilSyncMessage decode(PacketBuffer buf) {
		return new SMatchCoilSyncMessage(buf.readVarInt());
	}
	
	public static void handle(SMatchCoilSyncMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.getSender();
			Container ct = sender.containerMenu;
			if (!(ct instanceof MatchCoilContainer)) return;
			MatchCoilContainer coilCt = (MatchCoilContainer) ct;
			coilCt.setCutLength(MathHelper.clamp(msg.cutLength, MatchCoilContainer.MINIMUM_CORD_LEFT, MatchCoilContainer.MAX_CORD_CUT));
			coilCt.updateOutput();
		});
		ctx.setPacketHandled(true);
	}
	
}
