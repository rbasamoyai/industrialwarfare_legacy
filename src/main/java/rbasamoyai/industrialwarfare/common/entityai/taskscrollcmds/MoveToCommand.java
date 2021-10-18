package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Optional;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;

public class MoveToCommand extends TaskScrollCommand {
	
	private static final int POS_ARG_INDEX = 0;
	
	public MoveToCommand() {
		super(CommandTrees.POS_ONLY);
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		Optional<BlockPos> pos = order.getWrappedArg(POS_ARG_INDEX).getPos();
		if (!pos.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_ORDER.get());
			return false;
		}
		
		boolean inRange = pos.get().closerThan(npc.position(), TaskScrollCommand.MAX_DISTANCE_FROM_POI);
		if (!(world.loadedAndEntityCanStandOn(pos.get(), npc) && world.noCollision(npc) && inRange)) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(),
					inRange 
					? NPCComplaintInit.CANT_ACCESS.get()
					: NPCComplaintInit.TOO_FAR.get());
			return false;
		}
		
		return true;
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(pos));
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
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
		Brain<?> brain = npc.getBrain();
		int index = brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX.get()).orElse(0);
		brain.setMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX.get(), index + 1);
	}

}
