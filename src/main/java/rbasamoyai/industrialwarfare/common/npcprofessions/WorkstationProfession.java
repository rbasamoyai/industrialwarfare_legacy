package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.IWorkstationDataHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.WorkAtCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;

public class WorkstationProfession extends NPCProfession {

	private final Supplier<ImmutableMap<MemoryModuleType<?>, MemoryModuleStatus>> memoryChecks;
	private final Set<Block> workstations;
	
	public WorkstationProfession(Block workstation) {
		this(ImmutableSet.of(workstation));
	}
	
	public WorkstationProfession(Set<Block> workstations) {
		this.memoryChecks = () -> ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED,
				MemoryModuleTypeInit.COMPLAINT.get(), MemoryModuleStatus.REGISTERED
				);
		this.workstations = workstations;
	}
	
	@Override
	public boolean checkMemories(NPCEntity npc) {
		Brain<?> brain = npc.getBrain();
		for (Entry<MemoryModuleType<?>, MemoryModuleStatus> e : this.memoryChecks.get().entrySet()) {
			if (!brain.checkMemory(e.getKey(), e.getValue())) return false;
		}
		return true;
	}

	@Override
	public Optional<BlockPos> getWorkingArea(World level, BlockPos pos, NPCEntity npc) {
		if (!this.workstations.contains(level.getBlockState(pos).getBlock())) return Optional.empty();
		
		List<BlockPos> positions = Arrays.asList(pos.north(), pos.east(), pos.south(), pos.west());
		return positions.stream()
					.filter(p -> level.loadedAndEntityCanStandOn(p.below(), npc))
					.filter(p -> noCollision(level, p, npc))
					.sorted((pa, pb) -> Double.compare(pa.distSqr(npc.blockPosition()), pb.distSqr(npc.blockPosition())))
					.findFirst();
	}
	
	private static boolean noCollision(World level, BlockPos pos, NPCEntity npc) {
		return level.noCollision(
				npc.getBoundingBox()
				.move(Vector3d.ZERO.subtract(npc.getPosition(1.0f)))
				.move(pos)
				.move(0.5d, 0.0d, 0.5d));
	}
	
	@Override
	public void work(World level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		// Stay at workstation to do work
		Brain<?> brain = npc.getBrain();
		BlockPos pos = order.getWrappedArg(WorkAtCommand.POS_ARG_INDEX).getPos().get();
		AxisAlignedBB box = new AxisAlignedBB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2));
		
		if (!box.contains(npc.position())) {
			if (npc.getNavigation().isDone()) {
				BlockPos cachedPos = brain.getMemory(MemoryModuleTypeInit.CACHED_POS.get()).map(GlobalPos::pos).orElse(pos.below());
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(cachedPos, 3.0f, 0));
			}
			return;
		}
		
		TileEntity te = level.getBlockEntity(pos);
		if (te == null) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
			return;
		}
		if (!(te instanceof WorkstationTileEntity)) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
			return;
		}
		WorkstationTileEntity workstation = (WorkstationTileEntity) te;
		
		LazyOptional<IWorkstationDataHandler> lzop = workstation.getDataHandler();
		if (!lzop.isPresent()) {
			brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
			return;
		}
		lzop.ifPresent(h -> {
			if (h.hasWorker()) {
				if (!h.getWorkerUUID().equals(npc.getUUID())) {
					brain.setMemoryWithExpiry(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get(), 200L);
					return;
				}
			} else {
				h.setWorkerUUID(npc.getUUID());
			}
			
			if (!brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
				brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(pos));
			}
		});
		
	}
	
	@Override
	public void stopWorking(World level, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		TileEntity te = level.getBlockEntity(order.getWrappedArg(WorkAtCommand.POS_ARG_INDEX).getPos().orElse(BlockPos.ZERO));
		if (!(te instanceof WorkstationTileEntity)) return;
		((WorkstationTileEntity) te).setRecipe(ItemStack.EMPTY, false);
	}

}
