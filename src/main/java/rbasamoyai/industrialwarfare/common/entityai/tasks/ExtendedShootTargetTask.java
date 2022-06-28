package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob.ShootingStatus;

public class ExtendedShootTargetTask<E extends Mob & IWeaponRangedAttackMob> extends Behavior<E> {

	private ShootingStatus status = ShootingStatus.UNLOADED;
	
	public ExtendedShootTargetTask() {
		super(ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT),
				1200);
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel world, E shooter) {
		LivingEntity target = getAttackTarget(shooter);
		return shooter.canDoRangedAttack() && BehaviorUtils.canSee(shooter, target) && BehaviorUtils.isWithinAttackRange(shooter, target, 0);
	}
	
	@Override
	protected void start(ServerLevel world, E shooter, long gameTime) {
		this.status = shooter.getNextStatus();
	}
	
	@Override
	protected boolean canStillUse(ServerLevel world, E shooter, long gameTime) {
		return shooter.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(world, shooter);
	}
	
	@Override
	protected void tick(ServerLevel world, E shooter, long gameTime) {
		LivingEntity target = getAttackTarget(shooter);
		BehaviorUtils.lookAtEntity(shooter, target);
		this.attackTarget(shooter, target);
	}
	
	@Override
	protected void stop(ServerLevel world, E shooter, long gameTime) {
		shooter.stopRangedAttack();
	}
	
	private void attackTarget(E shooter, LivingEntity target) {
		if (!shooter.canDoRangedAttack()) return;
		
		if (this.status == ShootingStatus.UNLOADED) {
			shooter.startReloading();
			this.status = ShootingStatus.RELOADING;
		} else if (this.status == ShootingStatus.RELOADING) {
			if (shooter.whileReloading()) return;
			this.status = ShootingStatus.READY_TO_FIRE;
		} else if (this.status == ShootingStatus.READY_TO_FIRE) {
			if (shooter.whileWaitingToAttack()) return;
			
			shooter.performRangedAttack(target, 0.0f);
			this.status = ShootingStatus.FIRED;
		} else if (this.status == ShootingStatus.FIRED) {
			if (shooter.whileCoolingDown()) return;
			
			this.status = shooter.getNextStatus();
			switch (this.status) {
			case CYCLING: shooter.startCycling(); break;
			case RELOADING: shooter.startReloading(); break;
			default: break;
			}
		} else if (this.status == ShootingStatus.CYCLING) {
			if (shooter.whileCycling()) return;
			this.status = ShootingStatus.READY_TO_FIRE;
		}
	}
	
	private static LivingEntity getAttackTarget(LivingEntity attacker) {
		return attacker.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
	}
	
}
