package rbasamoyai.industrialwarfare.common.blockentities;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.common.ModTags.Blocks;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;

public class TreeBlockIterator {

	private final Level level;
	private final BlockPos source;
	private final AABB limit;
	private final List<BlockInteraction> foundInteractions = new LinkedList<>();
	private final Set<BlockPos> knownPositions = new HashSet<>();
	
	public TreeBlockIterator(Level level, BlockPos source, AABB limit) {
		this.level = level;
		this.source = source;
		this.limit = limit;
	}
	
	public Collection<BlockInteraction> findBlockInteractions() {
		this.foundInteractions.clear();
		this.knownPositions.clear();
		
		if (!this.limit.contains(Vec3.atCenterOf(this.source))) {
			return this.foundInteractions;
		}
		
		Queue<BlockPos> posToCheck = new LinkedList<>();
		posToCheck.add(this.source);
		while (!posToCheck.isEmpty()) {
			BlockPos center = posToCheck.poll();
			for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(2, 2, 2))) {
				if (this.level.isOutsideBuildHeight(pos) || !this.limit.contains(Vec3.atCenterOf(pos)) || this.knownPositions.contains(pos)) continue;
				BlockState state = this.level.getBlockState(pos);
				if (!state.is(Blocks.FORESTRY_HARVESTABLE)) continue;
				
				BlockPos immutable = pos.immutable();
				GlobalPos gp = GlobalPos.of(this.level.dimension(), immutable);
				int reach = Math.max(6, Math.abs(pos.distManhattan(this.source) + 1));
				this.foundInteractions.add(BlockInteraction.breakBlockAt(gp, reach));
				this.knownPositions.add(immutable);
				
				if (!center.equals(immutable)) {
					posToCheck.offer(immutable);
				}
			}
		}
		
		return this.foundInteractions;
	}
	
}
