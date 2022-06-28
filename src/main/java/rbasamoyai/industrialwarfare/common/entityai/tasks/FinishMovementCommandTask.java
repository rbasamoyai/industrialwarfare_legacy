package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.AABB;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class FinishMovementCommandTask extends Behavior<LivingEntity> {

	private final MemoryModuleType<GlobalPos> memoryType;
	
	public FinishMovementCommandTask(MemoryModuleType<GlobalPos> memoryType) {
		super(ImmutableMap.<MemoryModuleType<?>, MemoryStatus>builder()
				.put(memoryType, MemoryStatus.VALUE_PRESENT)
				.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED)
				.put(MemoryModuleTypeInit.IN_FORMATION.get(), MemoryStatus.VALUE_ABSENT)
				.put(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), MemoryStatus.VALUE_PRESENT)
				.put(MemoryModuleTypeInit.PRECISE_POS.get(), MemoryStatus.REGISTERED)
				.put(MemoryModuleTypeInit.REACHED_MOVEMENT_TARGET.get(), MemoryStatus.VALUE_ABSENT)
				.build());
		this.memoryType = memoryType;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
		Brain<?> brain = entity.getBrain();
		
		GlobalPos gpos = brain.getMemory(this.memoryType).get();
		if (level.dimension() != gpos.dimension()) return true;
		int closeEnoughDist = brain.getMemory(MemoryModuleType.WALK_TARGET).map(WalkTarget::getCloseEnoughDist).orElse(0);
		if (entity.blockPosition().distManhattan(gpos.pos()) <= closeEnoughDist) {
			return true;
		}
		
		AABB checkAround = entity.getBoundingBox().inflate(1.0d, 1.0d, 1.0d);
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, checkAround)) {
			Brain<?> brain1 = e.getBrain();
			if (this.checkMemories(brain1) && getCommandGroup(brain).equals(getCommandGroup(brain1))) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
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
