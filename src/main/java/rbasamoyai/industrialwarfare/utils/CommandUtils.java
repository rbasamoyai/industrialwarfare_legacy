package rbasamoyai.industrialwarfare.utils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;

public class CommandUtils {

	public static void trySetWalkTarget(ServerWorld world, NPCEntity npc, BlockPos target, float speedModifier, int closeEnoughDist) {
		Brain<?> brain = npc.getBrain();
		
		List<BlockPos> list = BlockPos.betweenClosedStream(target.offset(-1, -2, -1), target.offset(1, 0, 1)).map(BlockPos::immutable).collect(Collectors.toList());
		Collections.shuffle(list);
		Optional<BlockPos> accessPos = list.stream()
				.filter(pos -> world.loadedAndEntityCanStandOn(pos, npc))
				.filter(pos -> world.noCollision(npc))
				.findFirst();
		accessPos.ifPresent(pos -> {
			brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(pos));
			brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, speedModifier, closeEnoughDist));
		});
		if (!accessPos.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.CANT_ACCESS.get());
		}
	}	
	
}
