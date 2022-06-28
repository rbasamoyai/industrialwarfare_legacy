package rbasamoyai.industrialwarfare.core.network.handlers;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.core.network.messages.FirearmActionMessages;

public class FirearmActionCHandlers {
	
	public static void handleCNotifyHeadshot(FirearmActionMessages.CNotifyHeadshot msg, Supplier<NetworkEvent.Context> contextSupplier) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		mc.player.playSound(SoundEvents.ARROW_HIT_PLAYER, 1.0f, 1.0f);
	}
	
}
