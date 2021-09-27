package rbasamoyai.industrialwarfare.utils;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.IWorkstationDataHandler;

public class IWUUIDUtils {

	public static boolean equalsFromWorkstationOptional(LazyOptional<IWorkstationDataHandler> optional, UUID uuid) {
		Supplier<UUID> notUUID = () -> new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() - 1L);
		return uuid.equals(optional.map(IWorkstationDataHandler::getWorkerUUID).orElseGet(notUUID));
	}
	
}
