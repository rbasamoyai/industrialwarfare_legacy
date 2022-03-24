package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class EndWhistleAttackTask extends Task<LivingEntity> {

	public EndWhistleAttackTask() {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.COMBAT_MODE.get(), MemoryModuleStatus.VALUE_PRESENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, LivingEntity entity) {
		return entity.getBrain().getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).get() == CombatMode.DONT_ATTACK;
	}
	
	@Override
	protected void start(ServerWorld level, LivingEntity entity, long gameTime) {
		entity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
	}
	
}
