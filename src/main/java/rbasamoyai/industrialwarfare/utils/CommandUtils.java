package rbasamoyai.industrialwarfare.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.NPCComplaint;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WaitMode;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;

public class CommandUtils {

	/**
	 * Will not set a new walk target if the NPC is within range of
	 * <b>target</b>.
	 * 
	 * @implNote
	 * NPC brain requires:
	 * <ul>
	 * <li>{@link net.minecraft.entity.ai.brain.memory.MemoryModuleType#WALK_TARGET MemoryModuleType#WALK_TARGET}</li>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#COMPLAINT MemoryModuleTypeInit#COMPLAINT}</li>
	 * </ul>
	 */
	public static void trySetInterfaceWalkTarget(ServerWorld world, NPCEntity npc, BlockPos target, float speedModifier, int closeEnoughDist) {
		AxisAlignedBB box = new AxisAlignedBB(target.offset(-1, -2, -1), target.offset(2, 1, 2));
		if (box.contains(npc.position())) return;
		
		List<BlockPos> list = BlockPos.betweenClosedStream(target.offset(-1, -2, -1), target.offset(1, 0, 1)).map(BlockPos::immutable).collect(Collectors.toList());
		Optional<BlockPos> optional = list.stream()
				.filter(pos -> world.loadedAndEntityCanStandOn(pos, npc))
				.filter(p -> world.noCollision(npc))
				.findFirst();
		if (!optional.isPresent()) {
			npc.getBrain().setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get(), 100L);
			return;
		}
		npc.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(optional.get(), speedModifier, closeEnoughDist));
	}
	
	/**
	 * @implNote
	 * NPC brain requires:
	 * <ul>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#COMPLAINT MemoryModuleTypeInit#COMPLAINT}</li>
	 * </ul>
	 */
	public static boolean validatePos(ServerWorld world, NPCEntity npc, Optional<BlockPos> optional, double maxDistanceFromPoi, NPCComplaint emptyComplaint) {
		if (!optional.isPresent()) {
			npc.getBrain().setMemory(MemoryModuleTypeInit.COMPLAINT.get(), emptyComplaint);
			return false;
		}
		BlockPos pos = optional.get();
		if (!pos.closerThan(npc.position(), maxDistanceFromPoi)) {
			npc.getBrain().setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TOO_FAR.get(), 200L);
			return false;
		}
		return true;
	}
	
	/**
	 * @implNote
	 * NPC brain requires:
	 * <ul>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#COMPLAINT MemoryModuleTypeInit#COMPLAINT}</li>
	 * </ul>
	 */
	public static boolean validateStandingPos(ServerWorld world, NPCEntity npc, Optional<BlockPos> optional, double maxDistanceFromPoi, NPCComplaint emptyComplaint) {
		if (!validatePos(world, npc, optional, maxDistanceFromPoi, emptyComplaint)) return false;
		if (!world.loadedAndEntityCanStandOn(optional.get(), npc) || !world.noCollision(npc)) {
			npc.getBrain().setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get(), 200L);
			return false;
		}
		return true;
	}
	
	/**
	 * @implNote
	 * NPC brain requires:
	 * <ul>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#COMPLAINT MemoryModuleTypeInit#COMPLAINT}</li>
	 * </ul>
	 */
	public static boolean validateWait(ServerWorld world, NPCEntity npc, WaitMode mode, NPCComplaint invalidModeComplaint) {
		if (mode == WaitMode.RELATIVE_TIME || mode == WaitMode.HEARD_BELL) return true;
		if (mode != WaitMode.DAY_TIME) {
			npc.getBrain().setMemory(MemoryModuleTypeInit.COMPLAINT.get(), invalidModeComplaint);
			return false;
		}
		if (!world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
			npc.getBrain().setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TIME_STOPPED.get(), 200L);
			return false;
		}
		return true;
	}
	
	/**
	 * @implNote
	 * NPC brain requires:
	 * <ul>
	 * <li>{@link net.minecraft.entity.ai.brain.memory.MemoryModuleType#HEARD_BELL_TIME MemoryModuleType#HEARD_BELL_TIME}</li>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#WAIT_FOR MemoryModuleTypeInit#WAIT_FOR}</li>
	 * <ul>
	 */
	public static void startWait(NPCEntity npc, WaitMode mode, long gameTime, long waitTime) {
		Brain<?> brain = npc.getBrain();	
		if (mode == WaitMode.DAY_TIME) {
			brain.setMemory(MemoryModuleTypeInit.WAIT_FOR.get(), waitTime);
		} else if (mode == WaitMode.RELATIVE_TIME) {
			brain.setMemory(MemoryModuleTypeInit.WAIT_FOR.get(), gameTime + waitTime);
		}
		brain.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
	}
	
	/**
	 * @implNote
	 * NPC brain requires:
	 * <ul>
	 * <li>{@link net.minecraft.entity.ai.brain.memory.MemoryModuleType#HEARD_BELL_TIME MemoryModuleType#HEARD_BELL_TIME}</li>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#COMPLAINT MemoryModuleTypeInit#COMPLAINT}</li>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#COMPLAINT MemoryModuleTypeInit#STOP_EXECUTION}</li>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#COMPLAINT MemoryModuleTypeInit#WAIT_FOR}</li>
	 * </ul>
	 */
	public static void tickWait(ServerWorld world, NPCEntity npc, WaitMode mode, long gameTime) {
		Brain<?> brain = npc.getBrain();
		long waitUntil = brain.getMemory(MemoryModuleTypeInit.WAIT_FOR.get()).orElse(0L);
		
		if (mode == WaitMode.DAY_TIME) {
			if (!world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
				brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.TIME_STOPPED.get(), 200L);
				return;
			} else if ((world.getDayTime() + TimeUtils.TIME_OFFSET) % 24000L >= waitUntil) {
				brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
				return;
			}
		}
		
		if (mode == WaitMode.RELATIVE_TIME && gameTime >= waitUntil || mode == WaitMode.HEARD_BELL && brain.hasMemoryValue(MemoryModuleType.HEARD_BELL_TIME)) {
			brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION.get(), true);
		}
	}
	
	/**
	 * @implNote
	 * NPC brain requires:
	 * <ul>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#CURRENT_ORDER_INDEX MemoryModuleTypeInit#CURRENT_INSTRUCTION_INDEX}</li>
	 * </ul>
	 */
	public static void incrementCurrentInstructionIndexMemory(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		brain.setMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get(), brain.getMemory(MemoryModuleTypeInit.CURRENT_ORDER_INDEX.get()).orElse(0) + 1);
	}
	
	public static boolean hasComplaint(NPCEntity npc) {
		return npc.getBrain().hasMemoryValue(MemoryModuleTypeInit.COMPLAINT.get());
	}
	
	/**
	 * @implNote
	 * NPC brain requires:
	 * <ul>
	 * <li>{@link rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit#COMPLAINT MemoryModuleTypeInit#COMPLAINT}</li>
	 * </ul>
	 */
	public static void complain(NPCEntity npc, NPCComplaint complaint) {
		npc.getBrain().setMemory(MemoryModuleTypeInit.COMPLAINT.get(), complaint);
	}

	public static boolean filterMatches(ItemStack filter, ItemStack stack) {
		if (filter.isEmpty() && !stack.isEmpty()) {
			return true;
		} else {
			// TODO: more complex stuff, such as filter items
			return filter.getItem().equals(stack.getItem());
		}
	}
	
	private static final Map<Integer, EquipmentSlotType> BY_FILTER_FLAG =
			Arrays.stream(EquipmentSlotType.values())
			.collect(Collectors.toMap(EquipmentSlotType::getFilterFlag, e -> e));

	
	public static EquipmentSlotType equipmentSlotTypeFromFilterFlag(int flag) {
		return BY_FILTER_FLAG.get(flag);
	}
}
