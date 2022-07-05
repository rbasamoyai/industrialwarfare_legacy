package rbasamoyai.industrialwarfare.client.screen.resourcestation;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationMenu;

public class ResourceStationScreen extends AbstractResourceStationScreen<ResourceStationMenu> {

	public ResourceStationScreen(ResourceStationMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title);
	}
	
}
