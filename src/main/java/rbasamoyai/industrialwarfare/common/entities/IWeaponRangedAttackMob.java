package rbasamoyai.industrialwarfare.common.entities;

import net.minecraft.core.Position;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.LivingEntity;

public interface IWeaponRangedAttackMob extends RangedAttackMob {

	enum ShootingStatus {
		UNLOADED,
		RELOADING,
		READY_TO_FIRE,
		FIRED,
		CYCLING
	}

	void startReloading();
	
	/**
	 * @return false when done reloading, true otherwise
	 */
	boolean whileReloading();
	
	boolean whileWaitingToAttack();
	
	/**
	 * @return true if mob should cycle weapon, false if should reload
	 */
	ShootingStatus getNextStatus();
	
	void performRangedAttack(Position target, float damage);
	
	/**
	 * @return false when done cooling down, true otherwise
	 */
	boolean whileCoolingDown();
	
	void startCycling();
	
	/**
	 * @return false when done cycling, true otherwise
	 */
	boolean whileCycling();
	
	boolean canDoRangedAttack();
	
	LivingEntity getTarget();
	
	void stopRangedAttack();
	
}
