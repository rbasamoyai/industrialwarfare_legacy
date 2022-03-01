package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class ReturnToWorkIfPatrollingTask extends Task<NPCEntity> {

	public ReturnToWorkIfPatrollingTask() {
		super(ImmutableMap.<MemoryModuleType<?>, MemoryModuleStatus>builder()
				.put(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_ABSENT)
				.put(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), MemoryModuleStatus.VALUE_PRESENT)
				.put(MemoryModuleTypeInit.ON_PATROL.get(), MemoryModuleStatus.VALUE_PRESENT)
				.build());
	}
	
	@Override
	protected void start(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		
		brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), ActivityStatus.WORKING);
		brain.setActiveActivityIfPossible(Activity.WORK);
		
		brain.eraseMemory(MemoryModuleTypeInit.ON_PATROL.get());
	}
	
}
