package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.ITaskScrollDataHandler;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.utils.ArgUtils;

public class JumpToCommand extends TaskScrollCommand {

	private static final int JUMP_POS_ARG_INDEX = 0;
	private static final int CONDITION_TYPE_INDEX = 1;
	
	public JumpToCommand() {
		super(CommandTrees.JUMP_TO);
		this.setRegistryName(IndustrialWarfare.MOD_ID, "jump_to");
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		int jumpPos = order.getWrappedArg(JUMP_POS_ARG_INDEX).getArgNum();
		ItemStack scroll = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.TASK_ITEM_INDEX);
		LazyOptional<ITaskScrollDataHandler> optional = TaskScrollItem.getDataHandler(scroll);
		
		if (!optional.map(h -> 0 <= jumpPos || jumpPos < h.getList().size()).orElse(false)) {
			// TODO: Complain that position is not valid
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		int condition = order.getWrappedArg(CONDITION_TYPE_INDEX).getArgNum();
		
		int dayTime = (int)(world.getDayTime() % 24000L);
		int dayTimeCondition = order.getWrappedArg(2).getArgNum();
		
		ItemStack filter = order.getWrappedArg(2).getItem().orElse(ItemStack.EMPTY);
		int itemCount = 0;
		boolean hasItems = false;
		
		ItemStack mainhand = npc.getItemInHand(Hand.MAIN_HAND);
		ItemStack offhand = npc.getItemInHand(Hand.OFF_HAND);
		if (ArgUtils.filterMatches(filter, mainhand)) {
			itemCount += mainhand.getCount();
			hasItems = true;
		}
		if (ArgUtils.filterMatches(filter, offhand)) {
			itemCount += offhand.getCount();
			hasItems = true;
		}
		
		if (!hasItems) {
			ItemStackHandler npcInv = npc.getInventoryItemHandler();
			for (int i = 0; i < npcInv.getSlots(); i++) {
				ItemStack stack = npcInv.getStackInSlot(i);
				if (ArgUtils.filterMatches(filter, stack)) {
					hasItems = true;
					itemCount += stack.getCount();
				}
			}
		}
		
		int itemCondition = order.getWrappedArg(3).getArgNum();
		int itemCountCondition = order.getWrappedArg(4).getArgNum();
		
		boolean heardBell = brain.hasMemoryValue(MemoryModuleType.HEARD_BELL_TIME);
		
		if (condition == BaseCondition.UNCONDITIONAL
				|| condition == BaseCondition.DAY_TIME &&
						(dayTimeCondition == DayTimeCondition.BEFORE && dayTime < dayTimeCondition
						|| dayTimeCondition == DayTimeCondition.AFTER && dayTime >= dayTimeCondition)
				|| condition == BaseCondition.HAS_ITEMS && hasItems &&
						(itemCondition == ItemCondition.UNCONDITIONAL
						|| itemCondition == ItemCondition.MORE_THAN && itemCount > itemCountCondition
						|| itemCondition == ItemCondition.LESS_THAN && itemCount < itemCountCondition
						|| itemCondition == ItemCondition.EQUAL_TO && itemCount == itemCountCondition)
				|| condition == BaseCondition.HEARD_BELL && heardBell) {
			brain.setMemory(MemoryModuleTypeInit.JUMP_TO, order.getWrappedArg(JUMP_POS_ARG_INDEX).getArgNum());
		} else {
			brain.setMemory(MemoryModuleTypeInit.JUMP_TO, brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX).orElse(0) + 1);
		}
		brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION, true);
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();

		brain.setMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX, brain.getMemory(MemoryModuleTypeInit.JUMP_TO).orElse(0));
		
		brain.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
		brain.eraseMemory(MemoryModuleTypeInit.JUMP_TO);
		IndustrialWarfare.LOGGER.debug(this.getRegistryName().toString() + " finished, index is now " + brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX).orElse(0));
	}
	
	public static class BaseCondition {
		public static final int UNCONDITIONAL = 0; 
		public static final int DAY_TIME = 1;
		public static final int HAS_ITEMS = 2;
		public static final int HEARD_BELL = 3;
		
		public static final int[] VALUES = new int[] {UNCONDITIONAL, DAY_TIME, HAS_ITEMS, HEARD_BELL};
	}
	
	public static class DayTimeCondition {
		public static final int BEFORE = 0;
		public static final int AFTER = 1;
		
		public static final int[] VALUES = new int[] {BEFORE, AFTER};
	}
	
	public static class ItemCondition {
		public static final int UNCONDITIONAL = 0;
		public static final int MORE_THAN = 1;
		public static final int LESS_THAN = 2;
		public static final int EQUAL_TO = 3;
		
		public static final int[] VALUES = new int[] {UNCONDITIONAL, MORE_THAN, LESS_THAN, EQUAL_TO};
	}

}