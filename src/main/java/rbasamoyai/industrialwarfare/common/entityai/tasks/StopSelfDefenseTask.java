package rbasamoyai.industrialwarfare.common.entityai.tasks;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class StopSelfDefenseTask extends Behavior<Mob> {

	private final Activity nextActivity;
	
	public StopSelfDefenseTask(Activity nextActivity) {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleTypeInit.DEFENDING_SELF.get(), MemoryStatus.VALUE_PRESENT));
		this.nextActivity = nextActivity;
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, Mob entity) {
		return entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map(target -> {
			if (target instanceof Player && (((Player) target).isCreative() || ((Player) target).isSpectator())) {
				return true;
			}
			return target.isDeadOrDying();
		}).orElse(true);
	}
	
	@Override
	protected void start(ServerLevel level, Mob entity, long gameTime) {
		Brain<?> brain = entity.getBrain();
		brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
		brain.eraseMemory(MemoryModuleTypeInit.DEFENDING_SELF.get());
		if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
			brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		}
		brain.setActiveActivityIfPossible(this.nextActivity);
	}
	
}
