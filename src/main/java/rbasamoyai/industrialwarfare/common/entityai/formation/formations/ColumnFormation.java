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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;

public class ColumnFormation extends UnitFormation {
	
	private int formationRank;
	private int width;
	private int depth;
	
	private final List<SegmentFormation> segmentFormations = new ArrayList<>();
	private final List<FormationLeaderEntity> segmentLeaders = new ArrayList<>();
	
	public ColumnFormation(UnitFormationType<? extends ColumnFormation> type, int formationRank) {
		this(type, formationRank, 0, 0);
	}
	
	public ColumnFormation(UnitFormationType<? extends ColumnFormation> type, int formationRank, int width, int depth) {
		super(type);
		this.formationRank = formationRank;
		this.width = width;
		this.depth = depth;
	}
	
	@Override
	public void killInnerFormationLeaders() {
		this.segmentLeaders.forEach(Entity::kill);
	}
	
	@Override
	public void setFollower(CreatureEntity entity) {
		if (this.segmentLeaders.isEmpty()) return;
		this.segmentLeaders.get(this.segmentLeaders.size() - 1).setFollower(entity);
	}
	
	@Override
	public FormationLeaderEntity spawnInnerFormationLeaders(World level, Vector3d pos, float facing, UUID commandGroup, PlayerIDTag owner) {
		this.segmentFormations.clear();
		this.segmentLeaders.clear();
		
		for (int i = 0; i < this.depth; ++i) {
			SegmentFormation inner = new SegmentFormation(UnitFormationTypeInit.COLUMN_SEGMENT.get(), this.formationRank, this.width, i == this.depth - 1);
			this.segmentFormations.add(inner);
			this.segmentLeaders.add(inner.spawnInnerFormationLeaders(level, pos, facing, commandGroup, owner));
		}
		
		for (int i = 0; i < this.depth; ++i) {
			if (i == 0) {
				this.follower = this.segmentLeaders.get(i); // setFollower() redirects to last entity, so directly set follower
			} else {
				this.segmentLeaders.get(i - 1).setFollower(this.segmentLeaders.get(i));
			}
		}
		
		return super.spawnInnerFormationLeaders(level, pos, facing, commandGroup, owner);
	}
	
	private void moveUpUnits() {
		if (this.segmentFormations.size() < 2) return;
		for (int rank = 1; rank < this.depth; ++rank) {
			for (int file = 0; file < this.width; ++file) {
				SegmentFormation backRank = this.segmentFormations.get(rank);
				if (this.segmentFormations.get(rank - 1).addEntityAtFile(backRank.getEntityAtFile(file), file)) {
					backRank.removeEntityAtFile(file);
				}
			}
		}
	}
	
	@Override
	public Vector3d getFollowPosition(FormationLeaderEntity leader) {
		return leader.position();
	}
	
	@Override
	public <E extends CreatureEntity & IMovesInFormation> boolean addEntity(E entity) {
		if (this.segmentLeaders.isEmpty()) return false;
		FormationLeaderEntity tail = this.segmentLeaders.get(this.segmentLeaders.size() - 1);
		if (tail.addEntity(entity)) return true;
		this.moveUpUnits();
		return tail.addEntity(entity);
	}

	@Override
	protected void tick(FormationLeaderEntity leader) {
		if (this.formationState == null || this.formationState == State.BROKEN) return;
		
		this.moveUpUnits();
	}

	@Override
	public float scoreOrientationAngle(float angle, World level, CreatureEntity leader, Vector3d pos) {
		return this.segmentLeaders.isEmpty() ? 0.0f : this.segmentLeaders.get(0).scoreOrientationAngle(angle, pos);
	}
	
	// Minimal data to serialize as most of the data is stored in followers
	
	private static final String TAG_WIDTH = "width";
	private static final String TAG_DEPTH = "depth";
	private static final String TAG_FORMATION_RANK = "formationRank";
	private static final String TAG_COLUMN_SEGMENTS = "columnSegments";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		
		nbt.putInt(TAG_WIDTH, this.width);
		nbt.putInt(TAG_DEPTH, this.depth);
		nbt.putInt(TAG_FORMATION_RANK, this.formationRank);
		
		ListNBT columnSegments =
				this.segmentLeaders
				.stream()
				.map(Entity::getUUID)
				.map(NBTUtil::createUUID)
				.collect(Collectors.toCollection(ListNBT::new));
		nbt.put(TAG_COLUMN_SEGMENTS, columnSegments);
		
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		super.deserializeNBT(nbt);
		
		this.width = nbt.getInt(TAG_WIDTH);
		this.depth = nbt.getInt(TAG_DEPTH);
		this.formationRank = nbt.getInt(TAG_FORMATION_RANK);
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt, World level) {
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
		this.segmentLeaders.clear();
		
		ListNBT columnSegments = nbt.getList(TAG_COLUMN_SEGMENTS, Constants.NBT.TAG_INT_ARRAY);
		columnSegments
		.stream()
		.map(NBTUtil::loadUUID)
		.map(slevel::getEntity)
		.filter(e -> e instanceof FormationLeaderEntity)
		.map(e -> (FormationLeaderEntity) e)
		.filter(e -> e.getFormation() instanceof SegmentFormation)
		.forEach(this.segmentLeaders::add);
		
		this.segmentFormations.clear();
		
		this.segmentLeaders
		.stream()
		.map(FormationLeaderEntity::getFormation)
		.map(f -> (SegmentFormation) f)
		.forEach(this.segmentFormations::add);
	}
	
	private static final String TAG_FOLLOWER = UnitFormation.TAG_FOLLOWER;
	
	@Override
	protected void loadFollowerData(CompoundNBT nbt, World level) {
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
		if (nbt.hasUUID(TAG_FOLLOWER)) {
			Entity e = slevel.getEntity(nbt.getUUID(TAG_FOLLOWER));
			if (!(e instanceof CreatureEntity)) return;
			this.follower = (CreatureEntity) e; // Direct setting due to setFollower() override
		}
	}
	
}
