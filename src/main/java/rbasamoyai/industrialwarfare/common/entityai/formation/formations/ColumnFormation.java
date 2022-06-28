package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.MovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
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
	public void updateOrderTime() {
		super.updateOrderTime();
		this.segmentLeaders.forEach(FormationLeaderEntity::updateOrderTime);
	}
	
	@Override
	public void setFollower(PathfinderMob entity) {
		if (this.segmentLeaders.isEmpty()) return;
		this.segmentLeaders.get(this.segmentLeaders.size() - 1).setFollower(entity);
	}
	
	@Override
	public FormationLeaderEntity spawnInnerFormationLeaders(Level level, Vec3 pos, UUID commandGroup, PlayerIDTag owner) {
		this.segmentFormations.clear();
		this.segmentLeaders.clear();
		
		for (int i = 0; i < this.depth; ++i) {
			SegmentFormation inner = new SegmentFormation(UnitFormationTypeInit.COLUMN_SEGMENT.get(), this.formationRank, this.width, i == this.depth - 1);
			this.segmentFormations.add(inner);
			this.segmentLeaders.add(inner.spawnInnerFormationLeaders(level, pos, commandGroup, owner));
		}
		
		for (int i = 0; i < this.depth; ++i) {
			if (i == 0) {
				this.follower = this.segmentLeaders.get(i); // setFollower() redirects to last entity, so directly set follower
			} else {
				this.segmentLeaders.get(i - 1).setFollower(this.segmentLeaders.get(i));
			}
		}
		
		return super.spawnInnerFormationLeaders(level, pos, commandGroup, owner);
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
	public Vec3 getFollowPosition(FormationLeaderEntity leader) {
		return leader.position();
	}
	
	@Override
	public <E extends PathfinderMob & MovesInFormation> boolean addEntity(E entity) {
		if (this.segmentLeaders.isEmpty()) return false;
		FormationLeaderEntity tail = this.segmentLeaders.get(this.segmentLeaders.size() - 1);
		if (tail.addEntity(entity)) return true;
		this.moveUpUnits();
		return tail.addEntity(entity);
	}
	
	@Override
	public void removeEntity(PathfinderMob entity) {
		if (this.segmentLeaders.isEmpty()) return;
		// Try doing this quickly by using MemoryModuleTypeInit#IN_FORMATION		
		Brain<?> brain = entity.getBrain();
		if (brain.hasMemoryValue(MemoryModuleTypeInit.IN_FORMATION.get())) {
			FormationLeaderEntity leader = brain.getMemory(MemoryModuleTypeInit.IN_FORMATION.get()).get();
			Optional<FormationLeaderEntity> optional =
					this.segmentLeaders
					.stream()
					.filter(e -> e == leader)
					.findFirst();
			
			if (optional.isPresent()) {
				optional.get().removeEntity(entity);
				return;
			}
		}
		// Else, do a full search
		for (int rank = 0; rank < this.depth; ++rank) {
			this.segmentFormations.get(rank).removeEntity(entity);
		}
	}
	
	@Override
	public boolean hasMatchingFormationLeader(FormationLeaderEntity inFormationWith) {
		return this.segmentLeaders.stream().anyMatch(f -> f.hasMatchingFormationLeader(inFormationWith));
	}

	@Override
	protected void tick(FormationLeaderEntity leader) {
		if (this.formationState == null || this.formationState == State.BROKEN) return;
		
		// Fixing units with multiple positions, since that seems to happen
		if (leader.tickCount % 20 == 0) {
			Set<PathfinderMob> units = new HashSet<>();
			for (int rank = 0; rank < this.depth; ++rank) {
				SegmentFormation segment = this.segmentFormations.get(rank);
				for (int file = 0; file < this.width; ++file) {
					PathfinderMob unit = segment.getEntityAtFile(file);
					if (unit == null) continue;
					if (units.contains(unit)) {
						segment.removeEntityAtFile(file);
					} else {
						units.add(unit);
					}
				}
			}
		}
		
		this.moveUpUnits();
	}

	@Override
	public float scoreOrientationAngle(float angle, Level level, PathfinderMob leader, Vec3 pos) {
		return this.segmentLeaders.isEmpty() ? 0.0f : this.segmentLeaders.get(0).scoreOrientationAngle(angle, pos);
	}
	
	// Minimal data to serialize as most of the data is stored in followers
	
	private static final String TAG_WIDTH = "width";
	private static final String TAG_DEPTH = "depth";
	private static final String TAG_FORMATION_RANK = "formationRank";
	private static final String TAG_COLUMN_SEGMENTS = "columnSegments";
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
		
		nbt.putInt(TAG_WIDTH, this.width);
		nbt.putInt(TAG_DEPTH, this.depth);
		nbt.putInt(TAG_FORMATION_RANK, this.formationRank);
		
		ListTag columnSegments =
				this.segmentLeaders
				.stream()
				.map(Entity::getUUID)
				.map(NbtUtils::createUUID)
				.collect(Collectors.toCollection(ListTag::new));
		nbt.put(TAG_COLUMN_SEGMENTS, columnSegments);
		
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		
		this.width = nbt.getInt(TAG_WIDTH);
		this.depth = nbt.getInt(TAG_DEPTH);
		this.formationRank = nbt.getInt(TAG_FORMATION_RANK);
	}
	
	@Override
	protected void loadEntityData(CompoundTag nbt, Level level) {
		if (level.isClientSide) return;
		ServerLevel slevel = (ServerLevel) level;
		
		this.segmentLeaders.clear();
		
		ListTag columnSegments = nbt.getList(TAG_COLUMN_SEGMENTS, Tag.TAG_INT_ARRAY);
		columnSegments
		.stream()
		.map(NbtUtils::loadUUID)
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
	protected void loadFollowerData(CompoundTag nbt, Level level) {
		if (level.isClientSide) return;
		ServerLevel slevel = (ServerLevel) level;
		
		if (nbt.hasUUID(TAG_FOLLOWER)) {
			Entity e = slevel.getEntity(nbt.getUUID(TAG_FOLLOWER));
			if (!(e instanceof PathfinderMob)) return;
			this.follower = (PathfinderMob) e; // Direct setting due to setFollower() override
		}
	}
	
}
