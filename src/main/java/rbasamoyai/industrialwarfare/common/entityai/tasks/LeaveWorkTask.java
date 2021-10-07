package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigator;
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
				MemoryModuleTypeInit.CANT_INTERFACE, MemoryModuleStatus.REGISTERED,
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
			// TODO: do some complaining if result is false
			return false;
		}
		
		ItemStack scheduleItem = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.SCHEDULE_ITEM_INDEX);
		LazyOptional<IScheduleItemDataHandler> scheduleOptional = ScheduleItem.getDataHandler(scheduleItem);
		long dayTime = world.getDayTime() + TimeUtils.TIME_OFFSET;
		int minuteOfTheWeek = (int)(dayTime % TimeUtils.WEEK_TICKS / TimeUtils.MINUTE_TICKS);
		boolean shouldWork = scheduleOptional.map(h -> h.shouldWork(minuteOfTheWeek)).orElse(false);
		
		return !shouldWork;
	}
	
	@Override
	protected void start(ServerWorld world, NPCEntity npc, long gameTime) {
		if (gameTime > this.nextOkStartTime) {
			Brain<?> brain = npc.getBrain();
			Optional<GlobalPos> gpOptional = brain.getMemory(this.posMemoryType);
			gpOptional.ifPresent(gp -> {
				BlockPos targetPos = gp.pos();
				List<BlockPos> list = BlockPos.betweenClosedStream(targetPos.offset(-1, -2, -1), targetPos.offset(1, 0, 1)).map(BlockPos::immutable).collect(Collectors.toList());
				Collections.shuffle(list);
				Optional<BlockPos> accessPos = list.stream()
						.filter(pos -> world.loadedAndEntityCanStandOn(pos, npc))
						.filter(pos -> world.noCollision(npc))
						.findFirst();
				accessPos.ifPresent(pos -> {
					brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, this.speedModifier, this.closeEnoughDist));
				});
				if (!accessPos.isPresent()) {
					// TODO: Complain that area cannot be accessed
					brain.setMemory(MemoryModuleTypeInit.CANT_INTERFACE, true);
				}
			});
			this.nextOkStartTime = gameTime + 80L;
		}
	}
	
	@Override
	protected void tick(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		PathNavigator nav = npc.getNavigation();
		if (nav.isDone()) {
			brain.getMemory(this.posMemoryType).ifPresent(gp -> {
				BlockPos pos = gp.pos();
				TileEntity te = world.getBlockEntity(pos);
				AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, -2, -1), pos.offset(2, 1, 2));
				if (world.isLoaded(pos) && te != null && box.contains(npc.position())) {
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
								brain.setMemory(MemoryModuleTypeInit.WORKING, false);
								break;
							}
						}
						if (!inserted) {
							// TODO: Complain that can't deposit scroll
							brain.setMemory(MemoryModuleTypeInit.CANT_INTERFACE, true);
						}
					});
					if (!blockInvOptional.isPresent()) {
						// TODO: Complain that there's nothing to access here
						brain.setMemory(MemoryModuleTypeInit.CANT_INTERFACE, true);
					}
				}
			});
		}
	}	
	
	@Override
	protected boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		boolean cantInterface = brain.getMemory(MemoryModuleTypeInit.CANT_INTERFACE).orElse(false);
		boolean working = brain.getMemory(MemoryModuleTypeInit.WORKING).orElse(false);
		return !cantInterface && working;
	}
	
	@Override
	protected void stop(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		
		brain.eraseMemory(MemoryModuleTypeInit.CANT_INTERFACE);
		brain.eraseMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX);
		brain.eraseMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION);
	}
	
}
