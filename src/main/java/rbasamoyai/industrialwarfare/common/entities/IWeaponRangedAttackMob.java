package rbasamoyai.industrialwarfare.common.entities;

import net.minecraft.entity.IRangedAttackMob;

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
	boolean cycleOrReload();
	
	void startCycling();
	
	/**
	 * @return false when done cycling, true otherwise
	 */
	boolean whileCycling();
	
	boolean canDoRangedAttack();
	
}
