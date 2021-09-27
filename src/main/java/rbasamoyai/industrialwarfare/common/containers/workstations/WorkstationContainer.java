package rbasamoyai.industrialwarfare.common.containers.workstations;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;

public class WorkstationContainer extends Container {

	protected final IWorldPosCallable canUse;
	protected final Optional<? extends WorkstationTileEntity> workstationOptional;
	protected final Block block;
	
	protected final IIntArray data;
	
	protected UUID workerUUID;
	protected UUID viewerUUID;
	
	protected WorkstationContainer(ContainerType<?> type, int windowId, PlayerEntity player, BlockPos activationPos, IIntArray data, Optional<? extends WorkstationTileEntity> workstationOptional) {
		super(type, windowId);
		this.canUse = IWorldPosCallable.create(player.level, activationPos);
		this.block = player.level.getBlockState(activationPos).getBlock();
		this.workstationOptional = workstationOptional;
		this.data = data;
		
		this.addDataSlots(data);
	}

	public boolean stillValid(PlayerEntity player) {
		return stillValid(this.canUse, player, this.block);
	}
	
	public Optional<? extends WorkstationTileEntity> getWorkstationTE() {
		return this.workstationOptional;
	}
	
	public boolean hasWorker() {
		return this.data.get(0) > 0;
	}
	
	public float workingTicksScaled() {
		return (float) this.data.get(1) / (float) this.data.get(2);
	}
	
	public BlockPos blockPos() {
		return new BlockPos(this.data.get(3), this.data.get(4), this.data.get(5));
	}
	
	public boolean isViewerDifferentFromWorker() {
		return this.data.get(6) > 0;
	}
	
	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
	}

}