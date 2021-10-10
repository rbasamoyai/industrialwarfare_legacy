package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.wait_for;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class WaitForCommand extends TaskScrollCommand {
	
	private static final List<Supplier<IArgHolder>> WAIT_FOR_ARGS = ImmutableList.of(WaitModeArgHolder::new, WaitTimeArgHolder::new);
	private static final int WAIT_MODE_ARG_INDEX = 0;
	private static final int WAIT_TIME_ARG_INDEX = 1;
	
	public WaitForCommand() {
		super(WAIT_FOR_ARGS);
		this.setRegistryName(IndustrialWarfare.MOD_ID, "wait_for");
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		return true;
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		int waitMode = order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum();
		int waitTimeArg = order.getWrappedArg(WAIT_TIME_ARG_INDEX).getArgNum();
		
		long waitTime = (long) waitTimeArg * 20L;
		
		long waitUntil = 0;
		waitMode = 1; // TODO: need to implement command trees or something so that the order can be even more versatile, default to relative time for now
		
		if (waitMode == 0) { // Day time mode
			
		} else if (waitMode == 1) { // Relative time mode
			waitUntil = gameTime + waitTime;
		} else if (waitMode == 2) {
			// TODO: bell, for now just put day time code here
		} else {
			// TODO: complain about invalid instruction
		}
		
		brain.setMemory(MemoryModuleTypeInit.WAIT_FOR, waitUntil);
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		long waitUntil = npc.getBrain().getMemory(MemoryModuleTypeInit.WAIT_FOR).orElse(0L);
		int waitMode = order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum();
		
		if (waitMode == 1 && gameTime >= waitUntil) {
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION, true);
		} else {
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION, true);
		}
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		brain.eraseMemory(MemoryModuleTypeInit.WAIT_FOR);
	}

	@Override
	public boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		return true;
	}

}
