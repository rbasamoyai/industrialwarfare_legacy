package rbasamoyai.industrialwarfare.common.npcprofessions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

public class NormalWorkstationProfessionComponent implements IWorkstationProfessionComponent {

	private final Set<Block> workstations;
	
	public NormalWorkstationProfessionComponent(Block workstation) {
		this(ImmutableSet.of(workstation));
	}
	
	public NormalWorkstationProfessionComponent(Set<Block> workstations) {
		this.workstations = workstations;
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

}
