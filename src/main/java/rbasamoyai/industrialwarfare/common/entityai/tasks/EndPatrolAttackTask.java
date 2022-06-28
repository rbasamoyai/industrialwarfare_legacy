package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class EndPatrolAttackTask extends Behavior<LivingEntity> {

	public EndPatrolAttackTask() {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.CACHED_POS.get(), MemoryStatus.REGISTERED,
				MemoryModuleTypeInit.ON_PATROL.get(), MemoryStatus.VALUE_PRESENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel world, LivingEntity entity) {
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
	protected void start(ServerLevel world, LivingEntity entity, long gameTime) {
		entity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
	}
	
}
