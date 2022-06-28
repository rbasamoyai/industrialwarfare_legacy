package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob.ShootingStatus;
import rbasamoyai.industrialwarfare.common.entityai.navigation.PosWrapper;

public class ShootPositionTask<E extends PathfinderMob & IWeaponRangedAttackMob> extends Behavior<E> {

	private final MemoryModuleType<Position> memoryType;
	private ShootingStatus status = ShootingStatus.UNLOADED;
	
	public ShootPositionTask(MemoryModuleType<Position> type) {
		super(ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
				type, MemoryStatus.VALUE_PRESENT),
				1200);
		this.memoryType = type;		
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E shooter) {
		if (!shooter.canDoRangedAttack()) return false;
		
		Item item = shooter.getMainHandItem().getItem();
		if (!(item instanceof ProjectileWeaponItem)) return false;
		int range = ((ProjectileWeaponItem) item).getDefaultProjectileRange() + 5;
		return shooter.position().closerThan(this.getAttackTarget(shooter), range);
	}
	
	@Override
	protected void start(ServerLevel level, E shooter, long gameTime) {
		this.status = shooter.getNextStatus();
	}
	
	@Override
	protected boolean canStillUse(ServerLevel level, E shooter, long gameTime) {
		return shooter.getBrain().hasMemoryValue(this.memoryType) && this.checkExtraStartConditions(level, shooter);
	}
	
	@Override
	protected void tick(ServerLevel level, E shooter, long gameTime) {
		Position target = this.getAttackTarget(shooter);
		shooter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new PosWrapper(target));
		this.attackTarget(shooter, target);
	}
	
	@Override
	protected void stop(ServerLevel world, E shooter, long gameTime) {
		shooter.stopRangedAttack();
	}
	
	private void attackTarget(E shooter, Position target) {
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
	
	private Position getAttackTarget(LivingEntity attacker) {
		return attacker.getBrain().getMemory(this.memoryType).get();
	}
	
}
