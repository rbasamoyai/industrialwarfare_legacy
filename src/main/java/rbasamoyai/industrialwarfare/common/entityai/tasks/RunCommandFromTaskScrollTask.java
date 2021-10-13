package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll.ITaskScrollDataHandler;
import rbasamoyai.industrialwarfare.common.containers.npcs.EquipmentItemHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class RunCommandFromTaskScrollTask extends Task<NPCEntity> {
	
	private long nextOkStartTime;
	
	public RunCommandFromTaskScrollTask() {
		super(ImmutableMap.<MemoryModuleType<?>, MemoryModuleStatus>builder()
				.put(MemoryModuleType.HEARD_BELL_TIME, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleType.JOB_SITE, MemoryModuleStatus.VALUE_PRESENT)
				.put(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.CANT_INTERFACE, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.EXECUTING_INSTRUCTION, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.JUMP_TO, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.STOP_EXECUTION, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.WAIT_FOR, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.WORKING, MemoryModuleStatus.VALUE_PRESENT)
				.build(), 6000); // Ample amount of time, maybe not so much if you're waiting for relative time
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		if (!brain.getMemory(MemoryModuleTypeInit.WORKING).orElse(false)
				|| brain.getMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION).orElse(false)
				|| brain.hasMemoryValue(MemoryModuleTypeInit.CANT_INTERFACE)) {
			return false;
		}
		
		Optional<Integer> indexOptional = brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX);
		int currentIndex;
		if (indexOptional.orElse(-1) < 0) {
			brain.setMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX, 0);
			currentIndex = 0;
		} else {
			currentIndex = indexOptional.get();
		}
		
		ItemStack stack = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.TASK_ITEM_INDEX);
		LazyOptional<ITaskScrollDataHandler> taskScrollOptional = TaskScrollItem.getDataHandler(stack);
		boolean indexIsValid = taskScrollOptional.map(h -> 0 <= currentIndex && currentIndex < h.getList().size()).orElse(false);
		if (indexIsValid) {
			return taskScrollOptional.map(h -> {
				TaskScrollOrder order = h.getOrder(currentIndex);
				return order.getCommand().checkExtraStartConditions(world, npc, order);
			}).orElse(false);
		} else {
			return false;
		}
	}
	
	@Override
	protected void start(ServerWorld world, NPCEntity npc, long gameTime) {
		if (gameTime > this.nextOkStartTime) {
			Brain<?> brain = npc.getBrain();
			ItemStack stack = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.TASK_ITEM_INDEX);
			LazyOptional<ITaskScrollDataHandler> optional = TaskScrollItem.getDataHandler(stack);
			
			optional.ifPresent(h -> {
				TaskScrollOrder order = h.getOrder(brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX).orElse(0));
				order.getCommand().start(world, npc, gameTime, order);
				brain.setMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION, true);
			});
			
			this.nextOkStartTime = gameTime + 20L;
		}
	}
	
	@Override
	protected void tick(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		ItemStack stack = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.TASK_ITEM_INDEX);
		LazyOptional<ITaskScrollDataHandler> optional = TaskScrollItem.getDataHandler(stack);
		
		optional.ifPresent(h -> {
			TaskScrollOrder order = h.getOrder(brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX).orElse(0));
			order.getCommand().tick(world, npc, gameTime, order);
		});
	}
	
	@Override
	protected boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		
		return brain.getMemory(MemoryModuleTypeInit.WORKING).orElse(false)
				&& brain.getMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION).orElse(false)
				&& !brain.hasMemoryValue(MemoryModuleTypeInit.STOP_EXECUTION)
				&& !brain.hasMemoryValue(MemoryModuleTypeInit.CANT_INTERFACE);
	}
	
	@Override
	protected void stop(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		if (brain.hasMemoryValue(MemoryModuleTypeInit.STOP_EXECUTION)) {
			ItemStack stack = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.TASK_ITEM_INDEX);
			LazyOptional<ITaskScrollDataHandler> optional = TaskScrollItem.getDataHandler(stack);
			
			optional.ifPresent(h -> {
				TaskScrollOrder order = h.getOrder(brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX).orElse(0));
				order.getCommand().stop(world, npc, gameTime, order);
			});
			brain.setMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION, false);
			brain.eraseMemory(MemoryModuleTypeInit.STOP_EXECUTION);
		}
	}
	
}
