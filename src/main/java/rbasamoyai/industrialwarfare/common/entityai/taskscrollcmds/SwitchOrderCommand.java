package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Optional;
import java.util.UUID;

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
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.label.ILabelItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.ITaskScrollDataHandler;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.LabelItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class SwitchOrderCommand extends TaskScrollCommand {

	private static final int LOOK_FOR_NUMBER = 0;
	private static final int LOOK_FOR_NAME = 1;
	private static final int ACCESS_SIDE_ARG_INDEX = 2;
	private static final int POS_MODE_ARG_INDEX = 3;
	private static final int POS_ARG_INDEX = 4;
	
	public SwitchOrderCommand() {
		super(CommandTrees.SWITCH_ORDER, () -> ImmutableMap.of(
				MemoryModuleType.JOB_SITE, MemoryModuleStatus.VALUE_PRESENT,
				MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED
				));
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		int mode = order.getWrappedArg(POS_MODE_ARG_INDEX).getArgNum();
		
		BlockPos pos;
		if (mode == PosModes.GET_FROM_POS) {
			Optional<BlockPos> optional = order.getWrappedArg(POS_ARG_INDEX).getPos();
			if (!optional.isPresent()) {
				brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_ORDER.get());
				return false;
			}
			pos = optional.get();
		} else {
			pos = brain.getMemory(MemoryModuleType.JOB_SITE).get().pos();
		}
		if (!pos.closerThan(npc.position(), TaskScrollCommand.MAX_DISTANCE_FROM_POI)) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TOO_FAR.get());
			return false;
		}
		return true;
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		BlockPos targetPos;
		if (order.getWrappedArg(POS_MODE_ARG_INDEX).getArgNum() == PosModes.GET_FROM_JOB_SITE) {
			targetPos = npc.getBrain().getMemory(MemoryModuleType.JOB_SITE).get().pos();
		} else {
			targetPos = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		}
		CommandUtils.trySetInterfaceWalkTarget(world, npc, targetPos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST);
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		BlockPos pos = order.getWrappedArg(POS_MODE_ARG_INDEX).getArgNum() == PosModes.GET_FROM_JOB_SITE
				? brain.getMemory(MemoryModuleType.JOB_SITE).get().pos()
				: order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, -2, -1), pos.offset(2, 1, 2));
		
		if (!box.contains(npc.position())) {
			if (npc.getNavigation().isDone()) {
				CommandUtils.trySetInterfaceWalkTarget(world, npc, pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST);
			}
			return;
		}
		
		TileEntity te = world.getBlockEntity(pos);
		if (te == null) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NOTHING_HERE.get());
			return;
		}
		
		Direction side = Direction.from3DDataValue(order.getWrappedArg(ACCESS_SIDE_ARG_INDEX).getArgNum());
		LazyOptional<IItemHandler> teLzop = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		if (!teLzop.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_OPEN.get());
			return;
		}
		IItemHandler blockInv = teLzop.resolve().get();
		
		int lookForNum = order.getWrappedArg(LOOK_FOR_NUMBER).getArgNum();
		boolean anyNumber = lookForNum == -1;
		boolean lookForName = order.getWrappedArg(LOOK_FOR_NAME).getArgNum() == LookNameModes.LOOK_FOR_NAME;
		UUID npcUUID = npc.getUUID();
		
		for (int i = 0; i < blockInv.getSlots(); i++) {
			ItemStack scroll = blockInv.getStackInSlot(i);
			ItemStack label = TaskScrollItem.getDataHandler(scroll).map(ITaskScrollDataHandler::getLabel).orElse(ItemStack.EMPTY);
			LazyOptional<ILabelItemDataHandler> labelLzop = LabelItem.getDataHandler(label);
			
			if (labelLzop.isPresent()) {
				ILabelItemDataHandler labelData = labelLzop.resolve().get();
				if (!anyNumber && labelData.getNumber() != lookForNum) continue;
				if (lookForName && !labelData.getUUID().equals(npcUUID)) continue;
			}
			
			EquipmentItemHandler npcInv = npc.getEquipmentItemHandler();
			ItemStack takeScroll = blockInv.extractItem(i, 1, false);
			ItemStack depositScroll = npcInv.extractItem(EquipmentItemHandler.TASK_ITEM_INDEX, 1, false);
			blockInv.insertItem(i, depositScroll, false);
			npcInv.insertItem(EquipmentItemHandler.TASK_ITEM_INDEX, takeScroll, false);
			
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
			return;
		}
		brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_GET_ITEM.get());
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		if (!CommandUtils.hasComplaint(npc)) {
			brain.setMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get(), 0);
		}
	}
	
	public static class LookNameModes {
		public static final int DONT_LOOK_FOR_NAME = 0;
		public static final int LOOK_FOR_NAME = 1;
		
		public static final int[] VALUES = {DONT_LOOK_FOR_NAME, LOOK_FOR_NAME};
	}

	public static class PosModes {
		public static final int GET_FROM_JOB_SITE = 0;
		public static final int GET_FROM_POS = 1;
		
		public static final int[] VALUES = {GET_FROM_JOB_SITE, GET_FROM_POS};
	}
	
}
