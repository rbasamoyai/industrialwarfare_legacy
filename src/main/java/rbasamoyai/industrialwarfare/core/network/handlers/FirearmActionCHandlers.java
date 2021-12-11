package rbasamoyai.industrialwarfare.core.network.handlers;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.core.network.messages.FirearmActionMessages;

public class FirearmActionCHandlers {

	public static void handleCApplyRecoil(FirearmActionMessages.CApplyRecoil msg, Supplier<NetworkEvent.Context> contextSupplier) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		mc.player.xRot = msg.xRot;
		mc.player.yRot = msg.yRot;
	}
	
}
