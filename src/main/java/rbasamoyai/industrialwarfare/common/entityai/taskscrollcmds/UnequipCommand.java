package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
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
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class UnequipCommand extends TaskScrollCommand {

	private static final int POS_ARG_INDEX = 0;
	private static final int EQUIP_SLOT_ARG_INDEX = 1;
	private static final int ACCESS_SIDE_ARG_INDEX = 2;

	public UnequipCommand() {
		super(CommandTrees.UNEQUIP, () -> ImmutableMap.of(
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
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NOTHING_HERE.get());
			return;
		}
		
		Direction side = Direction.from3DDataValue(order.getWrappedArg(ACCESS_SIDE_ARG_INDEX).getArgNum());
		LazyOptional<IItemHandler> lzop = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		if (!lzop.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_OPEN.get());
			return;
		}
		IItemHandler blockInv = lzop.resolve().get();
		
		EquipmentItemHandler npcEquipment = npc.getEquipmentItemHandler();
		EquipmentSlot type = CommandUtils.equipmentSlotTypeFromFilterFlag(order.getWrappedArg(EQUIP_SLOT_ARG_INDEX).getArgNum());
		int slot = EquipmentItemHandler.getTypeSlot(type);
		ItemStack unequipItem = npcEquipment.extractItem(slot, npcEquipment.getSlotLimit(slot), false);
		
		for (int i = 0; i < blockInv.getSlots(); i++) {
			unequipItem = blockInv.insertItem(i, unequipItem, false);
			if (unequipItem.isEmpty()) break;
		}
		if (!unequipItem.isEmpty()) {
			Containers.dropItemStack(level, npc.getX(), npc.getY(), npc.getZ(), unequipItem);
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
