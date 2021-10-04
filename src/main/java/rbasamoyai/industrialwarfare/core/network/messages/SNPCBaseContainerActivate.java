package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.client.screen.npc.NPCBaseScreen;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCContainer;

public class SNPCBaseContainerActivate {

	public int page;
	
	public SNPCBaseContainerActivate() {
	}
	
	public SNPCBaseContainerActivate(int page) {
		this.page = page;
	}
	
	public static void encode(SNPCBaseContainerActivate msg, PacketBuffer buf) {
		buf.writeVarInt(msg.page);
	}
	
	public static SNPCBaseContainerActivate decode(PacketBuffer buf) {
		return new SNPCBaseContainerActivate(buf.readVarInt());
	}
	
	public static void handle(SNPCBaseContainerActivate msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ServerPlayerEntity player = context.getSender();
			Container ct = player.containerMenu;
			if (ct instanceof NPCContainer) {
				NPCContainer npcCt = (NPCContainer) ct;
				npcCt.setNPCEquipmentSlotsActive(msg.page == NPCBaseScreen.MAIN_PAGE);
				npcCt.setNPCInventorySlotsActive(msg.page == NPCBaseScreen.INVENTORY_PAGE);
			}
		});
		context.setPacketHandled(true);
	}
	
}
