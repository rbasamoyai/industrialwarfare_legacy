package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.NPCActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WaitMode;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.CommandUtils;

public class PatrolCommand extends TaskScrollCommand {

	private static final int POS_ARG_INDEX = 0;
	private static final int PURSUIT_ARG_INDEX = 1;
	private static final int WAIT_MODE_ARG_INDEX = 2;
	private static final int WAIT_TIME_ARG_INDEX = 3;
	
	public PatrolCommand() {
		super(CommandTrees.PATROL,
				() -> ImmutableMap.<MemoryModuleType<?>, MemoryModuleStatus>builder()
				.put(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleType.HEARD_BELL_TIME, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.CACHED_POS.get(), MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.ON_PATROL.get(), MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.WAIT_FOR.get(), MemoryModuleStatus.REGISTERED)
				.build());
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		if (!CommandUtils.validatePos(world, npc, order.getWrappedArg(POS_ARG_INDEX).getPos(), TaskScrollCommand.MAX_DISTANCE_FROM_POI, NPCComplaintInit.INVALID_ORDER.get())) {
			return false;
		}
		return CommandUtils.validateWait(world, npc, WaitMode.fromId(order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum()), NPCComplaintInit.INVALID_ORDER.get());
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		Optional<GlobalPos> gpop = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get());
		if (gpop.isPresent()) {
			GlobalPos gp = gpop.get();
			if (gp.dimension() == world.dimension()) pos = gp.pos();
		}
		
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
		brain.setMemory(MemoryModuleTypeInit.ON_PATROL.get(), order.getWrappedArg(PURSUIT_ARG_INDEX).getArgNum());
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		Optional<LivingEntity> target = brain.getMemory(MemoryModuleType.ATTACK_TARGET);
		if (target.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), false);
		}
		
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2));
		
		if (!box.contains(npc.position())) {
			if (npc.getNavigation().isDone()) {
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
			}
			return;
		}

		WaitMode workMode = WaitMode.fromId(order.getWrappedArg(WAIT_MODE_ARG_INDEX).getArgNum());
		if (workMode != WaitMode.HEARD_BELL && !brain.hasMemoryValue(MemoryModuleTypeInit.WAIT_FOR.get())) {
			if (!CommandUtils.validateWait(world, npc, workMode, NPCComplaintInit.INVALID_ORDER.get())) return;
			CommandUtils.startWait(npc, workMode, gameTime, (long) order.getWrappedArg(WAIT_TIME_ARG_INDEX).getArgNum() * 20L);
		}
		CommandUtils.tickWait(world, npc, workMode, gameTime);
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		brain.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
		brain.eraseMemory(MemoryModuleTypeInit.WAIT_FOR.get());
		
		if (CommandUtils.hasComplaint(npc)) {
			return;
		}
		
		if (!brain.getMemory(MemoryModuleTypeInit.STOP_EXECUTION.get()).orElse(true)) { // Interrupt
			brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(world.dimension(), npc.blockPosition()));
			brain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), NPCActivityStatus.FIGHTING);
			brain.setActiveActivityIfPossible(Activity.FIGHT);
			return;
		}
		
		CommandUtils.incrementCurrentInstructionIndexMemory(npc);
		brain.eraseMemory(MemoryModuleTypeInit.CACHED_POS.get());
		brain.eraseMemory(MemoryModuleTypeInit.ON_PATROL.get());
	}

}
