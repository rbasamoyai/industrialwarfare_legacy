package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
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

public class TakeFromCommand extends TaskScrollCommand {

	private static final int POS_ARG_INDEX = 0;
	private static final int FILTER_ARG_INDEX = 1;
	private static final int ACCESS_SIDE_ARG_INDEX = 2;
	private static final int ITEM_COUNT_ARG_INDEX = 3;
	
	public TakeFromCommand() {
		super(CommandTrees.ITEM_HANDLING, () -> ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED
				));
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		return CommandUtils.validatePos(world, npc, order.getWrappedArg(POS_ARG_INDEX).getPos(), TaskScrollCommand.MAX_DISTANCE_FROM_POI, NPCComplaintInit.INVALID_ORDER.get());
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		CommandUtils.trySetInterfaceWalkTarget(world, npc, order.getWrappedArg(POS_ARG_INDEX).getPos().get(), TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST);
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO);
		AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, -2, -1), pos.offset(2, 1, 2));
		
		if (!box.contains(npc.position())) {
			if (npc.getNavigation().isDone()) {
				CommandUtils.trySetInterfaceWalkTarget(world, npc, pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST);
			}
			return;
		}
		
		TileEntity te = world.getBlockEntity(pos);
		if (te == null) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NOTHING_HERE.get(), 200L);
			return;
		}
		
		Direction side = Direction.from3DDataValue(order.getWrappedArg(ACCESS_SIDE_ARG_INDEX).getArgNum());
		LazyOptional<IItemHandler> lzop = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		if (!lzop.isPresent()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_OPEN.get(), 200L);
			return;
		}
		IItemHandler blockInv = lzop.resolve().get();
		
		int count = order.getWrappedArg(ITEM_COUNT_ARG_INDEX).getArgNum();
		boolean flag = count == 0;
		ItemStack filter = order.getWrappedArg(FILTER_ARG_INDEX).getItem().orElse(ItemStack.EMPTY);
		
		ItemStackHandler npcInv = npc.getInventoryItemHandler();
		for (int i = 0; i < blockInv.getSlots(); i++) {
			if (!CommandUtils.filterMatches(filter, blockInv.getStackInSlot(i))) continue;
			
			ItemStack takeItem = blockInv.extractItem(i, flag ? blockInv.getSlotLimit(i) : count, false);
			for (int j = 0; j < npcInv.getSlots(); j++) {
				int stackCount = takeItem.getCount();
				takeItem = npcInv.insertItem(j, takeItem, false);
				count -= stackCount - takeItem.getCount();
				if (takeItem.isEmpty() || count < 1 && !flag) break;
			}
			blockInv.insertItem(i, takeItem, false);
			if (count < 1 && !flag) break;
		}
		brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		if (!CommandUtils.hasComplaint(npc)) {
			CommandUtils.incrementCurrentInstructionIndexMemory(npc);
		}
	}

}
