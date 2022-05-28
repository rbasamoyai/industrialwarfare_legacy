package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCDataHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WaitMode;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class WorkAtCommand extends TaskScrollCommand {

	public static final int POS_ARG_INDEX = 0;
	private static final int WORK_MODE_ARG_INDEX = 1;
	private static final int WORK_TIME_ARG_INDEX = 2;
	
	public WorkAtCommand() {
		super(CommandTrees.WORK_AT, () -> ImmutableMap.of(
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.CACHED_POS.get(), MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.WAIT_FOR.get(), MemoryModuleStatus.REGISTERED
				));
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		LazyOptional<INPCDataHandler> lzop = npc.getDataHandler();
		if (!lzop.isPresent()) {
			npc.getBrain().setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NO_DATA_HANDLER.get(), 200L);
			return false;
		}
		if (lzop.map(INPCDataHandler::getProfession).map(p -> !p.checkMemories(npc)).get()) {
			npc.getBrain().setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_ORDER.get(), 200L);
			return false;
		}
		
		Optional<BlockPos> optional = order.getWrappedArg(POS_ARG_INDEX).getPos();
		if (!optional.isPresent()) {
			npc.getBrain().setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_ORDER.get(), 200L);
			return false;
		}
		BlockPos pos = optional.get();
		if (!pos.closerThan(npc.position(), TaskScrollCommand.MAX_DISTANCE_FROM_POI)) {
			npc.getBrain().setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TOO_FAR.get(), 200L);
			return false;
		}
		
		return CommandUtils.validateWait(world, npc, WaitMode.fromId(order.getWrappedArg(POS_ARG_INDEX).getArgNum()), NPCComplaintInit.INVALID_ORDER.get());
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		LazyOptional<INPCDataHandler> lzop = npc.getDataHandler();
		if (!lzop.isPresent()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NO_DATA_HANDLER.get(), 200L);
			return;
		}
		INPCDataHandler handler = lzop.resolve().get();
		
		NPCProfession profession = handler.getProfession();
		BlockPos target = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		Optional<BlockPos> posOptional = profession.getWorkingArea(world, target, npc);
		if (!posOptional.isPresent()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get(), 200L);
			return;
		}
		
		BlockPos pos = posOptional.get();
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
		brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(world.dimension(), pos));
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		LazyOptional<INPCDataHandler> lzop = npc.getDataHandler();
		if (!lzop.isPresent()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NO_DATA_HANDLER.get(), 200L);
			return;
		}
		lzop.ifPresent(h -> {
			h.getProfession().work(world, npc, gameTime, order);
		});
		
		WaitMode workMode = WaitMode.fromId(order.getWrappedArg(WORK_MODE_ARG_INDEX).getArgNum());
		if (workMode != WaitMode.HEARD_BELL && !brain.hasMemoryValue(MemoryModuleTypeInit.WAIT_FOR.get())) {
			if (!CommandUtils.validateWait(world, npc, workMode, NPCComplaintInit.INVALID_ORDER.get())) return;
			CommandUtils.startWait(npc, workMode, gameTime, (long) order.getWrappedArg(WORK_TIME_ARG_INDEX).getArgNum() * 20L);
		}
		CommandUtils.tickWait(world, npc, workMode, gameTime);
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		if (!CommandUtils.hasComplaint(npc)) {
			int index = brain.getMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get()).orElse(0);
			brain.setMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get(), index + 1);
		}
		brain.eraseMemory(MemoryModuleTypeInit.CACHED_POS.get());
		brain.eraseMemory(MemoryModuleTypeInit.WAIT_FOR.get());
		brain.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
		
		npc.getDataHandler().ifPresent(h -> {
			h.getProfession().stopWorking(world, npc, gameTime, order);
		});
	}

}
