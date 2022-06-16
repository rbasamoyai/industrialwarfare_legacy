package rbasamoyai.industrialwarfare.core.network.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Container;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationContainer;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.CSyncRequests;

public class ResourceStationCHandlers {

	public static void handleCSyncRequests(CSyncRequests msg) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		Container ct = mc.player.containerMenu;
		if (!(ct instanceof ResourceStationContainer)) return;
		((ResourceStationContainer) ct).setRequests(msg.getPredicates());
	}
	
}
