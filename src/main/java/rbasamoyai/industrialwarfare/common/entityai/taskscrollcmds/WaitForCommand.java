package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WaitMode;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class WaitForCommand extends TaskScrollCommand {
	
	private static final int WAIT_MODE_ARG_INDEX = 0;
	private static final int WAIT_TIME_ARG_INDEX = 1;
	
	public WaitForCommand() {
		super(CommandTrees.WAIT_FOR, ImmutableMap.of(
				MemoryModuleType.HEARD_BELL_TIME, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.WAIT_FOR.get(), MemoryModuleStatus.REGISTERED
				));
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		return CommandUtils.validateWait(world, npc, WaitMode.fromId(order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum()), NPCComplaintInit.INVALID_ORDER.get());
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		WaitMode mode = WaitMode.fromId(order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum());
		long waitTime = (long) order.getWrappedArg(WAIT_TIME_ARG_INDEX).getArgNum() * 20L;
		CommandUtils.startWait(npc, mode, gameTime, waitTime);
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		CommandUtils.tickWait(world, npc, WaitMode.fromId(order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum()), gameTime);
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		if (!CommandUtils.hasComplaint(npc)) {
			CommandUtils.incrementCurrentInstructionIndexMemory(npc);
		}
		brain.eraseMemory(MemoryModuleTypeInit.WAIT_FOR.get());
	}
	
}
