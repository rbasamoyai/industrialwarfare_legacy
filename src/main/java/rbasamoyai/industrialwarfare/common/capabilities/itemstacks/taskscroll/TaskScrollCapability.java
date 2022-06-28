package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class TaskScrollCapability {

	public static final Capability<ITaskScrollData> INSTANCE = CapabilityManager.get(new CapabilityToken<>(){});

	public static void register(RegisterCapabilitiesEvent event) {
		event.register(ITaskScrollData.class);		
	}

}
