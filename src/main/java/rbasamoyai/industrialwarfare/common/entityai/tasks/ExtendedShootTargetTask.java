package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;

public class ExtendedShootTargetTask<E extends MobEntity & IWeaponRangedAttackMob> extends Task<E> {

	private int attackDelay;
	private Status status = Status.UNLOADED;
	
	public ExtendedShootTargetTask() {
		super(ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT),
				1200);
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld world, E shooter) {
		LivingEntity target = getAttackTarget(shooter);
		return shooter.canDoRangedAttack() && BrainUtil.canSee(shooter, target) && BrainUtil.isWithinAttackRange(shooter, target, 0);
	}
	
	@Override
	protected void start(ServerWorld world, E shooter, long gameTime) {
		this.status = Status.UNLOADED;
	}
	
	@Override
	protected boolean canStillUse(ServerWorld world, E shooter, long gameTime) {
		return shooter.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(world, shooter);
	}
	
	@Override
	protected void tick(ServerWorld world, E shooter, long gameTime) {
		LivingEntity target = getAttackTarget(shooter);
		BrainUtil.lookAtEntity(shooter, target);
		this.attackTarget(shooter, target);
	}
	
	@Override
	protected void stop(ServerWorld world, E shooter, long gameTime) {
		if (shooter.isUsingItem()) {
			shooter.stopUsingItem();
		}
	}
	
	private void attackTarget(E shooter, LivingEntity target) {
		if (!shooter.canDoRangedAttack()) return;
		
		if (this.status == Status.UNLOADED) {
			shooter.startReloading();
			this.status = Status.RELOADING;
		} else if (this.status == Status.RELOADING) {
			if (shooter.whileReloading()) return;
			this.attackDelay = shooter.getRangedAttackDelay(); 
			this.status = Status.READY_TO_FIRE;
		} else if (this.status == Status.READY_TO_FIRE) {
			--this.attackDelay;
			if (this.attackDelay > 0) return;
			shooter.performRangedAttack(target, 0.0f);
			this.status = Status.FIRED;
		} else if (this.status == Status.FIRED) {
			this.status = shooter.cycleOrReload() ? Status.CYCLING : Status.UNLOADED;
			if (this.status == Status.CYCLING) shooter.startCycling();
		} else if (this.status == Status.CYCLING) {
			if (shooter.whileCycling()) return;
			this.status = Status.READY_TO_FIRE;
		}
	}
	
	private static LivingEntity getAttackTarget(LivingEntity attacker) {
		return attacker.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
	}
	
	public static enum Status {
		UNLOADED,
		RELOADING,
		READY_TO_FIRE,
		FIRED,
		CYCLING
	}
	
}
