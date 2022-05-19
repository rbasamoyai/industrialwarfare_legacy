package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class StopSelfDefenseTask extends Task<MobEntity> {

	private final Activity nextActivity;
	
	public StopSelfDefenseTask(Activity nextActivity) {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.DEFENDING_SELF.get(), MemoryModuleStatus.VALUE_PRESENT));
		this.nextActivity = nextActivity;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, MobEntity entity) {
		return entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map(target -> {
			if (target instanceof PlayerEntity && (((PlayerEntity) target).isCreative() || ((PlayerEntity) target).isSpectator())) {
				return true;
			}
			return target.isDeadOrDying();
		}).orElse(true);
	}
	
	@Override
	protected void start(ServerWorld level, MobEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
		brain.eraseMemory(MemoryModuleTypeInit.DEFENDING_SELF.get());
		if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
			brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		}
		brain.setActiveActivityIfPossible(this.nextActivity);
	}
	
}
