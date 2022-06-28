package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.common.blockentities.ManufacturingBlockEntity;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.WorkAtCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;

public class WorkstationProfession extends NPCProfession {

	private final Supplier<ImmutableMap<MemoryModuleType<?>, MemoryStatus>> memoryChecks;
	private final Set<Block> workstations;
	
	public WorkstationProfession(Block workstation) {
		this(ImmutableSet.of(workstation));
	}
	
	public WorkstationProfession(Set<Block> workstations) {
		this.memoryChecks = () -> ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
				MemoryModuleTypeInit.COMPLAINT.get(), MemoryStatus.REGISTERED
				);
		this.workstations = workstations;
	}
	
	@Override
	public boolean checkMemories(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		for (Entry<MemoryModuleType<?>, MemoryStatus> e : this.memoryChecks.get().entrySet()) {
			if (!brain.checkMemory(e.getKey(), e.getValue())) return false;
		}
		return true;
	}

	@Override
	public Optional<BlockPos> getWorkingArea(Level level, BlockPos pos, NPCEntity npc) {
		if (!this.workstations.contains(level.getBlockState(pos).getBlock())) return Optional.empty();
		
		List<BlockPos> positions = Arrays.asList(pos.north(), pos.east(), pos.south(), pos.west());
		return positions.stream()
					.filter(p -> level.loadedAndEntityCanStandOn(p.below(), npc))
					.filter(p -> noCollision(level, p, npc))
					.sorted((pa, pb) -> Double.compare(pa.distSqr(npc.blockPosition()), pb.distSqr(npc.blockPosition())))
					.findFirst();
	}
	
	private static boolean noCollision(Level level, BlockPos pos, NPCEntity npc) {
		return level.noCollision(
				npc.getBoundingBox()
				.move(Vec3.ZERO.subtract(npc.position()))
				.move(Vec3.atCenterOf(pos)));
	}
	
	@Override
	public void work(Level level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		// Stay at workstation to do work
		Brain<?> brain = npc.getBrain();
		BlockPos pos = order.getWrappedArg(WorkAtCommand.POS_ARG_INDEX).getPos().get();
		AABB box = new AABB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2));
		
		if (!box.contains(npc.position())) {
			if (npc.getNavigation().isDone()) {
				BlockPos cachedPos = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get()).map(GlobalPos::pos).orElse(pos.below());
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(cachedPos, 3.0f, 0));
			}
			return;
		}
		
		BlockEntity te = level.getBlockEntity(pos);
		if (te == null) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
			return;
		}
		if (!(te instanceof ManufacturingBlockEntity)) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
			return;
		}
		ManufacturingBlockEntity workstation = (ManufacturingBlockEntity) te;
		
		if (workstation.hasWorker() && !workstation.isSameWorker(npc)) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
			return;
		}
		workstation.setWorker(npc);
		if (!brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
			brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
		}
	}
	
	@Override
	public void stopWorking(Level level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		BlockEntity te = level.getBlockEntity(order.getWrappedArg(WorkAtCommand.POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO));
		if (!(te instanceof ManufacturingBlockEntity)) return;
		((ManufacturingBlockEntity) te).setRecipe(ItemStack.EMPTY, false);
	}

}
