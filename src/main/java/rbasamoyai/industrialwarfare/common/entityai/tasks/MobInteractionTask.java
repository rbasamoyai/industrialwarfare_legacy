package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.MobInteraction;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class MobInteractionTask extends Behavior<NPCEntity> {

	public MobInteractionTask() {
		super(ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleTypeInit.MOB_INTERACTION.get(), MemoryStatus.VALUE_PRESENT), 1200);
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel pLevel, NPCEntity pOwner) {
		Brain<?> brain = pOwner.getBrain();
		MobInteraction interaction = brain.getMemory(MemoryModuleTypeInit.MOB_INTERACTION.get()).get();
		return interaction.action() != null && interaction.canStillInteract(pOwner) && pOwner.has(interaction.item()::matches);
	}
	
	@Override
	protected boolean canStillUse(ServerLevel pLevel, NPCEntity pEntity, long pGameTime) {
		return pEntity.getBrain().hasMemoryValue(MemoryModuleTypeInit.MOB_INTERACTION.get()) && this.checkExtraStartConditions(pLevel, pEntity);
	}
	
	@Override
	protected void tick(ServerLevel pLevel, NPCEntity pOwner, long pGameTime) {
		Brain<?> brain = pOwner.getBrain();
		MobInteraction interaction = brain.getMemory(MemoryModuleTypeInit.MOB_INTERACTION.get()).get();
		Mob mob = interaction.mob();
		
		BehaviorUtils.lookAtEntity(pOwner, mob);
		if (interaction.isInRange(pOwner)) {
			switch (interaction.action()) {
				case KILL_MOB -> {
					pOwner.doHurtTarget(mob);
				}
				case USE_ON_MOB -> {
					interaction.doMobInteractionIfPossible(pOwner);
				}
			}
		} else {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(mob, false), 3.0f, 0));
		}
	}
	
	@Override
	protected void stop(ServerLevel pLevel, NPCEntity pEntity, long pGameTime) {
		Brain<?> brain = pEntity.getBrain();
		brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		brain.eraseMemory(MemoryModuleTypeInit.MOB_INTERACTION.get());
	}
	
}
