package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class JoinNearbyFormationTask<E extends CreatureEntity & IMovesInFormation> extends Task<E> {

	private int remainingCooldown;
	
	public JoinNearbyFormationTask() {
		super(ImmutableMap.of(
				MemoryModuleType.LIVING_ENTITIES, MemoryModuleStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryModuleStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.IN_FORMATION.get(), MemoryModuleStatus.VALUE_ABSENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		if (this.remainingCooldown > 0) {
			--this.remainingCooldown;
			return false;
		}
		return true;
	}
	
	@Override
	protected void start(ServerWorld level, E entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		UUID commandGroup = brain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
		List<LivingEntity> nearbyEntities = brain.getMemory(MemoryModuleType.LIVING_ENTITIES).get();
				
		for (LivingEntity e : nearbyEntities) {
			Brain<?> otherBrain = e.getBrain();
			if (e instanceof FormationLeaderEntity
				&& otherBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())
				&& otherBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get().equals(commandGroup)) {
				if (((FormationLeaderEntity) e).addEntity(entity)) 	break;
			}
		}
		
		this.remainingCooldown = 20;
	}
	
}
