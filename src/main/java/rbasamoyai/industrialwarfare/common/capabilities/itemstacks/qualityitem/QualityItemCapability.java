package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class QualityItemCapability {

	public static final Capability<IQualityItemData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

	public static void register(RegisterCapabilitiesEvent event) {
		event.register(IQualityItemData.class);
	}

}
