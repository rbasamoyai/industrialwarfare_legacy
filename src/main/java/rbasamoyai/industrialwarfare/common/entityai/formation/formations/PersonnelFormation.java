package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;

public class PersonnelFormation extends UnitFormation {

	private List<PersonnelPoint> personnel;
	
	public PersonnelFormation(World level) {
		super(null, level);
	}
	
	@Override
	public boolean addEntity(CreatureEntity entity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void tick(FormationLeaderEntity leader) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeGroupAction(int group, Consumer<CreatureEntity> action) {
		// TODO Auto-generated method stub

	}

	@Override
	public float getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getDepth() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public UnitFormationType<?> getType() {
		return null;
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt) {
		
	}
	
	public static class PersonnelPoint {
		public final float x;
		public final float z;
		
		public PersonnelPoint(float x, float z) {
			this.x = x;
			this.z = z;
		}
	}

}
