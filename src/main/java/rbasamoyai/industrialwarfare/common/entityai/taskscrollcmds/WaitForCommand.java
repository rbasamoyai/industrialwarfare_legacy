package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.TimeUtils;

public class WaitForCommand extends TaskScrollCommand {
	
	private static final int WAIT_MODE_ARG_INDEX = 0;
	private static final int WAIT_TIME_ARG_INDEX = 1;
	
	public WaitForCommand() {
		super(CommandTrees.WAIT_FOR);
		this.setRegistryName(IndustrialWarfare.MOD_ID, "wait_for");
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		int waitMode = order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum();
		boolean doingDaylightCycle = world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
		
		if (waitMode == WaitModes.DAY_TIME && !doingDaylightCycle) {
			// somehow the NPCs sense that doDaylightCycle is false, don't ask how
			npc.getBrain().setMemory(MemoryModuleTypeInit.COMPLAINT, NPCComplaintInit.TIME_STOPPED);
		}
		
		return waitMode == WaitModes.DAY_TIME && doingDaylightCycle || waitMode == WaitModes.RELATIVE_TIME || waitMode == WaitModes.BELL;
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		int waitMode = order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum();
		int waitTimeArg = order.getWrappedArg(WAIT_TIME_ARG_INDEX).getArgNum();
		
		long waitTime = (long) waitTimeArg * 20L;
		
		long waitUntil = 0;
		
		if (waitMode == WaitModes.DAY_TIME) {
			waitUntil = waitTime;
		} else if (waitMode == WaitModes.RELATIVE_TIME) {
			waitUntil = gameTime + waitTime;
		} else if (waitMode == WaitModes.BELL) {
			
		}
		
		brain.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
		if (waitMode != WaitModes.BELL) brain.setMemory(MemoryModuleTypeInit.WAIT_FOR, waitUntil);
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		boolean heardBell = brain.hasMemoryValue(MemoryModuleType.HEARD_BELL_TIME);
		long waitUntil = brain.getMemory(MemoryModuleTypeInit.WAIT_FOR).orElse(0L);
		int waitMode = order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum();
		
		if (waitMode == WaitModes.DAY_TIME && !world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT, NPCComplaintInit.TIME_STOPPED);
		}
		
		if (waitMode == WaitModes.DAY_TIME && (int)((world.getDayTime() + TimeUtils.TIME_OFFSET) % 24000L) >= waitUntil 
				|| waitMode == WaitModes.RELATIVE_TIME && gameTime >= waitUntil
				|| waitMode == WaitModes.BELL && heardBell) {
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION, true);
		}
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.COMPLAINT)) {
			brain.setMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX, brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX).orElse(0) + 1);
		}
		brain.eraseMemory(MemoryModuleTypeInit.WAIT_FOR);
	}

	public static class WaitModes {
		public static final int DAY_TIME = 0;
		public static final int RELATIVE_TIME = 1;
		public static final int BELL = 2;
		
		public static final int[] VALUES = new int[] {DAY_TIME, RELATIVE_TIME, BELL};
	}
	
}
