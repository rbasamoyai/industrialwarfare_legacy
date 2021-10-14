package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Optional;

import net.minecraft.entity.ai.brain.Brain;
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
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.ArgUtils;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class DepositAtCommand extends TaskScrollCommand {

	private static final int POS_ARG_INDEX = 0;
	private static final int FILTER_ARG_INDEX = 1;
	private static final int ACCESS_SIDE_ARG_INDEX = 2;
	private static final int ITEM_COUNT_ARG_INDEX = 3;
	
	public DepositAtCommand() {
		super(CommandTrees.ITEM_HANDLING);
		this.setRegistryName(IndustrialWarfare.MOD_ID, "deposit_at");
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		Optional<BlockPos> pos = order.getWrappedArg(POS_ARG_INDEX).getPos();
		if (!pos.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT, NPCComplaintInit.INVALID_ORDER);
			return false;
		}
		
		boolean result = pos.get().closerThan(npc.position(), TaskScrollCommand.MAX_DISTANCE_FROM_POI);
		if (!result) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT, NPCComplaintInit.TOO_FAR);
		}
		return result;
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		CommandUtils.trySetWalkTarget(world, npc, order.getWrappedArg(POS_ARG_INDEX).getPos().get(), TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST);
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO);
		TileEntity te = world.getBlockEntity(pos);
		AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, -2, -1), pos.offset(2, 1, 2));
		
		boolean hasTE = te != null;
		boolean atDestination = box.contains(npc.position());
		
		if (world.isLoaded(pos) && hasTE && atDestination) {
			Direction side = Direction.from3DDataValue(order.getWrappedArg(ACCESS_SIDE_ARG_INDEX).getArgNum());
			LazyOptional<IItemHandler> blockInvOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			
			blockInvOptional.ifPresent(blockInv -> {
				int count = order.getWrappedArg(ITEM_COUNT_ARG_INDEX).getArgNum();
				boolean flag = count == 0;
				ItemStack filter = order.getWrappedArg(FILTER_ARG_INDEX).getItem().orElse(ItemStack.EMPTY);
				
				ItemStackHandler npcInv = npc.getInventoryItemHandler();
				for (int i = 0; i < npcInv.getSlots(); i++) {
					if (ArgUtils.filterMatches(filter, npcInv.getStackInSlot(i))) {
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
				}
				brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION, true);
			});
			if (!blockInvOptional.isPresent()) {
				brain.setMemory(MemoryModuleTypeInit.COMPLAINT, NPCComplaintInit.CANT_OPEN);
			}
		} else if (!hasTE) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT, NPCComplaintInit.NOTHING_HERE);
		} else if (!atDestination && npc.getNavigation().isDone()) {
			CommandUtils.trySetWalkTarget(world, npc, pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST);
		}
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.COMPLAINT)) {
			brain.setMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX, brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX).orElse(0) + 1);
		}
	}

}
