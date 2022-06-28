package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob.ShootingStatus;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class PrepareForShootingTask<E extends Mob & IWeaponRangedAttackMob> extends Behavior<E> {

	private final MemoryModuleType<?> noType;
	private ShootingStatus status = ShootingStatus.READY_TO_FIRE;
	
	public PrepareForShootingTask(MemoryModuleType<?> noType) {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
				MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get(), MemoryStatus.REGISTERED,
				noType, MemoryStatus.VALUE_ABSENT));
		this.noType = noType;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		Brain<?> brain = entity.getBrain();
		return !brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && !brain.hasMemoryValue(this.noType)
				&& brain.getMemory(MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get()).orElse(true);
	}
	
	@Override
	protected void start(ServerLevel level, E entity, long gameTime) {
		this.status = entity.getNextStatus();
	}
	
	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		if (!this.checkExtraStartConditions(level, entity)) return false;
		return this.status == ShootingStatus.FIRED || this.status == ShootingStatus.CYCLING || this.status == ShootingStatus.RELOADING;
	}
	
	@Override
	protected void tick(ServerLevel level, E entity, long gameTime) {
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
	protected void stop(ServerLevel level, E shooter, long gameTime) {
		shooter.getBrain().eraseMemory(MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get());
	}
	
}
