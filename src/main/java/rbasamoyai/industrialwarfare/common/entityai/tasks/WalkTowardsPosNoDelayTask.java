package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.Optional;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.WalkTowardsPosTask;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class WalkTowardsPosNoDelayTask extends WalkTowardsPosTask {

	public WalkTowardsPosNoDelayTask(MemoryModuleType<GlobalPos> memoryType, float speedModifier, int closeEnough, int maxDistanceFromPoi) {
		super(memoryType, speedModifier, closeEnough, maxDistanceFromPoi);
	}

	@Override
	protected void start(ServerWorld level, CreatureEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		Optional<GlobalPos> optional = brain.getMemory(this.memoryType);
		optional.ifPresent(gp -> {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(gp.pos(), this.speedModifier, this.closeEnoughDist));
		});
	}
	
}
