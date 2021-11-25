package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
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
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NOTHING_HERE.get());
			return;
		}
		
		Direction side = Direction.from3DDataValue(order.getWrappedArg(ACCESS_SIDE_ARG_INDEX).getArgNum());
		LazyOptional<IItemHandler> lzop = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		if (!lzop.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_OPEN.get());
			return;
		}
		IItemHandler blockInv = lzop.resolve().get();
		
		EquipmentItemHandler npcEquipment = npc.getEquipmentItemHandler();
		EquipmentSlotType type = CommandUtils.equipmentSlotTypeFromFilterFlag(order.getWrappedArg(EQUIP_SLOT_ARG_INDEX).getArgNum());
		int slot = EquipmentItemHandler.getTypeSlot(type);
		ItemStack unequipItem = npcEquipment.extractItem(slot, npcEquipment.getSlotLimit(slot), false);
		
		for (int i = 0; i < blockInv.getSlots(); i++) {
			unequipItem = blockInv.insertItem(i, unequipItem, false);
			if (unequipItem.isEmpty()) break;
		}
		if (!unequipItem.isEmpty()) {
			InventoryHelper.dropItemStack(world, npc.getX(), npc.getY(), npc.getZ(), unequipItem);
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
