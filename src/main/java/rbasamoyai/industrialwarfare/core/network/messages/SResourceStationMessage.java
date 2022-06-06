package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.ResourceStationContainer;

public class SResourceStationMessage {

	private int selectedTab;
	
	public SResourceStationMessage() {}
	
	public SResourceStationMessage(int selectedTab) {
		this.selectedTab = selectedTab;
	}
	
	public static void encode(SResourceStationMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.selectedTab);
	}
	
	public static SResourceStationMessage decode(PacketBuffer buf) {
		return new SResourceStationMessage(buf.readVarInt());
	}
	
	public static void handle(SResourceStationMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.getSender();
			Container ct = sender.containerMenu;
			if (!(ct instanceof ResourceStationContainer)) return;
			((ResourceStationContainer) ct).setSelected(msg.selectedTab);
		});
		ctx.setPacketHandled(true);
	}
	
}
