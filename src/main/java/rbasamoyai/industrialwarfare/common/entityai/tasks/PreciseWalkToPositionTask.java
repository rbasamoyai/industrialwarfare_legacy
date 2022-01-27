package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class PreciseWalkToPositionTask extends Task<CreatureEntity> {

	private final float speedModifier;
	private final double precisionDistance;
	private final double closeEnough;
	
	public PreciseWalkToPositionTask(float speedModifier, double precisionDistance, double closeEnough) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleTypeInit.PRECISE_POS.get(), MemoryModuleStatus.VALUE_PRESENT));
		this.speedModifier = speedModifier;
		this.precisionDistance = precisionDistance;
		this.closeEnough = closeEnough;
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
		entity.getMoveControl().setWantedPosition(precisePos.x, precisePos.y, precisePos.z, this.speedModifier);
		if (precisePos.y - entity.position().y >= 0.5d) entity.getJumpControl().jump();
		entity.getBrain().eraseMemory(MemoryModuleTypeInit.PRECISE_POS.get());
	}
	
}
