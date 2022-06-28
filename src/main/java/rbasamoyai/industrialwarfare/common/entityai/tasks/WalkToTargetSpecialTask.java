package rbasamoyai.industrialwarfare.common.entityai.tasks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class WalkToTargetSpecialTask extends MoveToTargetSink {

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, Mob entity) {
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
