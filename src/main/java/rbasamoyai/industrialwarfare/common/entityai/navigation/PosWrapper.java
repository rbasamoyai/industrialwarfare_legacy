package rbasamoyai.industrialwarfare.common.entityai.navigation;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IPosWrapper;
import net.minecraft.util.math.vector.Vector3d;

public class PosWrapper implements IPosWrapper {

	private final Vector3d vec;
	private final BlockPos blockPos;
	
	public PosWrapper(IPosition pos) {
		this.vec = new Vector3d(pos.x(), pos.y(), pos.z());
		this.blockPos = new BlockPos(pos);
	}
	
	@Override public Vector3d currentPosition() { return this.vec; }
	@Override public BlockPos currentBlockPosition() { return this.blockPos; }
	@Override public boolean isVisibleBy(LivingEntity entity) { return true; }

}
