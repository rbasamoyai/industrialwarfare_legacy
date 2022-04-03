package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;

public class FollowPointFormation extends PointFormation {
	
	private Point followPoint;
	
	public FollowPointFormation(UnitFormationType<? extends FollowPointFormation> type, int formationRank) {
		this(type, new HashMap<>(), new ArrayList<>(), new Point(0, 0));
	}
	
	public FollowPointFormation(UnitFormationType<? extends FollowPointFormation> type, Map<Point, Integer> positions, List<UnitFormation> innerFormations, Point followPoint) {
		super(type, positions, innerFormations);
		this.followPoint = followPoint;
	}
	
	@Override
	public Vector3d getFollowPosition(FormationLeaderEntity leader) {
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * RAD_TO_DEG), 0.0d, MathHelper.cos(leader.yRot * RAD_TO_DEG));
		Vector3d leaderRight = new Vector3d(-leaderForward.z, 0.0d, leaderForward.x);
		
		return leader.position()
				.add(leaderForward.scale(this.followPoint.z))
				.add(leaderRight.scale(this.followPoint.x))
				.add(0.0d, this.follower.getY() - leader.getY(), 0.0d);
	}
	
	@Override
	public boolean hasMatchingFormationLeader(FormationLeaderEntity inFormationWith) {
		return super.hasMatchingFormationLeader(inFormationWith)
			|| this.follower instanceof FormationLeaderEntity && ((FormationLeaderEntity) this.follower).hasMatchingFormationLeader(inFormationWith);
	}
	
	private static final String TAG_FOLLOW_POINT = "followPoint";
	private static final String TAG_X = "x";
	private static final String TAG_Z = "z";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		CompoundNBT followPointTag = new CompoundNBT();
		followPointTag.putInt(TAG_X, this.followPoint.x);
		followPointTag.putInt(TAG_Z, this.followPoint.z);
		nbt.put(TAG_FOLLOW_POINT, followPointTag);
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		super.deserializeNBT(nbt);
		CompoundNBT followPointTag = nbt.getCompound(TAG_FOLLOW_POINT);
		this.followPoint = new Point(followPointTag.getInt(TAG_X), followPointTag.getInt(TAG_Z));
	}
	
	public static class Builder extends PointFormation.Builder<FollowPointFormation, Builder> {
		private final Point followPoint;
		
		public Builder(Point followPoint) {
			super();
			this.followPoint = followPoint;
		}
		
		@Override 
		public Builder getThis() {
			return this;
		}
		
		@Override
		public FollowPointFormation build() {
			return new FollowPointFormation(UnitFormationTypeInit.POINTS.get(), this.positions, this.innerFormations, this.followPoint);
		}
	}
	
}
