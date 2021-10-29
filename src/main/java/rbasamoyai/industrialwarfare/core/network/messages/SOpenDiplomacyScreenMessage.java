package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.fml.network.NetworkEvent;

public class SOpenDiplomacyScreenMessage {

	public SOpenDiplomacyScreenMessage() {
	}
	
	public static void encode(SOpenDiplomacyScreenMessage msg, PacketBuffer buf) {
	}
	
	public static SOpenDiplomacyScreenMessage decode(PacketBuffer buf) {
		return new SOpenDiplomacyScreenMessage();
	}
	
	public static void handle(SOpenDiplomacyScreenMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ServerPlayerEntity player = context.getSender();
			player.displayClientMessage(new StringTextComponent("testing").withStyle(Style.EMPTY.withColor(Color.fromRgb(0x9bbc0f))), true);
		});
		context.setPacketHandled(true);
	}
	
}
