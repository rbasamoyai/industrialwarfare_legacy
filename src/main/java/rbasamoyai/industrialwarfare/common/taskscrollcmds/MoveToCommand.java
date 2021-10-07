package rbasamoyai.industrialwarfare.common.taskscrollcmds;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class MoveToCommand extends TaskScrollCommand {
	
	public MoveToCommand() {
		super(true, false, TaskScrollCommand.NO_ARGS);
		this.setRegistryName(IndustrialWarfare.MOD_ID, "move_to");
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		return world.loadedAndEntityCanStandOn(order.getPos(), npc) && world.noCollision(npc) && order.getPos().closerThan(npc.position(), TaskScrollCommand.MAX_DISTANCE_FROM_POI);
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(order.getPos(), TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
	}
	
	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		AxisAlignedBB box = new AxisAlignedBB(order.getPos().offset(-1, 0, -1), order.getPos().offset(2, 3, 2));
		if (box.contains(npc.position())) {
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION, true);
		}
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		brain.setMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX, brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX).orElse(0) + 1);
	}

	@Override
	public boolean canStillUse(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		return npc.getBrain().getMemory(MemoryModuleTypeInit.STOP_EXECUTION).orElse(true);
	}
	
}
