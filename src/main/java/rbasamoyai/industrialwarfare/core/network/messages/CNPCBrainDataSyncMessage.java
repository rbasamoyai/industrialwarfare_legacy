package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.network.handlers.CNPCBrainDataSyncHandler;

public class CNPCBrainDataSyncMessage {

	public int id;
	public NPCComplaint complaint;
	public BlockPos pos;
	
	public CNPCBrainDataSyncMessage() {
	}
	
	public CNPCBrainDataSyncMessage(int id, NPCComplaint complaint, BlockPos pos) {
		this.id = id;
		this.complaint = complaint;
		this.pos = pos;
	}
	
	public static void encode(CNPCBrainDataSyncMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.id);
		buf
				.writeResourceLocation(msg.complaint.getRegistryName())
				.writeBlockPos(msg.pos);
	}
	
	public static CNPCBrainDataSyncMessage decode(PacketBuffer buf) {
		int id = buf.readVarInt();
		NPCComplaint complaint = IWModRegistries.NPC_COMPLAINTS.getValue(buf.readResourceLocation());
		BlockPos pos = buf.readBlockPos();
		return new CNPCBrainDataSyncMessage(id, complaint, pos);
	}
	
	public static void handle(CNPCBrainDataSyncMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CNPCBrainDataSyncHandler.handle(msg));
		});
		context.setPacketHandled(true);
	}
	
}
