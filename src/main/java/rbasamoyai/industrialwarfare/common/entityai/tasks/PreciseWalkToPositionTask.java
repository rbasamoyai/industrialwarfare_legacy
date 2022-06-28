package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.common.entityai.navigation.PosWrapper;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class PreciseWalkToPositionTask extends Behavior<PathfinderMob> {

	private final float speedModifier;
	private final double precisionDistance;
	private final double closeEnough;
	private final boolean stabilizeLook;
	
	public PreciseWalkToPositionTask(float speedModifier, double precisionDistance, double closeEnough, boolean stabilizeLook) {
		super(ImmutableMap.of(
				MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleTypeInit.PRECISE_POS.get(), MemoryStatus.VALUE_PRESENT));
		this.speedModifier = speedModifier;
		this.precisionDistance = precisionDistance;
		this.closeEnough = closeEnough;
		this.stabilizeLook = stabilizeLook;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob entity) {
		Vec3 precisePos = entity.getBrain().getMemory(MemoryModuleTypeInit.PRECISE_POS.get()).get();
		BlockPos blockPos = new BlockPos(precisePos);
		if (!level.loadedAndEntityCanStandOn(blockPos.below(), entity)) return false;		
		Vec3 entityPos = entity.position();
		return precisePos.closerThan(entityPos, this.precisionDistance) && !precisePos.closerThan(entityPos, this.closeEnough);
	}
	
	@Override
	protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
		Vec3 precisePos = entity.getBrain().getMemory(MemoryModuleTypeInit.PRECISE_POS.get()).get();
		
		if (this.stabilizeLook) {
			Vec3 eyePos = entity.getEyePosition(1.0f).add(entity.getViewVector(1.0f).scale(2.0f));
			entity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new PosWrapper(eyePos));
		}
		
		entity.getMoveControl().setWantedPosition(precisePos.x, precisePos.y, precisePos.z, this.speedModifier);
		if (precisePos.y - entity.position().y >= 0.5d) entity.getJumpControl().jump();
		entity.getBrain().eraseMemory(MemoryModuleTypeInit.PRECISE_POS.get());
	}
	
}
