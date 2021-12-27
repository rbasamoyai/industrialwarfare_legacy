package rbasamoyai.industrialwarfare.common.entityai.tasks;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.world.server.ServerWorld;

public class WalkToTargetNoCooldownTask extends WalkToTargetTask {

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, MobEntity entity) {
		Brain<?> brain = entity.getBrain();
		WalkTarget walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
		boolean reachedTarget = this.reachedTarget(entity, walkTarget);
		if (!reachedTarget && this.tryComputePath(entity, walkTarget, level.getGameTime())) {
			this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();
			return true;
		}
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		if (reachedTarget) brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		return false;
	}
	
}
