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
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class RunCommandFromTaskScrollTask extends Task<NPCEntity> {
	
	private long nextOkStartTime;
	
	public RunCommandFromTaskScrollTask() {
		super(ImmutableMap.<MemoryModuleType<?>, MemoryModuleStatus>builder()
				.put(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), MemoryModuleStatus.VALUE_PRESENT)
				.put(MemoryModuleTypeInit.COMPLAINT.get(), MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.CURRENT_ORDER.get(), MemoryModuleStatus.VALUE_ABSENT)
				.put(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get(), MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get(), MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.STOP_EXECUTION.get(), MemoryModuleStatus.REGISTERED)
				.build());
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc) {
		Brain<NPCEntity> brain = npc.getBrain();
		if (brain.getMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get()).get() != ActivityStatus.WORKING
				|| brain.getMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get()).orElse(false)
				|| CommandUtils.hasComplaint(npc)) {
			return false;
		}
		
		Optional<Integer> indexOptional = brain.getMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get());
		int currentIndex;
		if (indexOptional.orElse(-1) < 0) {
			brain.setMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get(), 0);
			currentIndex = 0;
		} else {
			currentIndex = indexOptional.get();
		}
		
		ItemStack stack = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.TASK_ITEM_INDEX);
		LazyOptional<ITaskScrollDataHandler> handlerOptional = TaskScrollItem.getDataHandler(stack);
		if (!handlerOptional.isPresent()) return false;
		ITaskScrollDataHandler handler = handlerOptional.resolve().get();
		
		if (currentIndex < 0 || currentIndex >= handler.getList().size()) return false;
		TaskScrollOrder order = handler.getOrder(currentIndex);
		TaskScrollCommand command = order.getCommand();
		return command.hasRequiredMemories(brain) && command.checkExtraStartConditions(world, npc, order);
	}
	
	@Override
	protected void start(ServerWorld world, NPCEntity npc, long gameTime) {
		if (gameTime > this.nextOkStartTime) {
			Brain<?> brain = npc.getBrain();
			ItemStack stack = npc.getEquipmentItemHandler().getStackInSlot(EquipmentItemHandler.TASK_ITEM_INDEX);
			LazyOptional<ITaskScrollDataHandler> optional = TaskScrollItem.getDataHandler(stack);
			
			optional.ifPresent(h -> {
				TaskScrollOrder order = h.getOrder(brain.getMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get()).orElse(0));
				order.getCommand().start(world, npc, gameTime, order);
				brain.setMemory(MemoryModuleTypeInit.CURRENT_ORDER.get(), order);
				brain.setMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get(), true);
			});
			
			this.nextOkStartTime = gameTime + 20L;
		}
	}
	
	@Override
	protected void tick(ServerWorld world, NPCEntity npc, long gameTime) {
		TaskScrollOrder order = npc.getBrain().getMemory(MemoryModuleTypeInit.CURRENT_ORDER.get()).get();
		order.getCommand().tick(world, npc, gameTime, order);
	}
	
	@Override
	protected boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		
		return brain.getMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get()).get() == ActivityStatus.WORKING
				&& brain.getMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get()).orElse(false)
				&& !CommandUtils.hasComplaint(npc)
				&& brain.hasMemoryValue(MemoryModuleTypeInit.CURRENT_ORDER.get())
				&& !brain.hasMemoryValue(MemoryModuleTypeInit.STOP_EXECUTION.get());
	}
	
	@Override
	protected void stop(ServerWorld world, NPCEntity npc, long gameTime) {
		Brain<?> brain = npc.getBrain();
		if (!CommandUtils.hasComplaint(npc)) {
			if (brain.hasMemoryValue(MemoryModuleTypeInit.CURRENT_ORDER.get())) {
				TaskScrollOrder order = brain.getMemory(MemoryModuleTypeInit.CURRENT_ORDER.get()).get();
				order.getCommand().stop(world, npc, gameTime, order);
			}
			brain.eraseMemory(MemoryModuleTypeInit.STOP_EXECUTION.get());
		}
		brain.setMemory(MemoryModuleTypeInit.EXECUTING_INSTRUCTION.get(), false);
		brain.eraseMemory(MemoryModuleTypeInit.CURRENT_ORDER.get());
	}
	
	@Override
	protected boolean timedOut(long time) {
		return false;
	}
	
}
