package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;

public class NoFormation extends UnitFormation {

	public NoFormation(UnitFormationType<? extends NoFormation> type, int formationRank) {
		super(type);
	}
	
	@Override public <E extends CreatureEntity & IMovesInFormation> boolean addEntity(E entity) { return false; }
	@Override public void removeEntity(CreatureEntity entity) {}
	@Override public boolean hasMatchingFormationLeader(FormationLeaderEntity inFormationWith) { return false; }
	@Override protected void tick(FormationLeaderEntity leader) {}
	@Override protected void loadEntityData(CompoundNBT nbt, World level) {}
	@Override public Vector3d getFollowPosition(FormationLeaderEntity leader) { return leader.position(); }
	@Override public float scoreOrientationAngle(float angle, World level, CreatureEntity leader, Vector3d pos) { return 0; }

}
