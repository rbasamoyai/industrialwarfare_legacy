package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.IScheduleItemDataHandler;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.LabelItem;
import rbasamoyai.industrialwarfare.common.items.ScheduleItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;
import rbasamoyai.industrialwarfare.utils.TimeUtils;

/**
 * Notes taken from {@link net.minecraft.entity.ai.brain.task.WalkTowardsPosTask} I guess
 * 
 * @author rbasamoyai
 */

public class GoToWorkTask extends Task<NPCEntity> {

	private final MemoryModuleType<GlobalPos> posMemoryType;
	private final float speedModifier;
	private final int closeEnoughDist;
	private final int maxDistanceFromPoi;
	private long nextOkStartTime;
	
	public GoToWorkTask(MemoryModuleType<GlobalPos> posMemoryType, float speedModifier, int closeEnoughDist, int maxDistanceFromPoi) {
		super(ImmutableMap.<MemoryModuleType<?>, MemoryModuleStatus>builder()
				.put(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.COMPLAINT.get(), MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX.get(), MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.STOP_EXECUTION.get(), MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.WORKING.get(), MemoryModuleStatus.REGISTERED)
				.put(posMemoryType, MemoryModuleStatus.VALUE_PRESENT)
				.build(),
				180);
		this.posMemoryType = posMemoryType;
		this.speedModifier = speedModifier;
		this.closeEnoughDist = closeEnoughDist;
		this.maxDistanceFromPoi = maxDistanceFromPoi;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc) {
		// TODO: Add wanted scroll details
		ItemStack scheduleItem = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.SCHEDULE_ITEM_INDEX);
		LazyOptional<IScheduleItemDataHandler> scheduleOptional = ScheduleItem.getDataHandler(scheduleItem);
		if (!scheduleOptional.isPresent()) {
			return false;
		}
		IScheduleItemDataHandler scheduleHandler = scheduleOptional.resolve().get();
		
		int minute = TimeUtils.getMinuteOfTheWeek(world);
		if (!scheduleHandler.shouldWork(minute + 2)) return false;
		
		Brain<?> brain = npc.getBrain();
		if (brain.getMemory(MemoryModuleTypeInit.WORKING.get()).orElse(false)) return false;
		
		Optional<GlobalPos> gpOptional = brain.getMemory(this.posMemoryType);
		if (!gpOptional.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get());
			return false;
		}
		GlobalPos gp = gpOptional.get();
		
		if (gp.dimension() != world.dimension()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get());
			return false;
		}
		if (gp.pos().closerThan(npc.position(), (double) this.maxDistanceFromPoi)) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TOO_FAR.get());
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void start(ServerWorld world, NPCEntity npc, long gameTime) {
		if (gameTime > this.nextOkStartTime) {
			Brain<?> brain = npc.getBrain();
			Optional<GlobalPos> optional = brain.getMemory(this.posMemoryType);
			optional.ifPresent(gp -> CommandUtils.trySetWalkTarget(world, npc, gp.pos(), this.speedModifier, this.closeEnoughDist));
			this.nextOkStartTime = gameTime + 80L;
		}
	}
	
	@Override
	protected void tick(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		Optional<GlobalPos> gp = brain.getMemory(this.posMemoryType);
		if (!gp.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get());
			return;
		}
			
		BlockPos pos = gp.get().pos();
		AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, -2, -1), pos.offset(2, 1, 2));
		
		if (!box.contains(npc.position())) {
			if (!npc.getNavigation().isDone()) {
				CommandUtils.trySetWalkTarget(world, npc, pos, this.speedModifier, this.closeEnoughDist);
			}
			return;
		}
		
		TileEntity te = world.getBlockEntity(pos);
		if (te == null) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NOTHING_HERE.get());
		}
		
		if (!world.isLoaded(pos)) return;
		
		LazyOptional<IItemHandler> blockInvOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		// Me omw to have a minor pyramid of doom, not too deep but i think we can do better
		if (!blockInvOptional.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_OPEN.get());
			return;
		}
		IItemHandler blockInv = blockInvOptional.resolve().get();
		boolean matches = false;
		for (int i = 0; i < blockInv.getSlots(); i++) {
			ItemStack taskScrollItemGet = blockInv.getStackInSlot(i);
			matches = TaskScrollItem.getDataHandler(taskScrollItemGet)
					.map(ts -> LabelItem.getDataHandler(ts.getLabel())
							.map(l -> l.getUUID().equals(npc.getUUID()))
							.orElse(false))
					.orElse(false);
			if (matches) {
				EquipmentItemHandler equipmentHandler = npc.getEquipmentItemHandler();
				ItemStack npcSlotItem = equipmentHandler.extractItem(EquipmentItemHandler.TASK_ITEM_INDEX, 1, false);
				ItemStack taskScrollItem = blockInv.extractItem(i, 1, false);
				equipmentHandler.insertItem(EquipmentItemHandler.TASK_ITEM_INDEX, taskScrollItem, false);
				blockInv.insertItem(i, npcSlotItem, false);
				te.setChanged();
				brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
				return;
			}
		}
		if (!matches) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_GET_ITEM.get());
		}
	}
	
	@Override
	protected boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		boolean hasComplaint = brain.hasMemoryValue(MemoryModuleTypeInit.COMPLAINT.get());
		boolean stopExecution = brain.hasMemoryValue(MemoryModuleTypeInit.STOP_EXECUTION.get());
		return !hasComplaint && !stopExecution;
	}
	
	@Override
	protected void stop(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.COMPLAINT.get())) {
			brain.setMemory(MemoryModuleTypeInit.WORKING.get(), true);
			brain.setMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX.get(), 0);
			
			brain.setActiveActivityIfPossible(Activity.WORK);
		}
		brain.eraseMemory(MemoryModuleTypeInit.STOP_EXECUTION.get());
	}
	
}
