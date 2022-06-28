package rbasamoyai.industrialwarfare.common.entityai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

public class PosWrapper implements PositionTracker {

	private final Vec3 vec;
	private final BlockPos blockPos;
	
	public PosWrapper(Position pos) {
		this.vec = new Vec3(pos.x(), pos.y(), pos.z());
		this.blockPos = new BlockPos(pos);
	}
	
	@Override public Vec3 currentPosition() { return this.vec; }
	@Override public BlockPos currentBlockPosition() { return this.blockPos; }
	@Override public boolean isVisibleBy(LivingEntity entity) { return true; }

}
