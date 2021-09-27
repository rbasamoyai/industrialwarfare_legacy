package rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;

public class WorkstationDataHandler implements IWorkstationDataHandler {
	
	public static final UUID EMPTY_UUID = new UUID(0L, 0L); 
	
	private UUID workerUUID = EMPTY_UUID;
	private boolean hasWorker = false;
	private boolean isWorking = false;
	private int workingTicks = 0;
	
	public WorkstationDataHandler() {
	}
	
	@Override
	public void setWorker(@Nullable LivingEntity worker) {
		UUID previousUUID = this.workerUUID;
		this.workerUUID = worker == null ? EMPTY_UUID : worker.getUUID();
		this.hasWorker = worker != null;
		this.isWorking = false;
		if (!previousUUID.equals(this.workerUUID) && worker != null) this.workingTicks = 0;
	}
	
	@Override
	public void setWorkerUUID(@Nullable UUID workerUUID) {
		UUID previousUUID = this.workerUUID;
		this.workerUUID = workerUUID == null ? EMPTY_UUID : workerUUID;
		this.hasWorker = workerUUID != null && !workerUUID.equals(EMPTY_UUID);
		this.isWorking = false;
		if (!previousUUID.equals(this.workerUUID) && workerUUID != null && !workerUUID.equals(EMPTY_UUID)) this.workingTicks = 0;
	}
	
	@Override
	public void setWorkerUUIDOnly(@Nullable UUID workerUUID) {
		this.workerUUID = workerUUID == null ? EMPTY_UUID : workerUUID;
	}

	@Override
	public UUID getWorkerUUID() {
		return this.workerUUID;
	}

	@Override
	public void setHasWorker(boolean hasWorker) {
		this.hasWorker = hasWorker;
	}

	@Override
	public boolean hasWorker() {
		return this.hasWorker;
	}

	@Override
	public void setIsWorking(boolean isWorking) {
		this.isWorking = isWorking;
	}

	@Override
	public boolean isWorking() {
		return this.isWorking;
	}

	@Override
	public void setWorkingTicks(int workingTicks) {
		this.workingTicks = workingTicks;
	}

	@Override
	public void incrementWorkingTicks(int multiplier) {
		this.workingTicks += multiplier;
	}
	
	@Override
	public int getWorkingTicks() {
		return this.workingTicks;
	}

}
