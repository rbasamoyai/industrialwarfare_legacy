package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.math.Constants;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationEntityWrapper;
import rbasamoyai.industrialwarfare.common.entityai.formation.MovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class SegmentFormation extends UnitFormation {
	
	private int width;
	private int formationRank;
	private boolean tailEnd;
	
	private FormationEntityWrapper<?>[] units; 
	
	public SegmentFormation(UnitFormationType<? extends SegmentFormation> type, int formationRank) {
		this(type, formationRank, 0, false);
	}
	
	public SegmentFormation(UnitFormationType<? extends SegmentFormation> type, int formationRank, int width, boolean tailEnd) {
		super(type);
		this.formationRank = formationRank;
		this.width = width;
		this.units = new FormationEntityWrapper<?>[this.width];
		this.tailEnd = tailEnd;
	}

	@Override
	public <E extends PathfinderMob & MovesInFormation> boolean addEntity(E entity) {
		if (!entity.isLowLevelUnit() || entity.getFormationRank() != this.formationRank) return false;
		for (int file = 0; file < this.width; ++file) {
			if (this.addEntityAtFile(entity, file)) return true;
		}
		return false;
	}
	
	public <E extends PathfinderMob & MovesInFormation> boolean addEntityAtFile(E entity, int file) {
		if (entity == null || file < 0 || this.width <= file || !UnitFormation.isSlotEmpty(this.units[file])) return false;
		if (!entity.isLowLevelUnit() || entity.getFormationRank() != this.formationRank) return false;
		this.units[file] = new FormationEntityWrapper<>(entity);
		return true;
	}
	
	@Override
	public void removeEntity(PathfinderMob entity) {
		for (int file = 0; file < this.width; ++file) {
			if (UnitFormation.isSlotEmpty(this.units[file]) || this.units[file].getEntity() != entity) continue;
			this.units[file] = null;
			return;
		}
	}
	
	@Override
	public boolean hasMatchingFormationLeader(FormationLeaderEntity inFormationWith) {
		return false;
	}
	
	public void removeEntityAtFile(int file) {
		if (file < 0 || this.width <= file) return;
		this.units[file] = null;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends PathfinderMob & MovesInFormation> E getEntityAtFile(int file) {
		if (file < 0 || this.width <= file || UnitFormation.isSlotEmpty(this.units[file])) return null;
		return (E) this.units[file].getEntity();
	}

	@Override
	protected void tick(FormationLeaderEntity leader) {
		if (this.formationState == null || this.formationState == State.BROKEN) return;
		
		Vec3 leaderForward = new Vec3(-Mth.sin(leader.getYRot() * Constants.DEG_TO_RAD), 0.0d, Mth.cos(leader.getYRot() * Constants.DEG_TO_RAD));
		Vec3 leaderRight = new Vec3(-leaderForward.z, 0.0d, leaderForward.x);
		Vec3 startPoint = leader.position().subtract(leaderRight.scale(Math.ceil((double) this.width * 0.5d)));
		
		Brain<?> leaderBrain = leader.getBrain();
		
		boolean stopped = UnitFormation.isStopped(leader);
		
		if (!leaderBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) return;
		UUID commandGroup = leaderBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
		
		for (int file = 0; file < this.width; ++file) {
			FormationEntityWrapper<?> wrapper = this.units[file];
			if (UnitFormation.isSlotEmpty(wrapper)) {
				this.units[file] = null;
				continue;
			}
			PathfinderMob unit = wrapper.getEntity();
			if (!UnitFormation.checkMemoriesForMovement(unit)) {
				this.units[file] = null;
				continue;
			}
			
			Brain<?> unitBrain = unit.getBrain();
			
			if (!UnitFormation.checkMemoriesForSameGroup(commandGroup, unit)) {
				this.units[file] = null;
				continue;
			}
			
			unitBrain.setMemory(MemoryModuleTypeInit.IN_FORMATION.get(), leader);
			
			Vec3 precisePos = startPoint.add(leaderRight.scale(file)).add(0.0d, unit.getY() - startPoint.y, 0.0d);
			
			if (this.formationState == State.FORMED && stopped && unit.position().closerThan(precisePos, CLOSE_ENOUGH)) {
				// Stop and stay oriented if not attacking
				unit.setYRot(leader.getYRot());
				unit.yHeadRot = leader.getYRot();
				continue;
			}
			
			Vec3 possiblePos = this.tryFindingNewPosition(unit, precisePos);
			if (possiblePos == null || unit.position().closerThan(possiblePos, CLOSE_ENOUGH)) continue;
			unitBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
			unitBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()));
		}
	}
	
	@Override
	protected void tickFollower(FormationLeaderEntity leader) {
		if (this.follower == null) return;
		if (!this.follower.isAlive() || !checkMemoriesForMovement(this.follower)) {
			this.follower = null;
			return;
		} 
		
		Vec3 leaderForward = new Vec3(-Mth.sin(leader.getYRot() * Constants.DEG_TO_RAD), 0.0d, Mth.cos(leader.getYRot() * Constants.DEG_TO_RAD));
		
		double halfLength = this.tailEnd ? 2.0d : 1.0d;
		Vec3 joint = leader.position().subtract(leaderForward.scale(halfLength));
		Vec3 secondSegment = joint.subtract(this.follower.position()).normalize();
		this.cachedAngle = (float) -Math.toDegrees(Mth.atan2(secondSegment.x, secondSegment.z));
		
		super.tickFollower(leader);
	}
	
	@Override
	public Vec3 getFollowPosition(FormationLeaderEntity leader) {
		Vec3 leaderForward = new Vec3(-Mth.sin(leader.getYRot() * Constants.DEG_TO_RAD), 0.0d, Mth.cos(leader.getYRot() * Constants.DEG_TO_RAD));
		
		if (this.follower == null) {
			return leader.position().subtract(leaderForward.scale(this.tailEnd ? 4.0d : 2.0d));
		}

		double halfLength = this.tailEnd ? 2.0d : 1.0d;
		Vec3 joint = leader.position().subtract(leaderForward.scale(halfLength));
		Vec3 secondSegment = this.follower.position().subtract(joint).normalize().scale(halfLength);
		
		if (!leader.level.isClientSide) {
			((ServerLevel) leader.level).sendParticles(new DustParticleOptions(new Vector3f(0.0f, 0.0f, 1.0f), 1.0f), joint.x, joint.y + 2.5d, joint.z, 1, 0.0d, 0.0d, 0.0d, 0.0d);
		}
		
		float secondSegAngle = (float) Math.toDegrees(Mth.atan2(secondSegment.x, secondSegment.z));
		float angularDiff = (secondSegAngle - leader.getYRot() + 180.0f) % 360.0f - 180.0f;
		
		if (angularDiff <= -135.0f || angularDiff >= 135.0f) {
			return joint.add(secondSegment);
		}
		
		float angularDiff1 = secondSegAngle - leader.getYRot() % 180.0f;
		if (angularDiff1 < -180.0f) {
			angularDiff1 += 360.0f;
		}
		if (angularDiff1 > 180.0f) {
			angularDiff1 -= 360.0f;
		}
		
		float newRot = leader.getYRot() + (angularDiff < 0.0f ? -135.0f : 135.0f);
		Vec3 clampedSecondSegment = new Vec3(-Mth.sin(newRot * Constants.DEG_TO_RAD), 0.0d, Mth.cos(newRot * Constants.DEG_TO_RAD));
		return joint.add(clampedSecondSegment);
	}

	@Override
	public float scoreOrientationAngle(float angle, Level level, PathfinderMob leader, Vec3 pos) {
		Vec3 forward = new Vec3(-Mth.sin(angle * Constants.DEG_TO_RAD), 0.0d, Mth.cos(angle * Constants.DEG_TO_RAD));
		Vec3 right = new Vec3(-forward.z, 0.0d, forward.x);
		Vec3 startPoint = pos.subtract(right.scale(Math.ceil((double) this.width * 0.5d)));
		
		Stream<Integer> files = IntStream.range(0, this.width).boxed();
		
		return files.map(a -> {
			if (UnitFormation.isSlotEmpty(this.units[a])) return 0;
			PathfinderMob unit = this.units[a].getEntity();
			Vec3 unitPos = startPoint.add(right.scale(a));
			BlockPos blockPos = (new BlockPos(unitPos)).below();
			return level.loadedAndEntityCanStandOn(blockPos, unit) && level.noCollision(unit, unit.getBoundingBox().move(unitPos)) ? 1 : 0;
		}).reduce(Integer::sum).get();
	}
	
	private static final String TAG_WIDTH = "width";
	private static final String TAG_FORMATION_RANK = "formationRank";
	private static final String TAG_TAIL_END = "isTailEnd";
	private static final String TAG_UNITS = "units";
	private static final String TAG_FILE = "file";
	private static final String TAG_UUID = "uuid";
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
		
		nbt.putInt(TAG_WIDTH, this.width);
		nbt.putInt(TAG_FORMATION_RANK, this.formationRank);
		nbt.putBoolean(TAG_TAIL_END, this.tailEnd);
		
		ListTag unitTags = new ListTag();
		for (int file = 0; file < this.width; ++file) {
			if (UnitFormation.isSlotEmpty(this.units[file])) continue;
			CompoundTag unitTag = new CompoundTag();
			unitTag.putInt(TAG_FILE, file);
			unitTag.putUUID(TAG_UUID, this.units[file].getEntity().getUUID());
			unitTags.add(unitTag);
		}
		
		nbt.put(TAG_UNITS, unitTags);
		
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		
		this.width = nbt.getInt(TAG_WIDTH);
		this.formationRank = nbt.getInt(TAG_FORMATION_RANK);
		this.tailEnd = nbt.getBoolean(TAG_TAIL_END);
		this.units = new FormationEntityWrapper<?>[this.width];
	}
	
	@Override
	protected void loadEntityData(CompoundTag nbt, Level level) {
		if (level.isClientSide) return;
		ServerLevel slevel = (ServerLevel) level;
		
		if (nbt.contains(TAG_UNITS, Tag.TAG_LIST)) {
			ListTag unitTags = nbt.getList(TAG_UNITS, Tag.TAG_COMPOUND);
			for (int i = 0; i < unitTags.size(); ++i) {
				CompoundTag unitTag = unitTags.getCompound(i);
				if (!unitTag.hasUUID(TAG_UUID)) continue;
				UUID uuid = unitTag.getUUID(TAG_UUID);
				
				Entity e = slevel.getEntity(uuid);
				if (!(e instanceof PathfinderMob && e instanceof MovesInFormation)) continue;
				
				if (unitTag.contains(TAG_FILE)) {
					this.addEntityAtFile((PathfinderMob & MovesInFormation) e, unitTag.getInt(TAG_FILE));
				} else {
					this.addEntity((PathfinderMob & MovesInFormation) e);
				}
			}
		}
	}
	
}