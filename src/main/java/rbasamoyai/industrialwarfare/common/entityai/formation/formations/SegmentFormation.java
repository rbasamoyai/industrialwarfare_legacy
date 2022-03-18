package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.IntStream;

import com.google.common.collect.Streams;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationEntityWrapper;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class SegmentFormation extends UnitFormation {
	
	private FormationLeaderEntity following;
	
	private int width;
	private int formationRank;
	
	private Float cachedAngle;
	
	private FormationEntityWrapper<?>[] units; 
	
	public SegmentFormation(UnitFormationType<? extends SegmentFormation> type) {
		this(type, -1, 0);
	}
	
	public SegmentFormation(UnitFormationType<? extends SegmentFormation> type, int formationRank, int width) {
		super(type);
		this.formationRank = formationRank;
		this.width = width;
		this.units = new FormationEntityWrapper<?>[this.width];
	}

	@Override
	public <E extends CreatureEntity & IMovesInFormation> boolean addEntity(E entity) {
		if (!entity.isLowLevelUnit() || entity.getFormationRank() != this.formationRank) return false;
		for (int file = 0; file < this.width; ++file) {
			if (this.addEntityAtFile(entity, file)) return true;
		}
		this.moveUpToNextSegment();
		for (int file = 0; file < this.width; ++file) {
			if (this.addEntityAtFile(entity, file)) return true;
		}
		return false;
	}
	
	public <E extends CreatureEntity & IMovesInFormation> boolean addEntityAtFile(E entity, int file) {
		if (file < 0 || this.width >= file || !UnitFormation.isSlotEmpty(this.units[file])) return false;
		if (!entity.isLowLevelUnit() || entity.getFormationRank() != this.formationRank) return false;
		this.units[file] = new FormationEntityWrapper<>(entity);
		return true;
	}
	
	public void setFollowing(FormationLeaderEntity following) {
		this.following = following;
	}

	@Override
	protected void tick(FormationLeaderEntity leader) {
		if (this.formationState == null || this.formationState == State.BROKEN) return;
		
		this.moveUpToNextSegment();
		
		boolean stopped = leader.getDeltaMovement().lengthSqr() < 0.0064d; // 0.08^2
		
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * RAD_TO_DEG), 0.0d, MathHelper.cos(leader.yRot * RAD_TO_DEG));
		Vector3d leaderRight = new Vector3d(-leaderForward.z, 0.0d, leaderForward.x);
		Vector3d startPoint = leader.position().subtract(leaderRight.scale(Math.ceil((double) this.width * 0.5d)));
		
		Brain<?> leaderBrain = leader.getBrain();
		
		if (!leaderBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) return;
		UUID commandGroup = leaderBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
		UUID leaderUUID = leader.getUUID();
		
		for (int file = 0; file < this.width; ++file) {
			FormationEntityWrapper<?> wrapper = this.units[file];
			if (UnitFormation.isSlotEmpty(wrapper)) {
				this.units[file] = null;
				continue;
			}
			CreatureEntity unit = wrapper.getEntity();
			if (!UnitFormation.checkMemoriesForMovement(unit)) {
				this.units[file] = null;
				continue;
			}
			
			Brain<?> unitBrain = unit.getBrain();
			
			if (!UnitFormation.checkMemoriesForSameGroup(commandGroup, unit)) {
				this.units[file] = null;
				continue;
			}
			
			unitBrain.setMemory(MemoryModuleTypeInit.IN_FORMATION.get(), leaderUUID);
			
			Vector3d precisePos = startPoint.add(leaderRight.scale(file)).add(0.0d, unit.getY() - startPoint.y, 0.0d);
			
			Vector3d possiblePos = this.tryFindingNewPosition(unit, precisePos);
			if (possiblePos == null || unit.position().closerThan(possiblePos, CLOSE_ENOUGH)) continue;
			unitBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
			unitBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()));
		}
		
		if (this.follower != null && this.follower.isAlive()) {
			if (UnitFormation.checkMemoriesForMovement(this.follower)) {
				Vector3d followPos =
						leader.position()
						.subtract(leaderForward.scale(2))
						.add(0.0d, this.follower.getY() - leader.getY(), 0.0d);
				
				boolean closeEnough = this.follower.position().closerThan(followPos, CLOSE_ENOUGH); 
				
				if (stopped && closeEnough) {
					if (this.follower instanceof FormationLeaderEntity) {
						FormationLeaderEntity followerLeader = (FormationLeaderEntity) this.follower;
						
						// No derivation from the leader angle (i.e. straight angle, 0) is preferred, hence the largest weighting
						IntStream angles = Arrays.stream(new int[] {-45, -30, -15, 0, 15, 30, 45});
						IntStream weights = Arrays.stream(new int[] {6, 7, 8, 9, 8, 7, 6});
						
						if (this.cachedAngle == null) {
							this.cachedAngle =
									Streams.zip(angles.boxed(), weights.boxed(), (angle, weight) -> {
										float angle1 = leader.yRot + angle.floatValue();
										float score = followerLeader.scoreOrientationAngle(angle1) * weight.floatValue();
										return new Tuple<>(score, angle1);
									})
									.sorted((a, b) -> -Float.compare(a.getA(), b.getA()))
									.map(Tuple::getB)
									.findFirst()
									.get();
						}
						
						followerLeader.yRot = this.cachedAngle;
						followerLeader.yHeadRot = this.cachedAngle;
					}
				} else {
					this.cachedAngle = null;
					Brain<?> followerBrain = this.follower.getBrain();
					if (followerBrain.hasMemoryValue(MemoryModuleType.MEETING_POINT) && !followerBrain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
						followerBrain.eraseMemory(MemoryModuleType.MEETING_POINT);
					} else {
						Vector3d possiblePos = this.tryFindingNewPosition(this.follower, followPos);
						if (possiblePos != null && !closeEnough) {
							followerBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
							followerBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()));
						}
					}	
				}
			} else {
				this.setFollower(null);
			}
		}
	}
	
	private void moveUpToNextSegment() {
		if (this.following == null) return;
		UnitFormation formation = this.following.getFormation();
		if (!(formation instanceof SegmentFormation)) {
			this.setFollowing(null);
			return;
		}
		
		SegmentFormation nextSegment = (SegmentFormation) formation;
		for (int file = 0; file < this.width; ++file) {
			if (!UnitFormation.isSlotEmpty(this.units[file]) && nextSegment.addEntityAtFile(this.units[file].getEntity(), file)) {
				this.units[file] = null;
			}
		}
	}

	@Override
	public float scoreOrientationAngle(float angle, World level, CreatureEntity leader) {
		return 0;
	}
	
	private static final String TAG_WIDTH = "width";
	private static final String TAG_FORMATION_RANK = "formationRank";
	private static final String TAG_FOLLOWING = "following";
	private static final String TAG_UNITS = "units";
	private static final String TAG_FILE = "file";
	private static final String TAG_UUID = "uuid";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		
		nbt.putInt(TAG_WIDTH, this.width);
		nbt.putInt(TAG_FORMATION_RANK, this.formationRank);
		if (this.following != null) {
			nbt.putUUID(TAG_FOLLOWING, this.following.getUUID());
		}
		
		ListNBT unitTags = new ListNBT();
		for (int file = 0; file < this.width; ++file) {
			if (UnitFormation.isSlotEmpty(this.units[file])) continue;
			CompoundNBT unitTag = new CompoundNBT();
			unitTag.putInt(TAG_FILE, file);
			unitTag.putUUID(TAG_UUID, this.units[file].getEntity().getUUID());
			unitTags.add(unitTag);
		}
		
		nbt.put(TAG_UNITS, unitTags);
		
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		super.deserializeNBT(nbt);
		
		this.width = nbt.getInt(TAG_WIDTH);
		this.formationRank = nbt.getInt(TAG_FORMATION_RANK);
		this.units = new FormationEntityWrapper<?>[this.width];
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt, World level) {
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
		if (nbt.hasUUID(TAG_FOLLOWING)) {
			Entity e = slevel.getEntity(nbt.getUUID(TAG_FOLLOWING));
			if (!(e instanceof FormationLeaderEntity)) return;
			this.setFollowing((FormationLeaderEntity) e);
		}
		
		if (nbt.contains(TAG_UNITS, Constants.NBT.TAG_COMPOUND)) {
			ListNBT unitTags = nbt.getList(TAG_UNITS, Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < unitTags.size(); ++i) {
				CompoundNBT unitTag = unitTags.getCompound(i);
				if (!unitTag.hasUUID(TAG_UUID)) continue;
				UUID uuid = unitTag.getUUID(TAG_UUID);
				
				Entity e = slevel.getEntity(uuid);
				if (!(e instanceof CreatureEntity && e instanceof IMovesInFormation)) continue;
				
				if (unitTag.contains(TAG_FILE)) {
					this.addEntityAtFile((CreatureEntity & IMovesInFormation) e, unitTag.getInt(TAG_FILE));
				} else {
					this.addEntity((CreatureEntity & IMovesInFormation) e);
				}
			}
		}
	}
	
}