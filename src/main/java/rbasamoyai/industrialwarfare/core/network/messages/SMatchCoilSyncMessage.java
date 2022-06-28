package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.matchcoil.MatchCoilMenu;

public class SMatchCoilSyncMessage {

	private int cutLength;
	
	public SMatchCoilSyncMessage() {}
	
	public SMatchCoilSyncMessage(int cutLength) {
		this.cutLength = cutLength;
	}
	
	public static void encode(SMatchCoilSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.cutLength);
	}
	
	public static SMatchCoilSyncMessage decode(FriendlyByteBuf buf) {
		return new SMatchCoilSyncMessage(buf.readVarInt());
	}
	
	public static void handle(SMatchCoilSyncMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			AbstractContainerMenu ct = ctx.getSender().containerMenu;
			if (!(ct instanceof MatchCoilMenu)) return;
			MatchCoilMenu coilCt = (MatchCoilMenu) ct;
			coilCt.setCutLength(Mth.clamp(msg.cutLength, MatchCoilMenu.MINIMUM_CORD_LEFT, MatchCoilMenu.MAX_CORD_CUT));
			coilCt.updateOutput();
		});
		ctx.setPacketHandled(true);
	}
	
}
