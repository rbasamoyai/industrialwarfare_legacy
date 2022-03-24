package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.IHasDiplomaticOwner;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class StartSelfDefenseTask<E extends MobEntity & IHasDiplomaticOwner> extends Task<E> {
	
	public StartSelfDefenseTask() {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.DEFENDING_SELF.get(), MemoryModuleStatus.VALUE_ABSENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerWorld level, E entity) {
		Brain<?> brain = entity.getBrain();
		PlayerIDTag npcOwner = entity.getDiplomaticOwner();
		DiplomacySaveData saveData = DiplomacySaveData.get(level);
		
		if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
			LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
			if (target.isAlive()) return true;
			
			if (target instanceof IHasDiplomaticOwner) {
				PlayerIDTag targetOwner = ((IHasDiplomaticOwner) target).getDiplomaticOwner();
				if (npcOwner.equals(targetOwner)) {
					return entity.hasOwner();
				}
				DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, targetOwner);
				if (status == DiplomaticStatus.ALLY) {
					return false;
				}
			}
			
			if (target instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) target;
				if (player.isCreative() || player.isSpectator()) return false;
				PlayerIDTag playerTag = PlayerIDTag.of(player);
				if (npcOwner.equals(playerTag)) return false;
			}
		}
		if (brain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())) {
			return false;
		}
		if (brain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) {
			return brain.getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).orElse(CombatMode.ATTACK) != CombatMode.DONT_ATTACK;
		}
		return true;
	}
	
	@Override
	protected void start(ServerWorld level, E entity, long gameTime) {
		Optional<LivingEntity> optional = this.findNearestAttacker(entity);
		if (!optional.isPresent()) return;
		Brain<?> brain = entity.getBrain();
		brain.setMemory(MemoryModuleType.ATTACK_TARGET, optional);
		brain.setMemory(MemoryModuleTypeInit.DEFENDING_SELF.get(), true);
		brain.setActiveActivityIfPossible(Activity.FIGHT);
	}
	
	private Optional<LivingEntity> findNearestAttacker(E entity) {
		Brain<?> brain = entity.getBrain();
		PlayerIDTag npcOwner = entity.getDiplomaticOwner();
		DiplomacySaveData saveData = DiplomacySaveData.get(entity.level);
		
		LivingEntity directAttacker = entity.getLastHurtByMob();
		if (directAttacker != null && directAttacker.isAlive()) {
			if (directAttacker instanceof IHasDiplomaticOwner) {
				PlayerIDTag attackerOwner = ((IHasDiplomaticOwner) directAttacker).getDiplomaticOwner();
				if (npcOwner.equals(attackerOwner) && !entity.hasOwner()) {
					return Optional.of(directAttacker);
				} else {
					DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, attackerOwner);
					if (status != DiplomaticStatus.ALLY) {
						return Optional.of(directAttacker);
					}
				}
			}
			if (directAttacker instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) directAttacker;
				if (!player.isCreative() && !player.isSpectator()) {
					PlayerIDTag playerTag = PlayerIDTag.of(player);
					if (!npcOwner.equals(playerTag)) {
						DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, playerTag);
						if (status != DiplomaticStatus.ALLY) {
							return Optional.of(directAttacker);
						}
					}
				} 
			}
		}
		
		List<LivingEntity> visibleEntities = brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(new ArrayList<>());
		for (LivingEntity e : visibleEntities) {
			Brain<?> targetBrain = e.getBrain();
			if (targetBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && targetBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get() == entity) {
				return Optional.of(e);
			}
			if (e instanceof MobEntity && ((MobEntity) e).getTarget() == entity) {
				return Optional.of(e);
			}
		}
		return Optional.empty();
	}

}
