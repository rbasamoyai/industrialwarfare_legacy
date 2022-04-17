package rbasamoyai.industrialwarfare.utils;

import java.util.UUID;

import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.IWorkstationDataHandler;

public class IWUUIDUtils {

	public static boolean equalsFromWorkstationOptional(LazyOptional<IWorkstationDataHandler> optional, UUID uuid) {
		return optional.map(IWorkstationDataHandler::getWorkerUUID).map(uuid::equals).orElse(false);
	}
	
}
