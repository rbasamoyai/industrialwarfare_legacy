package rbasamoyai.industrialwarfare.common.entityai;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.InteractWithDoorTask;
import net.minecraft.entity.ai.brain.task.LookAtEntityTask;
import net.minecraft.entity.ai.brain.task.LookTask;
import net.minecraft.entity.ai.brain.task.SwimTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.WalkToTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsPosTask;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.tasks.GoToWorkTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.LeaveWorkTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.RunCommandFromTaskScrollTask;

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
				Pair.of(1, new LookAtEntityTask(4.0f)),
				Pair.of(2, new WalkTowardsLookTargetTask(2.5f, 2))
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getWorkPackage() {
		return ImmutableList.of(
				Pair.of(1, new RunCommandFromTaskScrollTask())
				);
	}
	
	public static ImmutableList<Pair<Integer, ? extends Task<? super NPCEntity>>> getRestPackage() {
		return ImmutableList.of(Pair.of(0, new WalkTowardsPosTask(MemoryModuleType.HOME, 2.5f, 1, 100)));
	}
	
}
