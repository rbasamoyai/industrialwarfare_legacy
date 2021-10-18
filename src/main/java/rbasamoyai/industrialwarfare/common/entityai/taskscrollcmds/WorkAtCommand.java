package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds;

import java.util.Optional;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.IWorkstationDataHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.WaitForCommand.WaitModes;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree.CommandTrees;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.common.npcprofessions.NPCProfession;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;
import rbasamoyai.industrialwarfare.utils.TimeUtils;

public class WorkAtCommand extends TaskScrollCommand {

	private static final int POS_ARG_INDEX = 0;
	private static final int WORK_MODE_ARG_INDEX = 1;
	private static final int WORK_TIME_ARG_INDEX = 2;
	
	public WorkAtCommand() {
		super(CommandTrees.WORK_AT);
	}
	
	@Override
	public boolean checkExtraStartConditions(ServerWorld world, NPCEntity npc, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		Optional<BlockPos> optional = order.getWrappedArg(POS_ARG_INDEX).getPos();
		if (!optional.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_ORDER.get());
			return false;
		}
		
		BlockPos pos = optional.get();
		boolean inRange = pos.closerThan(npc.position(), TaskScrollCommand.MAX_DISTANCE_FROM_POI);
		boolean blockCheck = world.loadedAndEntityCanStandOn(pos, npc)
				&& world.noCollision(npc)
				&& inRange;
		
		if (!blockCheck) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(),
					inRange
					? NPCComplaintInit.CANT_ACCESS.get()
					: NPCComplaintInit.TOO_FAR.get());
			return false;
		}
		
		int workMode = order.getWrappedArg(WORK_MODE_ARG_INDEX).getArgNum();
		boolean doingDaylightCycle = world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
		boolean workCheck = workMode == WaitModes.DAY_TIME && doingDaylightCycle
				|| workMode == WaitModes.RELATIVE_TIME
				|| workMode == WaitModes.BELL;
		
		if (!workCheck) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(),
					workMode == WaitForCommand.WaitModes.DAY_TIME && !doingDaylightCycle 
					? NPCComplaintInit.TIME_STOPPED.get()
					: NPCComplaintInit.INVALID_ORDER.get());
			return false;
		}
		
		return true;
	}

	@Override
	public void start(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		npc.getDataHandler().ifPresent(h -> {
			NPCProfession profession = h.getProfession();
			
			BlockPos target = order.getWrappedArg(POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO);
			Optional<BlockPos> posOptional = profession.getWorkComponent().getWorkingArea(world, target, npc);
			posOptional.ifPresent(pos -> {
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
				brain.setMemory(MemoryModuleTypeInit.CACHED_POS.get(), GlobalPos.of(world.dimension(), pos));
			});
			if (!posOptional.isPresent()) {
				brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get());
			}
		});
		if (!npc.getDataHandler().isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.NO_DATA_HANDLER.get());
		}
	}

	@Override
	public void tick(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		
		BlockPos pos = order.getWrappedArg(POS_ARG_INDEX).getPos().get();
		AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2));
			
		boolean atDestination = box.contains(npc.position());
		
		if (!atDestination) {
			if (npc.getNavigation().isDone()) {
				BlockPos cachedPos = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get()).map(GlobalPos::pos).orElse(pos.below());
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(cachedPos, TaskScrollCommand.SPEED_MODIFIER, TaskScrollCommand.CLOSE_ENOUGH_DIST));
			}
			return;
		}
		
		TileEntity te = world.getBlockEntity(pos);
		if (te == null) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get());
			return;
		}
		
		if (!(te instanceof WorkstationTileEntity)) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get());
			return;
		}
		
		WorkstationTileEntity workstationTE = (WorkstationTileEntity) te;
		Optional<IWorkstationDataHandler> optional = workstationTE.getDataHandler().resolve();
		
		if (!optional.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get());
			return;
		}
		
		IWorkstationDataHandler handler = optional.get();
		
		if (handler.hasWorker()) {
			if (!handler.getWorkerUUID().equals(npc.getUUID())) {
				brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get());
				return;
			}
		} else {
			workstationTE.attemptCraft(npc);
		}
		
		if (!brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
			brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(pos));
		}
		
		int workMode = order.getWrappedArg(WORK_MODE_ARG_INDEX).getArgNum();
		boolean doingDaylightCycle = world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
		
		if (workMode != WaitModes.BELL && brain.hasMemoryValue(MemoryModuleTypeInit.WAIT_FOR.get())) {
			int workTimeArg = order.getWrappedArg(WORK_TIME_ARG_INDEX).getArgNum();
			long workTime = (long) workTimeArg * 20L;
			long workUntil = 0;
			
			if (workMode == WaitModes.DAY_TIME) {
				workUntil = workTime;
			} else if (workMode == WaitModes.RELATIVE_TIME) {
				if (!doingDaylightCycle) {
					brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TIME_STOPPED.get());
					return;
				}
				workUntil = gameTime + workTime;
			}
			
			brain.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
			brain.setMemory(MemoryModuleTypeInit.WAIT_FOR.get(), workUntil);
		}
		
		if (workMode == WaitModes.DAY_TIME && !doingDaylightCycle) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TIME_STOPPED.get());
			return;
		}
		
		int workUntil = brain.getMemory(MemoryModuleTypeInit.WAIT_FOR.get()).orElse(0L).intValue();
		if (workMode == WaitModes.DAY_TIME && (int)(world.getDayTime() + TimeUtils.TIME_OFFSET) % 24000L >= workUntil
				|| workMode == WaitModes.RELATIVE_TIME && gameTime >= workUntil
				|| workMode == WaitModes.BELL && brain.hasMemoryValue(MemoryModuleType.HEARD_BELL_TIME)) {
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
		}
	}

	@Override
	public void stop(ServerWorld world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		Brain<?> brain = npc.getBrain();
		if (!brain.hasMemoryValue(MemoryModuleTypeInit.COMPLAINT.get())) {
			int index = brain.getMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX.get()).orElse(0);
			brain.setMemory(MemoryModuleTypeInit.CURRENT_INSTRUCTION_INDEX.get(), index + 1);
		}
		brain.eraseMemory(MemoryModuleTypeInit.CACHED_POS.get());
		brain.eraseMemory(MemoryModuleTypeInit.WAIT_FOR.get());
		
		TileEntity te = world.getBlockEntity(order.getWrappedArg(POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO));
		if (te == null) return;
		if (!(te instanceof WorkstationTileEntity)) return;
		((WorkstationTileEntity) te).setRecipe(ItemStack.EMPTY, false);
	}

}
