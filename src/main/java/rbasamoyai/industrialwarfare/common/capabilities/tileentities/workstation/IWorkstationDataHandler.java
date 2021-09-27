package rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;

public interface IWorkstationDataHandler {
	
	public void setWorker(@Nullable LivingEntity worker); // <- This method should affect the other variables and thus should be loaded last.
	
	public void setWorkerUUID(@Nullable UUID workerUUID); // <- Ditto above.
	
	/** 
	 * Only sets workerUUID. Only meant for testing for a worker entity, should be followed later on with a call to
	 * either {@code IWorkstationDataHandler#setWorker} or {@code IWorkstationDataHandler#setWorkerUUID}.
	 */
	public void setWorkerUUIDOnly(@Nullable UUID workerUUID);
	public UUID getWorkerUUID();
	
	public void setHasWorker(boolean hasWorker);
	public boolean hasWorker();
	
	public void setIsWorking(boolean isWorking);
	public boolean isWorking();
	
	public void setWorkingTicks(int workingTicks);
	public void incrementWorkingTicks(int multiplier);
	public int getWorkingTicks();
	
}
