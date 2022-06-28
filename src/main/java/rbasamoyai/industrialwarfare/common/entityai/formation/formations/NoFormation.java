package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.MovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;

public class NoFormation extends UnitFormation {

	public NoFormation(UnitFormationType<? extends NoFormation> type, int formationRank) {
		super(type);
	}
	
	@Override public <E extends PathfinderMob & MovesInFormation> boolean addEntity(E entity) { return false; }
	@Override public void removeEntity(PathfinderMob entity) {}
	@Override public boolean hasMatchingFormationLeader(FormationLeaderEntity inFormationWith) { return false; }
	@Override protected void tick(FormationLeaderEntity leader) {}
	@Override protected void loadEntityData(CompoundTag nbt, Level level) {}
	@Override public Vec3 getFollowPosition(FormationLeaderEntity leader) { return leader.position(); }
	@Override public float scoreOrientationAngle(float angle, Level level, PathfinderMob leader, Vec3 pos) { return 0; }

}
