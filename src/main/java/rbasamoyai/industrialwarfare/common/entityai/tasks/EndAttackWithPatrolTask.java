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

public class EndAttackWithPatrolTask extends Task<LivingEntity> {

	public EndAttackWithPatrolTask() {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.CACHED_POS.get(), MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.ON_PATROL.get(), MemoryModuleStatus.REGISTERED));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld world, LivingEntity entity) {
		Brain<?> brain = entity.getBrain();
		LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
		if (target.isDeadOrDying()) return true;
		
		Optional<GlobalPos> gpop = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get());
		BlockPos pos;
		if (gpop.isPresent()) {
			pos = gpop.get().pos();
		} else {
			pos = entity.blockPosition();
			brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(world.dimension(), pos));
		}
		
		Optional<Integer> pursuitDistance = brain.getMemory(MemoryModuleTypeInit.ON_PATROL.get());
		return pursuitDistance.isPresent() ? !target.blockPosition().closerThan(pos, pursuitDistance.get()) : true;
	}
	
	@Override
	protected void start(ServerWorld world, LivingEntity entity, long gameTime) {
		entity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
	}
	
}
