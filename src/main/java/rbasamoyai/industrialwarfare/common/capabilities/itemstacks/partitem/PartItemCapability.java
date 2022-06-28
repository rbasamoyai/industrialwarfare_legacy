package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class PartItemCapability {

	public static final Capability<IPartItemData> INSTANCE = CapabilityManager.get(new CapabilityToken<>(){});

	public static void register(RegisterCapabilitiesEvent event) {
		event.register(IPartItemData.class);
	}
	
}
