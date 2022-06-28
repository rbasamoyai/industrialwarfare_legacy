package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class DepositAtCommand extends TaskScrollCommand {

	private static final int POS_ARG_INDEX = 0;
	private static final int FILTER_ARG_INDEX = 1;
	private static final int ACCESS_SIDE_ARG_INDEX = 2;
	private static final int ITEM_COUNT_ARG_INDEX = 3;
	
	public DepositAtCommand() {
		super(CommandTrees.ITEM_HANDLING, () -> ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
				));
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerLevel level, NPCEntity npc, TaskScrollOrder order) {
		return CommandUtils.validatePos(level, npc, order.getWrappedArg(POS_ARG_INDEX).getPos(), TaskScrollCommand.MAX_DISTANCE_FROM_POI, NPCComplaintInit.INVALID_ORDER.get());
	}

	@Override
	public void start(ServerLevel level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		CommandUtils.trySetInterfaceWalkTarget(level, npc, order.getWrappedArg(POS_ARG_INDEX).getPos().get(), TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST);
	}

	@Override
	public void tick(ServerLevel level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO);
		AABB box = new AABB(pos.offset(-1, -2, -1), pos.offset(2, 1, 2));
		
		if (!box.contains(npc.position())) {
			if (npc.getNavigation().isDone()) {
				CommandUtils.trySetInterfaceWalkTarget(level, npc, pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST);
			}
			return;
		}
		
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NOTHING_HERE.get(), 200L);
			return;
		}
		
		Direction side = Direction.from3DDataValue(order.getWrappedArg(ACCESS_SIDE_ARG_INDEX).getArgNum());
		LazyOptional<IItemHandler> lzop = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		if (!lzop.isPresent()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_OPEN.get(), 200L);
			return;
		}
		IItemHandler blockInv = lzop.resolve().get();
		
		int count = order.getWrappedArg(ITEM_COUNT_ARG_INDEX).getArgNum();
		boolean flag = count == 0;
		ItemStack filter = order.getWrappedArg(FILTER_ARG_INDEX).getItem().orElse(ItemStack.EMPTY);
		
		ItemStackHandler npcInv = npc.getInventoryItemHandler();
		for (int i = 0; i < npcInv.getSlots(); i++) {
			if (!CommandUtils.filterMatches(filter, npcInv.getStackInSlot(i))) continue;
			
			ItemStack depositItem = npcInv.extractItem(i, flag ? npcInv.getSlotLimit(i) : count, false);
			for (int j = 0; j < blockInv.getSlots(); j++) {
				int stackCount = depositItem.getCount();
				depositItem = blockInv.insertItem(j, depositItem, false);
				count -= stackCount - depositItem.getCount();
				if (depositItem.isEmpty() || count < 1 && !flag) break;
			}
			npcInv.insertItem(i, depositItem, true);
			if (count < 1 && !flag) break;
		}
		brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
	}

	@Override
	public void stop(ServerLevel level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		if (!CommandUtils.hasComplaint(npc)) {
			CommandUtils.incrementCurrentInstructionIndexMemory(npc);
		}
	}

}
