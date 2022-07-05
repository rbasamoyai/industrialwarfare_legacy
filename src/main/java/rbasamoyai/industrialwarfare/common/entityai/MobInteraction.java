package rbasamoyai.industrialwarfare.common.entityai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;

public class MobInteraction {

	private final Mob mob;
	private final Type action;
	private final SupplyRequestPredicate item;
	private final OnMobInteraction mobInteraction;
	private final CheckMobStatus checkStatus;
	
	public MobInteraction(Mob mob, Type action, SupplyRequestPredicate item, OnMobInteraction interaction, CheckMobStatus checkStatus) {
		this.mob = mob;
		this.action = action;
		this.item = item;
		this.mobInteraction = interaction;
		this.checkStatus = checkStatus;
	}
	
	public Mob mob() { return this.mob; }
	public Type action() { return this.action; }
	public SupplyRequestPredicate item() { return this.item; }
	
	public void doMobInteractionIfPossible(LivingEntity actor) {
		if (this.mobInteraction != null && this.mob != null) {
			this.mobInteraction.interactWithMob(this.mob, actor);
		}
	}
	
	public boolean canStillInteract(LivingEntity actor) {
		return this.checkStatus != null && this.mob != null ? this.checkStatus.canInteractWithMob(this.mob, actor) : false;
	}
	
	public boolean isInRange(Mob entity) {
		return this.mob == null ? false : BehaviorUtils.isWithinMeleeAttackRange(entity, this.mob);
	}
	
	public static MobInteraction killMob(Mob mob, SupplyRequestPredicate item) {
		return new MobInteraction(mob, Type.KILL_MOB, item, null, (t, a) -> t.isAlive());
	}
	
	public static MobInteraction useItemOnMob(Mob mob, SupplyRequestPredicate item, OnMobInteraction interaction, CheckMobStatus checkStatus) {
		return new MobInteraction(mob, Type.USE_ON_MOB, item, interaction, checkStatus);
	}
	
	@FunctionalInterface
	public static interface OnMobInteraction {
		void interactWithMob(Mob target, LivingEntity actor);
	}
	
	@FunctionalInterface
	public static interface CheckMobStatus {
		boolean canInteractWithMob(Mob target, LivingEntity actor);
	}
	
	public static enum Type {
		KILL_MOB,
		USE_ON_MOB
	}
	
}
