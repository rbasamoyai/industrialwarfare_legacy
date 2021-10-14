package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
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

public class LeaveWorkTask extends Task<NPCEntity> {

	private final MemoryModuleType<GlobalPos> posMemoryType;
	private final float speedModifier;
	private final int closeEnoughDist;
	private final int maxDistanceFromPoi;
	private long nextOkStartTime;
	
	public LeaveWorkTask(MemoryModuleType<GlobalPos> posMemoryType, float speedModifier, int closeEnoughDist, int maxDistanceFromPoi) {
		super(ImmutableMap.of(
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.COMPLAINT, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.STOP_EXECUTION, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.WORKING, MemoryModuleStatus.VALUE_PRESENT,
				posMemoryType, MemoryModuleStatus.VALUE_PRESENT
				),
				180);
		this.posMemoryType = posMemoryType;
		this.speedModifier = speedModifier;
		this.closeEnoughDist = closeEnoughDist;
		this.maxDistanceFromPoi = maxDistanceFromPoi;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		Optional<GlobalPos> gpOptional = brain.getMemory(this.posMemoryType);
		boolean result = gpOptional.map(gp -> npc.level.dimension() == gp.dimension() && gp.pos().closerThan(npc.position(), (double) this.maxDistanceFromPoi)).orElse(false);
		if (!result) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT, !gpOptional.isPresent() ? NPCComplaintInit.INVALID_ORDER : NPCComplaintInit.TOO_FAR);
			return false;
		}
		
		ItemStack scheduleItem = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.SCHEDULE_ITEM_INDEX);
		LazyOptional<IScheduleItemDataHandler> scheduleOptional = ScheduleItem.getDataHandler(scheduleItem);
		long dayTime = world.getDayTime() + TimeUtils.TIME_OFFSET;
		int minuteOfTheWeek = (int)(dayTime % TimeUtils.WEEK_TICKS / TimeUtils.MINUTE_TICKS);
		boolean shouldWork = scheduleOptional.map(h -> h.shouldWork(minuteOfTheWeek)).orElse(false);
		boolean isWorking = brain.getMemory(MemoryModuleTypeInit.WORKING).orElse(false);
		
		return !shouldWork && isWorking;
	}
	
	@Override
	protected void start(ServerWorld world, NPCEntity npc, long gameTime) {
		if (gameTime > this.nextOkStartTime) {
			Brain<?> brain = npc.getBrain();
			Optional<GlobalPos> gpOptional = brain.getMemory(this.posMemoryType);
			gpOptional.ifPresent(gp -> CommandUtils.trySetWalkTarget(world, npc, gp.pos(), this.speedModifier, this.closeEnoughDist));
			this.nextOkStartTime = gameTime + 80L;
		}
	}
	
	@Override
	protected void tick(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		brain.getMemory(this.posMemoryType).ifPresent(gp -> {
			BlockPos pos = gp.pos();
			TileEntity te = world.getBlockEntity(pos);
			AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, -2, -1), pos.offset(2, 1, 2));
			
			boolean hasTE = te != null;
			boolean atDestination = box.contains(npc.position());
			
			if (world.isLoaded(pos) && hasTE && atDestination) {
				LazyOptional<IItemHandler> blockInvOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
				blockInvOptional.ifPresent(blockInv -> {
					EquipmentItemHandler equipmentHandler = npc.getEquipmentItemHandler();
					ItemStack taskScrollTest = equipmentHandler.extractItem(EquipmentItemHandler.TASK_ITEM_INDEX, 1, true);
					boolean inserted = false;
					for (int i = 0; i < blockInv.getSlots(); i++) {
						ItemStack result = blockInv.insertItem(i, taskScrollTest, true);
						if (result == ItemStack.EMPTY) {
							ItemStack taskScroll = equipmentHandler.extractItem(EquipmentItemHandler.TASK_ITEM_INDEX, 1, false);
							blockInv.insertItem(i, taskScroll, false);
							inserted = true;
							brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION, true);
							break;
						}
					}
					if (!inserted) {
						brain.setMemory(MemoryModuleTypeInit.COMPLAINT, NPCComplaintInit.CANT_DEPOSIT_ITEM);
					}
				});
				if (!blockInvOptional.isPresent()) {
					brain.setMemory(MemoryModuleTypeInit.COMPLAINT, NPCComplaintInit.CANT_OPEN);
				}
			} else if (!hasTE) {
				brain.setMemory(MemoryModuleTypeInit.COMPLAINT, NPCComplaintInit.NOTHING_HERE);
			} else if (!atDestination && npc.getNavigation().isDone()) {
				CommandUtils.trySetWalkTarget(world, npc, pos, this.speedModifier, this.closeEnoughDist);
			}
		});
	}	
	
	@Override
	protected boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		return !brain.hasMemoryValue(MemoryModuleTypeInit.COMPLAINT)
				&& !brain.hasMemoryValue(MemoryModuleTypeInit.STOP_EXECUTION);
	}
	
	@Override
	protected void stop(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		
		brain.setMemory(MemoryModuleTypeInit.WORKING, false);
		
		brain.eraseMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX);
		brain.eraseMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION);
		brain.eraseMemory(MemoryModuleTypeInit.STOP_EXECUTION);
	}
	
}
