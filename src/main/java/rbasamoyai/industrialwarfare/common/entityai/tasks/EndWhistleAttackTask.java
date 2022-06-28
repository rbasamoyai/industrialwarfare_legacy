package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class EndWhistleAttackTask extends Behavior<LivingEntity> {

	public EndWhistleAttackTask() {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.COMBAT_MODE.get(), MemoryStatus.VALUE_PRESENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
		return entity.getBrain().getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).get() == CombatMode.DONT_ATTACK;
	}
	
	@Override
	protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
		entity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
	}
	
}
