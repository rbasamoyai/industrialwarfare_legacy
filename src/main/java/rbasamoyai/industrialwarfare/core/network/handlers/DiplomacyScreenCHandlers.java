package rbasamoyai.industrialwarfare.core.network.handlers;

import net.minecraft.client.Minecraft;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;

public class DiplomacyScreenCHandlers {

	public static void handleCSendData(DiplomacyScreenMessages.CSendData msg) {
		Minecraft mc = Minecraft.getInstance();
		
		// TODO: Handle case in which mc#screen instance of DiplomacyScreen
		if (mc.screen != null) {
			// TODO: open DiplomacyScreen
		}
	}
	
}
