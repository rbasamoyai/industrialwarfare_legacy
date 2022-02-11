package rbasamoyai.industrialwarfare.core.network.handlers;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.core.network.messages.FirearmActionMessages;

public class FirearmActionCHandlers {

	public static void handleCApplyRecoil(FirearmActionMessages.CApplyRecoil msg, Supplier<NetworkEvent.Context> contextSupplier) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		mc.player.xRotO = mc.player.xRot;
		mc.player.yRotO = mc.player.yRot;
		mc.player.xRot = msg.xRot;
		mc.player.yRot = msg.yRot;
	}
	
	public static void handleCNotifyHeadshot(FirearmActionMessages.CNotifyHeadshot msg, Supplier<NetworkEvent.Context> contextSupplier) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		mc.player.playSound(SoundEvents.ARROW_HIT_PLAYER, 1.0f, 1.0f);
	}
	
}
