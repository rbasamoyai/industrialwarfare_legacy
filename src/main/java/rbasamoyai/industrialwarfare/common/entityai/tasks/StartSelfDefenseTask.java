package rbasamoyai.industrialwarfare.common.entityai.tasks;

import java.util.ArrayList;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.HasDiplomaticOwner;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class StartSelfDefenseTask<E extends Mob & HasDiplomaticOwner> extends Behavior<E> {
	
	public StartSelfDefenseTask() {
		super(ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
				MemoryModuleTypeInit.DEFENDING_SELF.get(), MemoryStatus.VALUE_ABSENT));
	}
	
	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		Brain<?> brain = entity.getBrain();
		PlayerIDTag npcOwner = entity.getDiplomaticOwner();
		DiplomacySaveData saveData = DiplomacySaveData.get(level);
		
		if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
			LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
			if (target.isAlive()) return true;
			
			if (target instanceof HasDiplomaticOwner) {
				PlayerIDTag targetOwner = ((HasDiplomaticOwner) target).getDiplomaticOwner();
				if (npcOwner.equals(targetOwner)) {
					return entity.hasOwner();
				}
				DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, targetOwner);
				if (status == DiplomaticStatus.ALLY) {
					return false;
				}
			}
			
			if (target instanceof Player) {
				Player player = (Player) target;
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
	protected void start(ServerLevel level, E entity, long gameTime) {
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
			if (directAttacker instanceof HasDiplomaticOwner) {
				PlayerIDTag attackerOwner = ((HasDiplomaticOwner) directAttacker).getDiplomaticOwner();
				if (npcOwner.equals(attackerOwner) && !entity.hasOwner()) {
					return Optional.of(directAttacker);
				} else {
					DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, attackerOwner);
					if (status != DiplomaticStatus.ALLY) {
						return Optional.of(directAttacker);
					}
				}
			}
			if (directAttacker instanceof Player) {
				Player player = (Player) directAttacker;
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
		
		for (LivingEntity e : brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).map(nvle -> nvle.findAll(e -> true)).orElse(new ArrayList<>())) {
			Brain<?> targetBrain = e.getBrain();
			if (targetBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && targetBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get() == entity) {
				return Optional.of(e);
			}
			if (e instanceof Mob && ((Mob) e).getTarget() == entity) {
				return Optional.of(e);
			}
		}
		return Optional.empty();
	}

}
