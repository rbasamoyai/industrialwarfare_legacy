package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.IHasDiplomaticOwner;

public class EndDiplomacyAttackTask<E extends LivingEntity & IHasDiplomaticOwner> extends Task<E> {

	public EndDiplomacyAttackTask() {
		super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_PRESENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		LivingEntity target = this.getAttackTarget(entity);
		if (!(target instanceof IHasDiplomaticOwner)) return false;
		PlayerIDTag owner = entity.getDiplomaticOwner();
		PlayerIDTag targetOwner = ((IHasDiplomaticOwner) target).getDiplomaticOwner();
		if (owner.equals(targetOwner)) return true;
		DiplomaticStatus status = DiplomacySaveData.get(level).getDiplomaticStatus(owner, targetOwner);
		// TODO: neutral and unknown
		return status == DiplomaticStatus.ALLY;
	}
	
	@Override
	protected void start(ServerWorld level, E entity, long gameTime) {
		entity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
	}
	
	private LivingEntity getAttackTarget(E entity) {
		return entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
	}
	
}
