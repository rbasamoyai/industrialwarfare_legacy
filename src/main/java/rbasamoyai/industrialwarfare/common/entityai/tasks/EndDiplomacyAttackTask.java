package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.HasDiplomaticOwner;

public class EndDiplomacyAttackTask<E extends LivingEntity & HasDiplomaticOwner> extends Behavior<E> {

	public EndDiplomacyAttackTask() {
		super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		LivingEntity target = this.getAttackTarget(entity);
		if (!(target instanceof HasDiplomaticOwner)) return false;
		PlayerIDTag owner = entity.getDiplomaticOwner();
		PlayerIDTag targetOwner = ((HasDiplomaticOwner) target).getDiplomaticOwner();
		if (owner.equals(targetOwner)) return true;
		DiplomaticStatus status = DiplomacySaveData.get(level).getDiplomaticStatus(owner, targetOwner);
		// TODO: neutral and unknown
		return status == DiplomaticStatus.ALLY;
	}
	
	@Override
	protected void start(ServerLevel level, E entity, long gameTime) {
		entity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
	}
	
	private LivingEntity getAttackTarget(E entity) {
		return entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
	}
	
}
