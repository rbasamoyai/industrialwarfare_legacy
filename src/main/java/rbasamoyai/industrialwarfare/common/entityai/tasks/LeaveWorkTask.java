package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem.IScheduleItemData;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.items.ScheduleItem;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;
import rbasamoyai.industrialwarfare.utils.TimeUtils;

/**
 * Pretty much a copy of {@link GoToWorkTask}
 * 
 * @author rbasamoyai
 */

public class LeaveWorkTask extends Behavior<NPCEntity> {

	private final MemoryModuleType<GlobalPos> posMemoryType;
	private final float speedModifier;
	private final int closeEnoughDist;
	private final int maxDistanceFromPoi;
	private long nextOkStartTime;
	
	public LeaveWorkTask(MemoryModuleType<GlobalPos> posMemoryType, float speedModifier, int closeEnoughDist, int maxDistanceFromPoi) {
		super(ImmutableMap.<MemoryModuleType<?>, MemoryStatus>builder()
				.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED)
				.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED)
				.put(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), MemoryStatus.VALUE_PRESENT)
				.put(MemoryModuleTypeInit.COMPLAINT.get(), MemoryStatus.REGISTERED)
				.put(MemoryModuleTypeInit.STOP_EXECUTION.get(), MemoryStatus.REGISTERED)
				.put(posMemoryType, MemoryStatus.VALUE_PRESENT)
				.build(),
				180);
		this.posMemoryType = posMemoryType;
		this.speedModifier = speedModifier;
		this.closeEnoughDist = closeEnoughDist;
		this.maxDistanceFromPoi = maxDistanceFromPoi;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel world, NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		Optional<GlobalPos> gpOptional = brain.getMemory(this.posMemoryType);
		if (!gpOptional.isPresent()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get(), 200L);
			return false;
		}
		GlobalPos gp = gpOptional.get();
		
		if (gp.dimension() != world.dimension()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get(), 200L);
			return false;
		}
		if (!gp.pos().closerToCenterThan(npc.position(), (double) this.maxDistanceFromPoi)) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TOO_FAR.get(), 200L);
			return false;
		}

		if (brain.getMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get()).get() != ActivityStatus.WORKING) {
			return false;
		}
		if (brain.getMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get()).orElse(false)) return false;		
		
		ItemStack scheduleItem = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.SCHEDULE_ITEM_INDEX);
		LazyOptional<IScheduleItemData> lzop = ScheduleItem.getDataHandler(scheduleItem);
		if (!lzop.isPresent()) return true;
		IScheduleItemData handler = lzop.resolve().get();
		
		int minute = TimeUtils.getMinuteOfTheWeek(world);
		return !handler.shouldWork(minute);
	}
	
	@Override
	protected void start(ServerLevel world, NPCEntity npc, long gameTime) {
		if (gameTime > this.nextOkStartTime) {
			Brain<?> brain = npc.getBrain();
			Optional<GlobalPos> gpOptional = brain.getMemory(this.posMemoryType);
			gpOptional.ifPresent(gp -> CommandUtils.trySetInterfaceWalkTarget(world, npc, gp.pos(), this.speedModifier, this.closeEnoughDist));
			this.nextOkStartTime = gameTime + 80L;
		}
	}
	
	@Override
	protected void tick(ServerLevel world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		Optional<GlobalPos> gp = brain.getMemory(this.posMemoryType);
		if (!gp.isPresent()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get(), 200L);
			return;
		}
		BlockPos pos = gp.get().pos();
		AABB box = new AABB(pos.offset(-1, -2, -1), pos.offset(2, 1, 2));
		if (!box.contains(npc.position())) {
			if (npc.getNavigation().isDone()) {
				CommandUtils.trySetInterfaceWalkTarget(world, npc, pos, this.speedModifier, this.closeEnoughDist);
			}
			return;
		}
		
		BlockEntity be = world.getBlockEntity(pos);
		if (be == null) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NOTHING_HERE.get(), 200L);
			return;
		}
		
		LazyOptional<IItemHandler> lzop = be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!lzop.isPresent()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_OPEN.get(), 200L);
			return;
		}
		IItemHandler blockInv = lzop.resolve().get();
		
		EquipmentItemHandler equipmentHandler = npc.getEquipmentItemHandler();
		ItemStack taskScrollTest = equipmentHandler.extractItem(EquipmentItemHandler.TASK_ITEM_INDEX, 1, true);
		for (int i = 0; i < blockInv.getSlots(); i++) {
			ItemStack result = blockInv.insertItem(i, taskScrollTest, true);
			if (result != ItemStack.EMPTY) continue;
			
			ItemStack taskScroll = equipmentHandler.extractItem(EquipmentItemHandler.TASK_ITEM_INDEX, 1, false);
			blockInv.insertItem(i, taskScroll, false);
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
			be.setChanged();
			return;
		}
		brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_DEPOSIT_ITEM.get(), 200L);
	}	
	
	@Override
	protected boolean canStillUse(ServerLevel world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		return !brain.hasMemoryValue(MemoryModuleTypeInit.COMPLAINT.get()) && !brain.hasMemoryValue(MemoryModuleTypeInit.STOP_EXECUTION.get());
	}
	
	@Override
	protected void stop(ServerLevel world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		
		brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), ActivityStatus.NO_ACTIVITY);
		
		brain.eraseMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get());
		brain.eraseMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get());
		brain.eraseMemory(MemoryModuleTypeInit.STOP_EXECUTION.get());
		
		brain.setActiveActivityIfPossible(Activity.IDLE);
	}
	
}
