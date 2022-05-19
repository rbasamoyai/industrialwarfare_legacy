package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class FinishMovementCommandTask extends Task<LivingEntity> {

	private final MemoryModuleType<GlobalPos> memoryType;
	
	public FinishMovementCommandTask(MemoryModuleType<GlobalPos> memoryType) {
		super(ImmutableMap.<MemoryModuleType<?>, MemoryModuleStatus>builder()
				.put(memoryType, MemoryModuleStatus.VALUE_PRESENT)
				.put(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.IN_FORMATION.get(), MemoryModuleStatus.VALUE_ABSENT)
				.put(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryModuleStatus.VALUE_PRESENT)
				.put(MemoryModuleTypeInit.PRECISE_POS.get(), MemoryModuleStatus.REGISTERED)
				.put(MemoryModuleTypeInit.REACHED_MOVEMENT_TARGET.get(), MemoryModuleStatus.VALUE_ABSENT)
				.build());
		this.memoryType = memoryType;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, LivingEntity entity) {
		Brain<?> brain = entity.getBrain();
		
		GlobalPos gpos = brain.getMemory(this.memoryType).get();
		if (level.dimension() != gpos.dimension()) return true;
		int closeEnoughDist = brain.getMemory(MemoryModuleType.WALK_TARGET).map(WalkTarget::getCloseEnoughDist).orElse(0);
		if (entity.blockPosition().distManhattan(gpos.pos()) <= closeEnoughDist) {
			return true;
		}
		
		AxisAlignedBB checkAround = entity.getBoundingBox().inflate(1.0d, 1.0d, 1.0d);
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, checkAround)) {
			Brain<?> brain1 = e.getBrain();
			if (this.checkMemories(brain1) && getCommandGroup(brain).equals(getCommandGroup(brain1))) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void start(ServerWorld level, LivingEntity entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		brain.eraseMemory(this.memoryType);
		brain.eraseMemory(MemoryModuleTypeInit.PRECISE_POS.get());
		brain.setMemory(MemoryModuleTypeInit.REACHED_MOVEMENT_TARGET.get(), true);
	}
	
	private boolean checkMemories(Brain<?> brain) {
		return brain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())
			&& !brain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())
			&& brain.hasMemoryValue(MemoryModuleTypeInit.REACHED_MOVEMENT_TARGET.get());
	}
	
	private static UUID getCommandGroup(Brain<?> brain) {
		return brain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
	}
	
}
