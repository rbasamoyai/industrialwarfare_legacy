package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class MoveToCommand extends TaskScrollCommand {
	
	private static final int POS_ARG_INDEX = 0;
	
	public MoveToCommand() {
		super(CommandTrees.POS_ONLY, ImmutableMap.of(
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED
				));
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		return CommandUtils.validateStandingPos(world, npc, order.getWrappedArg(POS_ARG_INDEX).getPos(), TaskScrollCommand.MAX_DISTANCE_FROM_POI, NPCComplaintInit.INVALID_ORDER.get());
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		
		npc.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
	}
	
	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2));
		
		if (box.contains(npc.position())) {
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
		} else if (npc.getNavigation().isDone()) {
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
		}
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		if (!CommandUtils.hasComplaint(npc)) {
			CommandUtils.incrementCurrentInstructionIndexMemory(npc);
		}
	}

}
