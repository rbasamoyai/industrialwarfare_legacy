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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.common.capabilities.tileentities.workstation.IWorkstationDataHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.WorkAtCommand;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.NPCComplaintInit;

public class WorkstationWorkUnit implements IWorkUnit {

	private final Supplier<ImmutableMap<MemoryModuleType<?>, MemoryModuleStatus>> memoryChecks;
	private final Set<Block> workstations;
	
	public WorkstationWorkUnit(Block workstation) {
		this(ImmutableSet.of(workstation));
	}
	
	public WorkstationWorkUnit(Set<Block> workstations) {
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
	public Optional<BlockPos> getWorkingArea(World world, BlockPos pos, NPCEntity npc) {
		if (!this.workstations.contains(world.getBlockState(pos).getBlock())) return Optional.empty();
		
		List<BlockPos> positions = Arrays.asList(pos.below().north(), pos.below().east(), pos.below().south(), pos.below().west());
		return positions.stream()
					.filter(p -> world.loadedAndEntityCanStandOn(p, npc))
					.filter(p -> world.noCollision(npc))
					.sorted((pa, pb) -> Double.compare(pa.distSqr(npc.blockPosition()), pb.distSqr(npc.blockPosition())))
					.findFirst();
	}
	
	@Override
	public void work(World world, NPCEntity npc, long gameTime, TaskScrollOrder order) {
		// Stay at workstation to do work
		Brain<?> brain = npc.getBrain();
		BlockPos pos = order.getWrappedArg(WorkAtCommand.POS_ARG_INDEX).getPos().get();
		
		TileEntity te = world.getBlockEntity(pos);
		if (te == null) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get());
			return;
		}
		if (!(te instanceof WorkstationTileEntity)) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get());
			return;
		}
		WorkstationTileEntity workstation = (WorkstationTileEntity) te;
		
		LazyOptional<IWorkstationDataHandler> lzop = workstation.getDataHandler();
		if (!lzop.isPresent()) {
			brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get());
			return;
		}
		IWorkstationDataHandler handler = lzop.resolve().get();
		
		if (handler.hasWorker()) {
			if (!handler.getWorkerUUID().equals(npc.getUUID())) {
				brain.setMemory(MemoryModuleTypeInit.COMPLAINT.get(), NPCComplaintInit.INVALID_WORKSTATION.get());
				return;
			}
		} else {
			handler.setWorkerUUID(npc.getUUID());
		}
		
		if (!brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
			brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(pos));
		}
	}

}
