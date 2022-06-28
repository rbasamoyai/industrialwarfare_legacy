package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class ReturnToWorkIfPatrollingTask extends Behavior<NPCEntity> {

	public ReturnToWorkIfPatrollingTask() {
		super(ImmutableMap.<MemoryModuleType<?>, MemoryStatus>builder()
				.put(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
				.put(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), MemoryStatus.VALUE_PRESENT)
				.put(MemoryModuleTypeInit.ON_PATROL.get(), MemoryStatus.VALUE_PRESENT)
				.build());
	}
	
	@Override
	protected void start(ServerLevel world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		
		brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), ActivityStatus.WORKING);
		brain.setActiveActivityIfPossible(Activity.WORK);
		
		brain.eraseMemory(MemoryModuleTypeInit.ON_PATROL.get());
	}
	
}
