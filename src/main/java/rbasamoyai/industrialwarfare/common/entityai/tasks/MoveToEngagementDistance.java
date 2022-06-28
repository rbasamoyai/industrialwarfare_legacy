package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class MoveToEngagementDistance extends Behavior<LivingEntity> {

	private final int distance;
	
	public MoveToEngagementDistance(int distance) {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
				MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), MemoryStatus.VALUE_ABSENT));
		this.distance = distance;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
		if (!entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return false;
		LivingEntity target = this.getAttackTarget(entity);
		return target != null && target.isAlive();
	}
	
	@Override
	protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		brain.setMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), false);
	}
	
	@Override
	protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
		return this.checkExtraStartConditions(level, entity) && !this.getAttackTarget(entity).position().closerThan(entity.position(), this.distance);
	}
	
	@Override
	protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		LivingEntity target = this.getAttackTarget(entity);
		if (!brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(target, false), 2.0f, this.distance));
		}
	}
	
	@Override
	protected void stop(ServerLevel level, LivingEntity entity, long gameTime) {
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
