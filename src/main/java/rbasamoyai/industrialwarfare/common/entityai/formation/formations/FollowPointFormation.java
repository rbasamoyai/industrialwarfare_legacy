package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.math.Constants;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
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
	public Vec3 getFollowPosition(FormationLeaderEntity leader) {
		Vec3 leaderForward = new Vec3(-Mth.sin(leader.getYRot() * Constants.DEG_TO_RAD), 0.0d, Mth.cos(leader.getYRot() * Constants.DEG_TO_RAD));
		Vec3 leaderRight = new Vec3(-leaderForward.z, 0.0d, leaderForward.x);
		
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
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
		CompoundTag followPointTag = new CompoundTag();
		followPointTag.putInt(TAG_X, this.followPoint.x);
		followPointTag.putInt(TAG_Z, this.followPoint.z);
		nbt.put(TAG_FOLLOW_POINT, followPointTag);
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		CompoundTag followPointTag = nbt.getCompound(TAG_FOLLOW_POINT);
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
