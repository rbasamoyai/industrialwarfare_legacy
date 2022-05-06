package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;

public class DeferredFollowPointFormation extends PointFormation {
	
	private UnitFormation deferredFormation;
	private FormationLeaderEntity deferredLeader;
	
	public DeferredFollowPointFormation(UnitFormationType<? extends DeferredFollowPointFormation> type, int formationRank) {
		this(type, new HashMap<>(), new ArrayList<>(), new Point(0, 0), null);
	}
	
	public DeferredFollowPointFormation(UnitFormationType<? extends DeferredFollowPointFormation> type, Map<Point, Integer> positions, List<UnitFormation> innerFormations, Point deferredPoint, UnitFormation deferredFormation) {
		super(type, joinPositions(positions, deferredPoint, deferredFormation), innerFormations);
		this.deferredFormation = deferredFormation;
	}
	
	private static Map<Point, Integer> joinPositions(Map<Point, Integer> positions, Point deferredPoint, UnitFormation deferredFormation) {
		Map<Point, Integer> result = new HashMap<>();
		result.putAll(positions);
		if (deferredPoint == null || deferredFormation == null) return result;
		result.put(deferredPoint, deferredFormation.getLeaderRank());
		return result;
	}
	
	@Override
	public FormationLeaderEntity spawnInnerFormationLeaders(World level, Vector3d pos, UUID commandGroup, PlayerIDTag owner) {
		FormationLeaderEntity leader = super.spawnInnerFormationLeaders(level, pos, commandGroup, owner);
		if (this.deferredFormation != null) {
			this.deferredLeader = this.deferredFormation.spawnInnerFormationLeaders(level, pos, commandGroup, owner);
			leader.addEntity(this.deferredLeader);
		}
		return leader;
	}
	
	public void setDeferredLeader(FormationLeaderEntity entity) {
		this.deferredLeader = entity;
		this.deferredFormation = this.deferredLeader.getFormation();
	}
	
	@Override
	public void setFollower(CreatureEntity entity) {
		if (this.deferredLeader != null && this.deferredLeader.isAlive()) {
			this.deferredLeader.setFollower(entity);
		}
	}
	
	@Override
	public Vector3d getFollowPosition(FormationLeaderEntity leader) {
		return leader.position();
	}
	
	private static final String TAG_DEFERRED_LEADER = "deferredLeader";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		if (this.deferredLeader != null) {
			nbt.putUUID(TAG_DEFERRED_LEADER, this.deferredLeader.getUUID());
		}
		return nbt;
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt, World level) {
		super.loadEntityData(nbt, level);
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
		if (nbt.contains(TAG_DEFERRED_LEADER)) {
			Entity e = slevel.getEntity(nbt.getUUID(TAG_DEFERRED_LEADER));
			if (!(e instanceof FormationLeaderEntity)) return;
			this.setDeferredLeader((FormationLeaderEntity) e);
		}
	}
	
	public static class Builder extends PointFormation.Builder<DeferredFollowPointFormation, Builder> {
		private final Point deferredPoint;
		private final UnitFormation deferredFormation;
		
		public Builder(Point deferredPoint, UnitFormation deferredFormation) {
			super();
			this.deferredPoint = deferredPoint;
			this.deferredFormation = deferredFormation;
		}
		
		@Override
		public Builder addFormationPoint(Point point, UnitFormation formation) {
			return this.checkPoint(point) ? super.addFormationPoint(point, formation) : this.getThis();
		}
		
		@Override
		public Builder addRegularPoint(Point point, int rank) {
			return this.checkPoint(point) ? super.addRegularPoint(point, rank) : this.getThis();
		}
		
		private boolean checkPoint(Point point) {
			if (point.equals(this.deferredPoint)) {
				IndustrialWarfare.LOGGER.warn("Point {} is already occupied by the deferred point and will not be added!", point);
				return false;
			}
			return true;
		}
		
		@Override
		public Builder getThis() {
			return this;
		}
		
		@Override
		public DeferredFollowPointFormation build() {
			return new DeferredFollowPointFormation(UnitFormationTypeInit.DEFERRED_FOLLOW.get(), this.positions, this.innerFormations, this.deferredPoint, this.deferredFormation);
		}
		
	}
	
}
