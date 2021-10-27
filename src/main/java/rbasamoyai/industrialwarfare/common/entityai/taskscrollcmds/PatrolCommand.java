package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
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
		super(CommandTrees.PATROL, ImmutableMap.of(
				MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.CACHED_POS.get(), MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.ON_PATROL.get(), MemoryModuleStatus.REGISTERED
				));
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
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
		brain.setMemory(MemoryModuleTypeInit.ON_PATROL.get(), true);
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		UUID npcOwnerUUID = npc.getDataHandler().map(INPCDataHandler::getOwnerUUID).orElse(NPCEntity.GAIA_UUID);
		
		List<LivingEntity> visibleEntities = brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Arrays.asList());
		for (LivingEntity e : visibleEntities) {
			LazyOptional<INPCDataHandler> elzop = e.getCapability(NPCDataCapability.NPC_DATA_CAPABILITY);
			if (elzop.isPresent()) {
				INPCDataHandler handler = elzop.resolve().get();
				// TODO: implement diplomacy
				if (!handler.getOwnerUUID().equals(npcOwnerUUID)) {
					// Fight
				}
			}
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
		if (CommandUtils.hasComplaint(npc)) return;
		if (brain.getMemory(MemoryModuleTypeInit.STOP_EXECUTION.get()).get() == false) return; // Interrupt
		
		CommandUtils.incrementCurrentInstructionIndexMemory(npc);
	}

}
