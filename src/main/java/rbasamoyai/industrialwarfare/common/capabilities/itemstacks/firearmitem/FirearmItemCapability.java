	package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.firearmitem;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class FirearmItemCapability {
	
	public static final Capability<IFirearmItemData> INSTANCE = CapabilityManager.get(new CapabilityToken<>(){});
	
	public static void register(RegisterCapabilitiesEvent event) {
		event.register(IFirearmItemData.class);
	}
	
	
}
