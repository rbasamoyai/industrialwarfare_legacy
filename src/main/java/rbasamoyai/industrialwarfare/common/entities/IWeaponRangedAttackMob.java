package rbasamoyai.industrialwarfare.common.entities;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import rbasamoyai.industrialwarfare.common.entityai.tasks.ExtendedShootTargetTask;

public interface IWeaponRangedAttackMob extends IRangedAttackMob {

	void startReloading();
	
	/**
	 * @return false when done reloading, true otherwise
	 */
	boolean whileReloading();
	
	int getRangedAttackDelay();
	
	/**
	 * @return true if mob should cycle weapon, false if should reload
	 */
	ExtendedShootTargetTask.Status getNextStatus();
	
	void startCycling();
	
	/**
	 * @return false when done cycling, true otherwise
	 */
	boolean whileCycling();
	
	boolean canDoRangedAttack();
	
	LivingEntity getTarget();
	
}
