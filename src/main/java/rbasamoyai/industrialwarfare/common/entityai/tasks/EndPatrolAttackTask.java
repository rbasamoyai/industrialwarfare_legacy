package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class EndPatrolAttackTask extends Task<LivingEntity> {

	public EndPatrolAttackTask() {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.CACHED_POS.get(), MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.ON_PATROL.get(), MemoryModuleStatus.VALUE_PRESENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld world, LivingEntity entity) {
		Brain<?> brain = entity.getBrain();
		LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
		Optional<GlobalPos> gpop = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get());
		BlockPos pos;
		if (gpop.isPresent()) {
			pos = gpop.get().pos();
		} else {
			pos = entity.blockPosition();
			brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(world.dimension(), pos));
		}
		
		return !target.blockPosition().closerThan(pos, brain.getMemory(MemoryModuleTypeInit.ON_PATROL.get()).get());
	}
	
	@Override
	protected void start(ServerWorld world, LivingEntity entity, long gameTime) {
		entity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
	}
	
}
