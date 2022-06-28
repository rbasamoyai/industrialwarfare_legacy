package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCMenu;

public class SNPCContainerActivateMessage {

	private int page;
	
	public SNPCContainerActivateMessage() {
	}
	
	public SNPCContainerActivateMessage(int page) {
		this.page = page;
	}
	
	public static void encode(SNPCContainerActivateMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.page);
	}
	
	public static SNPCContainerActivateMessage decode(FriendlyByteBuf buf) {
		return new SNPCContainerActivateMessage(buf.readVarInt());
	}
	
	public static void handle(SNPCContainerActivateMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			AbstractContainerMenu ct = ctx.getSender().containerMenu;
			if (!(ct instanceof NPCMenu)) return;
			((NPCMenu) ct).updateActiveSlots(msg.page);
		});
		ctx.setPacketHandled(true);
	}
	
}
