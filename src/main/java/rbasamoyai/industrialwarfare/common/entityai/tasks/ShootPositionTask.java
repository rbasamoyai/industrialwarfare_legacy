package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.Item;
import net.minecraft.item.ShootableItem;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob.ShootingStatus;
import rbasamoyai.industrialwarfare.common.entityai.navigation.PosWrapper;

public class ShootPositionTask<E extends CreatureEntity & IWeaponRangedAttackMob> extends Task<E> {

	private final MemoryModuleType<IPosition> memoryType;
	private ShootingStatus status = ShootingStatus.UNLOADED;
	
	public ShootPositionTask(MemoryModuleType<IPosition> type) {
		super(ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED,
				type, MemoryModuleStatus.VALUE_PRESENT),
				1200);
		this.memoryType = type;		
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E shooter) {
		if (!shooter.canDoRangedAttack()) return false;
		
		Item item = shooter.getMainHandItem().getItem();
		if (!(item instanceof ShootableItem)) return false;
		int range = ((ShootableItem) item).getDefaultProjectileRange() + 5;
		return shooter.position().closerThan(this.getAttackTarget(shooter), range);
	}
	
	@Override
	protected void start(ServerWorld level, E shooter, long gameTime) {
		this.status = shooter.getNextStatus();
	}
	
	@Override
	protected boolean canStillUse(ServerWorld level, E shooter, long gameTime) {
		return shooter.getBrain().hasMemoryValue(this.memoryType) && this.checkExtraStartConditions(level, shooter);
	}
	
	@Override
	protected void tick(ServerWorld level, E shooter, long gameTime) {
		IPosition target = this.getAttackTarget(shooter);
		shooter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new PosWrapper(target));
		this.attackTarget(shooter, target);
	}
	
	@Override
	protected void stop(ServerWorld world, E shooter, long gameTime) {
		shooter.stopRangedAttack();
	}
	
	private void attackTarget(E shooter, IPosition target) {
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
	
	private IPosition getAttackTarget(LivingEntity attacker) {
		return attacker.getBrain().getMemory(this.memoryType).get();
	}
	
}
