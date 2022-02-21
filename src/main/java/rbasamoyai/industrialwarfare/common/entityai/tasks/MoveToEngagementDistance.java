package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.EntityPosWrapper;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class MoveToEngagementDistance extends Task<LivingEntity> {

	private final int distance;
	
	public MoveToEngagementDistance(int distance) {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT,
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT,
				MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), MemoryModuleStatus.REGISTERED));
		this.distance = distance;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, LivingEntity entity) {
		LivingEntity target = this.getAttackTarget(entity);
		return target != null && target.isAlive();
	}
	
	@Override
	protected void start(ServerWorld level, LivingEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		brain.setMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), false);
	}
	
	@Override
	protected boolean canStillUse(ServerWorld level, LivingEntity entity, long gameTime) {
		return this.checkExtraStartConditions(level, entity) && !this.getAttackTarget(entity).position().closerThan(entity.position(), this.distance);
	}
	
	@Override
	protected void tick(ServerWorld level, LivingEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		LivingEntity target = this.getAttackTarget(entity);
		if (!brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityPosWrapper(target, false), 2.0f, this.distance));
		}
	}
	
	@Override
	protected void stop(ServerWorld level, LivingEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		brain.setMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), true);
	}
	
	@Override 
	protected boolean timedOut(long gameTime) {
		return false;
	}
	
	private LivingEntity getAttackTarget(LivingEntity entity) {
		return entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
	}
}
