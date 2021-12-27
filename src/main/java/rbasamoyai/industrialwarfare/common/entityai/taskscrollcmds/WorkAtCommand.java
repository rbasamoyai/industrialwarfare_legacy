package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
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
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;
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
			npc.getBrain().setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NO_DATA_HANDLER.get());
			return false;
		}
		if (lzop.resolve().get().getProfession().checkMemories(npc)) {
			npc.getBrain().setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_ORDER.get());
			return false;
		}
		
		Optional<BlockPos> optional = order.getWrappedArg(POS_ARG_INDEX).getPos();
		if (!optional.isPresent()) {
			npc.getBrain().setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_ORDER.get());
			return false;
		}
		BlockPos pos = optional.get();
		if (!pos.closerThan(npc.position(), TaskScrollCommand.MAX_DISTANCE_FROM_POI)) {
			npc.getBrain().setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TOO_FAR.get());
			return false;
		}
		
		return CommandUtils.validateWait(world, npc, WaitMode.fromId(order.getWrappedArg(POS_ARG_INDEX).getArgNum()), NPCComplaintInit.INVALID_ORDER.get());
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		LazyOptional<INPCDataHandler> lzop = npc.getDataHandler();
		if (!lzop.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NO_DATA_HANDLER.get());
			return;
		}
		INPCDataHandler handler = lzop.resolve().get();
		
		NPCProfession profession = handler.getProfession();
		BlockPos target = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		Optional<BlockPos> posOptional = profession.getWorkingArea(world, target, npc);
		if (!posOptional.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get());
			return;
		}
		
		BlockPos pos = posOptional.get();
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
		brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(world.dimension(), pos));
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2));
		
		if (!box.contains(npc.position())) {
			if (npc.getNavigation().isDone()) {
				BlockPos cachedPos = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get()).map(GlobalPos::pos).orElse(pos.below());
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(cachedPos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
			}
			return;
		}
		
		LazyOptional<INPCDataHandler> lzop = npc.getDataHandler();
		if (!lzop.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NO_DATA_HANDLER.get());
			return;
		}
		lzop.resolve().get().getProfession().work(world, npc, gameTime, order);
		
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
		
		TileEntity te = world.getBlockEntity(order.getWrappedArg(POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO));
		if (te == null) return;
		if (!(te instanceof WorkstationTileEntity)) return;
		((WorkstationTileEntity) te).setRecipe(ItemStack.EMPTY, false);
	}

}
