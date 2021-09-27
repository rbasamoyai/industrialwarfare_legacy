package rbasamoyai.industrialwarfare.common.taskscrollcmds;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

public class IWBaseTaskScrollMethods {
	
	public static boolean moveTo(NPCEntity npc, TaskScrollOrder order) {
		npc.getBrain().setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(npc.level.dimension(), order.getPos()));
		return true;
	}
	
	public static boolean takeFrom(NPCEntity npc, TaskScrollOrder order) {
		// Maybe TODO: make npc complain about certain problems along the way
		if (!checkForTargetTileEntity(npc, order.getPos())) return false;
		
		TileEntity te = npc.level.getBlockEntity(order.getPos());
		LazyOptional<IItemHandler> optional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, directionOf(order.getArgs().get(0)));
		if (optional.isPresent()) {
			optional.ifPresent(h -> takeItems(h, npc, order.getFilter()));
			return true;
		} else {
			
			return false;
		}	
	}
	
	public static boolean depositTo(NPCEntity npc, TaskScrollOrder order) {
		// Maybe TODO: make npc complain about certain problems along the way
		if (!checkForTargetTileEntity(npc, order.getPos())) return false;
		
		TileEntity te = npc.level.getBlockEntity(order.getPos());
		LazyOptional<IItemHandler> optional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, directionOf(order.getArgs().get(0)));
		if (optional.isPresent()) {
			optional.ifPresent(h -> depositItems(h, npc, order.getFilter()));
			return true;
		} else {
			
			return false;
		}
	}
	
	private static boolean checkForTargetTileEntity(NPCEntity npc, BlockPos pos) {
		if (npc.level.getBlockEntity(pos) == null) {
			npc.getBrain().setActiveActivityIfPossible(Activity.IDLE);
			// Maybe TODO: make NPC complain
			return false;
		}
		
		int x = MathHelper.floor(npc.getX()) - 1;
		int y = MathHelper.floor(npc.getY());
		int z = MathHelper.floor(npc.getZ()) - 1;
		AxisAlignedBB box = new AxisAlignedBB(new BlockPos(x, y, z), new BlockPos(x + 3, y + 3, z + 3));
		
		if (!box.contains((double) pos.getX(), (double) pos.getY(), (double) pos.getZ())) {
			npc.getBrain().setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(npc.level.dimension(), pos));
			return false;
		}
		return true;
	}
	
	private static void takeItems(IItemHandler handler, NPCEntity npc, ItemStack filter) {
		ItemStackHandler npcInventoryHandler = npc.getInventoryItemHandler();
		Set<ItemStack> items = ImmutableSet.of();
		if (!ItemStack.matches(filter, ItemStack.EMPTY)) {
			// TODO: Implement a filter item
			ItemStack filterCopy = filter.copy();
			filterCopy.setCount(1);
			items = ImmutableSet.of(filterCopy);
		}
		
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack currentSlotTestStack = handler.getStackInSlot(i).copy();
			currentSlotTestStack.setCount(1);
			if (items.isEmpty() || items.contains(currentSlotTestStack)) {
				ItemStack stack = handler.extractItem(i, handler.getSlotLimit(i), false);
				
				for (int j = 0; j < npcInventoryHandler.getSlots(); j++) {
					stack = npcInventoryHandler.insertItem(i, stack, false);
				}
				
				handler.insertItem(i, stack, false); // Anything not taken we put back
			}
		}
	}
	
	private static void depositItems(IItemHandler handler, NPCEntity npc, ItemStack filter) {
		// Pretty much a direct copy of takeItems
		ItemStackHandler npcInventoryHandler = npc.getInventoryItemHandler();
		Set<ItemStack> items = ImmutableSet.of();
		if (!ItemStack.matches(filter, ItemStack.EMPTY)) {
			// TODO: Implement a filter item
			ItemStack filterCopy = filter.copy();
			filterCopy.setCount(1);
			items = ImmutableSet.of(filterCopy);
		}
		
		for (int i = 0; i < npcInventoryHandler.getSlots(); i++) {
			ItemStack currentSlotTestStack = npcInventoryHandler.getStackInSlot(i).copy();
			currentSlotTestStack.setCount(1);
			if (items.isEmpty() || items.contains(currentSlotTestStack)) {
				ItemStack stack = npcInventoryHandler.extractItem(i, npcInventoryHandler.getSlotLimit(i), false);
				
				for (int j = 0; j < handler.getSlots(); j++) {
					stack = handler.insertItem(i, stack, false);
				}
				
				npcInventoryHandler.insertItem(i, stack, false); // Anything not taken we put back
			}
		}
	}
	
	private static Direction directionOf(byte b) {
		return Direction.values()[b];
	}
	
}
