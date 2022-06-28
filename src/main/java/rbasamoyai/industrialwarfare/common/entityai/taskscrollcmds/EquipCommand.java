package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCData;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class EquipCommand extends TaskScrollCommand {

	private static final int POS_ARG_INDEX = 0;
	private static final int EQUIP_ITEM_ARG_INDEX = 1;
	private static final int EQUIP_SLOT_ARG_INDEX = 2;
	private static final int ACCESS_SIDE_ARG_INDEX = 3;
		
	public EquipCommand() {
		super(CommandTrees.EQUIP, () -> ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
				));
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerLevel level, NPCEntity npc, TaskScrollOrder order) {
		EquipmentSlot type = CommandUtils.equipmentSlotTypeFromFilterFlag(order.getWrappedArg(EQUIP_SLOT_ARG_INDEX).getArgNum());
		boolean canWearEquipment = npc.getDataHandler().map(INPCData::canWearEquipment).orElse(false);
		
		if (type.getType() == EquipmentSlot.Type.ARMOR && !canWearEquipment) {
			CommandUtils.complain(npc, NPCComplaintInit.CANT_WEAR_ARMOR.get());
			return false;
		}
		
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
		
		EquipmentSlot type = CommandUtils.equipmentSlotTypeFromFilterFlag(order.getWrappedArg(EQUIP_SLOT_ARG_INDEX).getArgNum());
		
		ItemStack filter = order.getWrappedArg(EQUIP_ITEM_ARG_INDEX).getItem().orElse(ItemStack.EMPTY);
		for (int i = 0; i < blockInv.getSlots(); i++) {
			if (!CommandUtils.filterMatches(filter, blockInv.getStackInSlot(i))) continue;
			
			ItemStack takeItem = blockInv.extractItem(i, blockInv.getSlotLimit(i), false);
			int slot = EquipmentItemHandler.getTypeSlot(type);
			EquipmentItemHandler npcEquipment = npc.getEquipmentItemHandler();
			ItemStack insertItem = npcEquipment.extractItem(slot, npcEquipment.getSlotLimit(slot), false);
			
			npcEquipment.insertItem(slot, takeItem, false);
			blockInv.insertItem(i, insertItem, false);
			break;
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
