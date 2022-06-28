package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.MovesInFormation;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class JoinNearbyFormationTask<E extends PathfinderMob & MovesInFormation> extends Behavior<E> {

	private int remainingCooldown;
	
	public JoinNearbyFormationTask() {
		super(ImmutableMap.of(
				MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.IN_FORMATION.get(), MemoryStatus.VALUE_ABSENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		if (this.remainingCooldown > 0) {
			--this.remainingCooldown;
			return false;
		}
		return true;
	}
	
	@Override
	protected void start(ServerLevel level, E entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		UUID commandGroup = brain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
		List<LivingEntity> nearbyEntities = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).get();
				
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
