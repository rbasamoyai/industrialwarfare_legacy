package rbasamoyai.industrialwarfare.common.tileentities;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entityai.BlockInteraction;
import rbasamoyai.industrialwarfare.common.tags.IWBlockTags;

public class TreeBlockIterator {

	private final World level;
	private final BlockPos source;
	private final AxisAlignedBB limit;
	private final List<BlockInteraction> foundInteractions = new LinkedList<>();
	private final Set<BlockPos> checkedPositions = new HashSet<>();
	
	public TreeBlockIterator(World level, BlockPos source, AxisAlignedBB limit) {
		this.level = level;
		this.source = source;
		this.limit = limit;
	}
	
	public Collection<BlockInteraction> findBlockInteractions() {
		this.foundInteractions.clear();
		this.checkedPositions.clear();
		
		if (!this.limit.contains(Vector3d.atCenterOf(this.source))) {
			return this.foundInteractions;
		}
		
		Queue<BlockPos> posToCheck = new LinkedList<>();
		posToCheck.add(this.source);
		while (!posToCheck.isEmpty()) {
			BlockPos center = posToCheck.poll();
			for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(2, 2, 2))) {
				if (World.isOutsideBuildHeight(pos) || this.checkedPositions.contains(pos)) continue;
				BlockState state = this.level.getBlockState(pos);
				BlockPos immutable = pos.immutable();
				if (!state.is(IWBlockTags.FORESTRY_HARVESTABLE)) continue;
				GlobalPos gp = GlobalPos.of(this.level.dimension(), immutable);
				int reach = Math.max(6, Math.abs(pos.distManhattan(this.source) + 1));
				this.foundInteractions.add(BlockInteraction.breakBlockAt(gp, reach));
				posToCheck.offer(immutable);
			}
			this.checkedPositions.add(center);
		}
		
		return this.foundInteractions;
	}
	
}
