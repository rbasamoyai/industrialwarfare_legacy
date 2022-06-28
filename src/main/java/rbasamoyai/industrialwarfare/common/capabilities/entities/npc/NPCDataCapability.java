package rbasamoyai.industrialwarfare.common.capabilities.entities.npc;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class NPCDataCapability {
	
	public static final Capability<INPCData> INSTANCE = CapabilityManager.get(new CapabilityToken<>(){});
	
	public static void register(RegisterCapabilitiesEvent event) {
		event.register(INPCData.class);
	}
	
}
