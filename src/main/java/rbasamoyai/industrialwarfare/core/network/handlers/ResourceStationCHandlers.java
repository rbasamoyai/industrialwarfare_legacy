package rbasamoyai.industrialwarfare.core.network.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationMenu;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.CSyncExtraStock;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.CSyncRequests;

public class ResourceStationCHandlers {

	public static void handleCSyncRequests(CSyncRequests msg) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		AbstractContainerMenu ct = mc.player.containerMenu;
		if (!(ct instanceof ResourceStationMenu)) return;
		((ResourceStationMenu) ct).setRequests(msg.getPredicates());
	}
	
	public static void handleCSyncExtraStock(CSyncExtraStock msg) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		AbstractContainerMenu ct = mc.player.containerMenu;
		if (!(ct instanceof ResourceStationMenu)) return;
		((ResourceStationMenu) ct).setExtraStock(msg.getExtraStock());
	}
	
}
