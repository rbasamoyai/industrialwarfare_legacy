package rbasamoyai.industrialwarfare.common.entityai.navigation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IPosWrapper;
import net.minecraft.util.math.vector.Vector3d;

public class PreciseBlockPosWrapper implements IPosWrapper {

	private final Vector3d pos;
	private final BlockPos blockPos;
	
	public PreciseBlockPosWrapper(Vector3d pos) {
		this.pos = pos;
		this.blockPos = new BlockPos(pos);
	}
	
	@Override public Vector3d currentPosition() { return this.pos; }
	@Override public BlockPos currentBlockPosition() { return this.blockPos; }
	@Override public boolean isVisibleBy(LivingEntity entity) { return true; }

}
