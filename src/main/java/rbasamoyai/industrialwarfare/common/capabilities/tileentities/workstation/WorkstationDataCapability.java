package rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class WorkstationDataCapability {

	private static final String TAG_WORKING_TICKS = "workingTicks";
	private static final String TAG_WORKER_UUID = "workerUUID";
	private static final String TAG_HAS_WORKER = "hasWorker";
	private static final String TAG_IS_WORKING = "isWorking";
	
	@CapabilityInject(IWorkstationDataHandler.class)
	public static Capability<IWorkstationDataHandler> WORKSTATION_DATA_CAPABILITY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IWorkstationDataHandler.class, new Storage(), WorkstationDataHandler::new);
	}
	
	public static class Storage implements IStorage<IWorkstationDataHandler> {

		@Override
		public INBT writeNBT(Capability<IWorkstationDataHandler> capability, IWorkstationDataHandler instance,
				Direction side) {
			CompoundNBT tag = new CompoundNBT();
			tag.putInt(TAG_WORKING_TICKS, instance.getWorkingTicks());
			tag.putBoolean(TAG_HAS_WORKER, instance.hasWorker());
			tag.putBoolean(TAG_IS_WORKING, instance.isWorking());
			tag.putUUID(TAG_WORKER_UUID, instance.getWorkerUUID());
			return tag;
		}

		@Override
		public void readNBT(Capability<IWorkstationDataHandler> capability, IWorkstationDataHandler instance,
				Direction side, INBT nbt) {
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.setWorkingTicks(tag.getInt(TAG_WORKING_TICKS));
			instance.setHasWorker(tag.getBoolean(TAG_HAS_WORKER));
			instance.setIsWorking(tag.getBoolean(TAG_IS_WORKING));
			// Read last as IWorkstationDataHandler#setWorker and IWorkstationDataHandler#setWorkerUUID modify the above values.
			instance.setWorkerUUID(tag.getUUID(TAG_WORKER_UUID));
		}
		
	}
	
}