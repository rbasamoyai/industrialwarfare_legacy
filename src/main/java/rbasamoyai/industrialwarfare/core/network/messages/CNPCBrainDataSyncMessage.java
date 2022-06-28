package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.network.handlers.CNPCBrainDataSyncHandler;

public class CNPCBrainDataSyncMessage {

	private int id;
	private NPCComplaint complaint;
	
	public CNPCBrainDataSyncMessage() {
	}
	
	public CNPCBrainDataSyncMessage(int id, NPCComplaint complaint) {
		this.id = id;
		this.complaint = complaint;
	}
	
	public int id() { return this.id; }
	public NPCComplaint complaint() { return this.complaint; }
	
	public static void encode(CNPCBrainDataSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.id);
		buf.writeResourceLocation(msg.complaint.getRegistryName());
	}
	
	public static CNPCBrainDataSyncMessage decode(FriendlyByteBuf buf) {
		int id = buf.readVarInt();
		NPCComplaint complaint = IWModRegistries.NPC_COMPLAINTS.get().getValue(buf.readResourceLocation());
		return new CNPCBrainDataSyncMessage(id, complaint);
	}
	
	public static void handle(CNPCBrainDataSyncMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CNPCBrainDataSyncHandler.handle(msg));
		});
		ctx.setPacketHandled(true);
	}
	
}
