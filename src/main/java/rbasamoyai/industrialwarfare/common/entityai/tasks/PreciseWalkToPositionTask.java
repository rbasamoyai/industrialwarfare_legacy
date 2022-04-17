package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entityai.navigation.PosWrapper;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class PreciseWalkToPositionTask extends Task<CreatureEntity> {

	private final float speedModifier;
	private final double precisionDistance;
	private final double closeEnough;
	private final boolean stabilizeLook;
	
	public PreciseWalkToPositionTask(float speedModifier, double precisionDistance, double closeEnough, boolean stabilizeLook) {
		super(ImmutableMap.of(
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.PRECISE_POS.get(), MemoryModuleStatus.VALUE_PRESENT));
		this.speedModifier = speedModifier;
		this.precisionDistance = precisionDistance;
		this.closeEnough = closeEnough;
		this.stabilizeLook = stabilizeLook;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, CreatureEntity entity) {
		Vector3d precisePos = entity.getBrain().getMemory(MemoryModuleTypeInit.PRECISE_POS.get()).get();
		BlockPos blockPos = new BlockPos(precisePos);
		if (!level.loadedAndEntityCanStandOn(blockPos.below(), entity)) return false;		
		Vector3d entityPos = entity.position();
		return precisePos.closerThan(entityPos, this.precisionDistance) && !precisePos.closerThan(entityPos, this.closeEnough);
	}
	
	@Override
	protected void start(ServerWorld level, CreatureEntity entity, long gameTime) {
		Vector3d precisePos = entity.getBrain().getMemory(MemoryModuleTypeInit.PRECISE_POS.get()).get();
		
		if (this.stabilizeLook) {
			Vector3d eyePos = entity.getEyePosition(1.0f).add(entity.getViewVector(1.0f).scale(2.0f));
			entity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new PosWrapper(eyePos));
		}
		
		entity.getMoveControl().setWantedPosition(precisePos.x, precisePos.y, precisePos.z, this.speedModifier);
		if (precisePos.y - entity.position().y >= 0.5d) entity.getJumpControl().jump();
		entity.getBrain().eraseMemory(MemoryModuleTypeInit.PRECISE_POS.get());
	}
	
}
