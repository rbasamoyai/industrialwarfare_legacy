package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.Optional;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class WalkTowardsPosNoDelayTask extends StrollToPoi {

	public WalkTowardsPosNoDelayTask(MemoryModuleType<GlobalPos> memoryType, float speedModifier, int closeEnough, int maxDistanceFromPoi) {
		super(memoryType, speedModifier, closeEnough, maxDistanceFromPoi);
	}

	@Override
	protected void start(ServerLevel level, PathfinderMob entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		Optional<GlobalPos> optional = brain.getMemory(this.memoryType);
		optional.ifPresent(gp -> {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(gp.pos(), this.speedModifier, this.closeEnoughDist));
		});
	}
	
}
