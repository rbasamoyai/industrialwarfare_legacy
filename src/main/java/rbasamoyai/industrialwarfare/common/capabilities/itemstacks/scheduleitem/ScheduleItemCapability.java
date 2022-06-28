package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class ScheduleItemCapability {

	public static final Capability<IScheduleItemData> INSTANCE = CapabilityManager.get(new CapabilityToken<>(){});

	public static void register(RegisterCapabilitiesEvent event) {
		event.register(IScheduleItemData.class);
	}

}
