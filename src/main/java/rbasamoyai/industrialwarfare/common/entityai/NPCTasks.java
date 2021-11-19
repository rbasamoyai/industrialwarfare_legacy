package rbasamoyai.industrialwarfare.common.entityai;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.AttackTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.InteractWithDoorTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.LookTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.SwimTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsPosTask;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.NPCDataCapability;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.tasks.EndAttackWithPatrolTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.ExtendedShootTargetTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.GoToWorkTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.LeaveWorkTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.ReturnToWorkIfPatrollingTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.RunCommandFromTaskScrollTask;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class NPCTasks {

	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getCorePackage() {
		return ImmutableList.of(
				Pair.of(0, new GoToWorkTask(MemoryModuleType.JOB_SITE, 3.0f, 1, 100)),
				Pair.of(0, new LeaveWorkTask(MemoryModuleType.JOB_SITE, 3.0f, 1, 100)),
				Pair.of(0, new WalkToTargetTask()),
				Pair.of(0, new InteractWithDoorTask()),
				Pair.of(0, new SwimTask(0.8f)),
				Pair.of(1, new LookTask(45, 90))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getIdlePackage() {
		return ImmutableList.of(
				Pair.of(0, new WalkTowardsPosTask(MemoryModuleType.MEETING_POINT, 2.5f, 1, 100)),
				Pair.of(1, new LookAtEntityTask(4.0f))//,
				//Pair.of(2, new WalkTowardsLookTargetTask(2.5f, 2))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getWorkPackage() {
		return ImmutableList.of(
				Pair.of(0, new RunCommandFromTaskScrollTask()),
				Pair.of(1, new ForgetAttackTargetTask<>(NPCTasks::onPatrol, NPCTasks::findNearestValidAttackTarget))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getRestPackage() {
		return ImmutableList.of(Pair.of(0, new WalkTowardsPosTask(MemoryModuleType.HOME, 2.5f, 1, 100)));
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getFightPackage() {
		return ImmutableList.of(
				Pair.of(0, new MoveToTargetTask(3.0f)),
				Pair.of(0, new ExtendedShootTargetTask<>()),
				Pair.of(0, new ForgetAttackTargetTask<>(NPCTasks::findNearestValidAttackTarget)),
				Pair.of(0, new EndAttackWithPatrolTask()),
				Pair.of(1, new AttackTargetTask(20)),
				Pair.of(1, new ReturnToWorkIfPatrollingTask())
				);
	}
	
	private static boolean onPatrol(NPCEntity npc) {
		return npc.getBrain().hasMemoryValue(MemoryModuleTypeInit.ON_PATROL.get());
	}
	
	private static Optional<? extends LivingEntity> findNearestValidAttackTarget(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		
		PlayerIDTag npcOwner = npc.getDataHandler().map(INPCDataHandler::getOwner).orElse(PlayerIDTag.NO_OWNER);

		DiplomacySaveData saveData = DiplomacySaveData.get(npc.level);
		
		Optional<Integer> pursuitOptional = brain.getMemory(MemoryModuleTypeInit.ON_PATROL.get());
		boolean onPatrol = false;
		int pursuitDistance = 0;
		if (pursuitOptional.isPresent()) {
			onPatrol = true;
			pursuitDistance = pursuitOptional.get();
		}
		Optional<GlobalPos> gpop = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get());
		BlockPos pos = gpop.isPresent() ? gpop.get().pos() : npc.blockPosition();
		
		List<LivingEntity> visibleEntities = brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Arrays.asList());
		for (LivingEntity e : visibleEntities) {
			
			if (onPatrol && !e.blockPosition().closerThan(pos, pursuitDistance)) continue;
			
			LazyOptional<INPCDataHandler> elzop = e.getCapability(NPCDataCapability.NPC_DATA_CAPABILITY);
			if (elzop.isPresent()) {
				INPCDataHandler handler = elzop.resolve().get();
				PlayerIDTag otherOwner = handler.getOwner();
				DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, otherOwner);
				if (status != DiplomaticStatus.ALLY || /* DEBUG */ otherOwner.equals(PlayerIDTag.NO_OWNER)) {
					// TODO: Fight given extra qualifiers
					return Optional.of(e);
				}
				continue;
			}
			
			if (e instanceof PlayerEntity) {
				PlayerIDTag otherPlayerTag = PlayerIDTag.of((PlayerEntity) e);
				if (npcOwner.equals(otherPlayerTag)) continue;
				DiplomaticStatus status = saveData.getDiplomaticStatus(npcOwner, PlayerIDTag.of((PlayerEntity) e));
				// TODO: stand down modifier (e.g. diplomatic meeting in game or sumfin)
				if (status != DiplomaticStatus.ALLY) return Optional.of(e);
			}

		}
		
		return Optional.empty();
	}
	
}
