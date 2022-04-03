package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob.ShootingStatus;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class PrepareForShootingTask<E extends MobEntity & IWeaponRangedAttackMob> extends Task<E> {

	private final MemoryModuleType<?> noType;
	private ShootingStatus status = ShootingStatus.READY_TO_FIRE;
	
	public PrepareForShootingTask(MemoryModuleType<?> noType) {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get(), MemoryModuleStatus.REGISTERED));
		this.noType = noType;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		Brain<?> brain = entity.getBrain();
		return !brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && !brain.hasMemoryValue(this.noType)
			|| brain.getMemory(MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get()).orElse(false);
	}
	
	@Override
	protected void start(ServerWorld level, E entity, long gameTime) {
		this.status = entity.getNextStatus();
	}
	
	@Override
	protected boolean canStillUse(ServerWorld level, E entity, long gameTime) {
		return this.status == ShootingStatus.FIRED || this.status == ShootingStatus.CYCLING || this.status == ShootingStatus.RELOADING;
	}
	
	@Override
	protected void tick(ServerWorld level, E entity, long gameTime) {
		if (this.status == ShootingStatus.FIRED && !entity.whileCoolingDown()
			|| this.status == ShootingStatus.CYCLING && !entity.whileCycling()
			|| this.status == ShootingStatus.RELOADING && !entity.whileReloading()) {
			this.status = entity.getNextStatus();
			switch (this.status) {
			case CYCLING: entity.startCycling(); break;
			case RELOADING: entity.startReloading(); break;
			default: this.status = ShootingStatus.READY_TO_FIRE;
			}
		}
	}
	
	@Override
	protected void stop(ServerWorld level, E shooter, long gameTime) {
		shooter.getBrain().eraseMemory(MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get());
	}
	
}
