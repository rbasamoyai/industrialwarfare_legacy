package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;

public class ColumnFormation extends UnitFormation {
	
	private int formationRank;
	private int width;
	private int depth;
	
	private List<SegmentFormation> innerFormations = new ArrayList<>();
	private List<FormationLeaderEntity> leaders = new ArrayList<>();
	
	public ColumnFormation(UnitFormationType<? extends ColumnFormation> type) {
		this(type, -1, 0, 0);
	}
	
	public ColumnFormation(UnitFormationType<? extends ColumnFormation> type, int formationRank, int width, int depth) {
		super(type);
		this.formationRank = formationRank;
		this.width = width;
		this.depth = depth;
	}
	
	@Override
	public void killInnerFormationLeaders() {
		this.leaders.forEach(Entity::kill);
	}
	
	@Override
	public FormationLeaderEntity spawnInnerFormationLeaders(World level, Vector3d pos, float facing, UUID commandGroup, PlayerIDTag owner) {
		FormationLeaderEntity leader = super.spawnInnerFormationLeaders(level, pos, facing, commandGroup, owner);
		
		this.innerFormations.clear();
		this.leaders.clear();
		
		this.leaders.add(leader);
		
		for (int i = 0; i < this.depth; ++i) {
			SegmentFormation inner = new SegmentFormation(UnitFormationTypeInit.COLUMN_SEGMENT.get(), this.formationRank, this.width);
			this.innerFormations.add(inner);
			this.leaders.add(inner.spawnInnerFormationLeaders(level, pos, facing, commandGroup, owner));
		}
		
		for (int i = 0; i < this.leaders.size(); ++i) {
			FormationLeaderEntity innerLeader = this.leaders.get(i);
			if (i == 0) {
				leader.setFollower(innerLeader);
			} else {
				this.leaders.get(i - 1).setFollower(innerLeader);
			}
		}
		
		return leader;
	}
	
	@Override
	public <E extends CreatureEntity & IMovesInFormation> boolean addEntity(E entity) {
		return false;
	}

	@Override
	protected void tick(FormationLeaderEntity leader) {
		
	}

	@Override
	protected void loadEntityData(CompoundNBT nbt, World level) {
		
	}

	@Override
	public float scoreOrientationAngle(float angle, World level, CreatureEntity leader) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	// Minimal data to serialize as most of the data is stored in followers
	
	private static final String TAG_WIDTH = "width";
	private static final String TAG_DEPTH = "depth";
	private static final String TAG_FORMATION_RANK = "formationRank";
	private static final String TAG_COLUMN_LEADERS = "columnLeaders";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		
		nbt.putInt(TAG_WIDTH, this.width);
		nbt.putInt(TAG_DEPTH, this.depth);
		nbt.putInt(TAG_FORMATION_RANK, this.formationRank);
		
		ListNBT columnLeaders =
				this.leaders
				.stream()
				.map(Entity::getUUID)
				.map(NBTUtil::createUUID)
				.collect(Collectors.toCollection(ListNBT::new));
		nbt.put(TAG_COLUMN_LEADERS, columnLeaders);
		
		return nbt;
	}
	
}
