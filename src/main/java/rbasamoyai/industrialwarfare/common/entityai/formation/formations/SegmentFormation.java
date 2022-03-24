package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
	public <E extends CreatureEntity & IMovesInFormation> boolean addEntity(E entity) {
		if (!entity.isLowLevelUnit() || entity.getFormationRank() != this.formationRank) return false;
		for (int file = 0; file < this.width; ++file) {
			if (this.addEntityAtFile(entity, file)) return true;
		}
		return false;
	}
	
	public <E extends CreatureEntity & IMovesInFormation> boolean addEntityAtFile(E entity, int file) {
		if (entity == null || file < 0 || this.width <= file || !UnitFormation.isSlotEmpty(this.units[file])) return false;
		if (!entity.isLowLevelUnit() || entity.getFormationRank() != this.formationRank) return false;
		this.units[file] = new FormationEntityWrapper<>(entity);
		return true;
	}
	
	public void removeEntityAtFile(int file) {
		if (file < 0 || this.width <= file) return;
		this.units[file] = null;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends CreatureEntity & IMovesInFormation> E getEntityAtFile(int file) {
		if (file < 0 || this.width <= file || UnitFormation.isSlotEmpty(this.units[file])) return null;
		return (E) this.units[file].getEntity();
	}

	@Override
	protected void tick(FormationLeaderEntity leader) {
		if (this.formationState == null || this.formationState == State.BROKEN) return;
		
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * RAD_TO_DEG), 0.0d, MathHelper.cos(leader.yRot * RAD_TO_DEG));
		Vector3d leaderRight = new Vector3d(-leaderForward.z, 0.0d, leaderForward.x);
		Vector3d startPoint = leader.position().subtract(leaderRight.scale(Math.ceil((double) this.width * 0.5d)));
		
		Brain<?> leaderBrain = leader.getBrain();
		
		boolean stopped = UnitFormation.isStopped(leader);
		
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
			
			if (this.formationState == State.FORMED && stopped && unit.position().closerThan(precisePos, CLOSE_ENOUGH)) {
				// Stop and stay oriented if not attacking
				unit.yRot = leader.yRot;
				unit.yHeadRot = leader.yRot;
				continue;
			}
			
			Vector3d possiblePos = this.tryFindingNewPosition(unit, precisePos);
			if (possiblePos == null || unit.position().closerThan(possiblePos, CLOSE_ENOUGH)) continue;
			unitBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
			unitBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()));
		}
	}
	
	@Override
	public Vector3d getFollowPosition(FormationLeaderEntity leader) {
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * RAD_TO_DEG), 0.0d, MathHelper.cos(leader.yRot * RAD_TO_DEG));
		Vector3d followPos = leader.position().subtract(leaderForward.scale(this.tailEnd ? 4 : 2));
		return this.follower == null ? followPos : followPos.add(0.0d, this.follower.getY() - leader.getY(), 0.0d);
	}

	@Override
	public float scoreOrientationAngle(float angle, World level, CreatureEntity leader, Vector3d pos) {
		Vector3d forward = new Vector3d(-MathHelper.sin(angle * RAD_TO_DEG), 0.0d, MathHelper.cos(angle * RAD_TO_DEG));
		Vector3d right = new Vector3d(-forward.z, 0.0d, forward.x);
		Vector3d startPoint = pos.subtract(right.scale(Math.ceil((double) this.width * 0.5d)));
		
		Stream<Integer> files = IntStream.range(0, this.width).boxed();
		
		return files.map(a -> {
			if (UnitFormation.isSlotEmpty(this.units[a])) return 0;
			CreatureEntity unit = this.units[a].getEntity();
			Vector3d unitPos = startPoint.add(right.scale(a));
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
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		
		nbt.putInt(TAG_WIDTH, this.width);
		nbt.putInt(TAG_FORMATION_RANK, this.formationRank);
		
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
		this.tailEnd = nbt.getBoolean(TAG_TAIL_END);
		this.units = new FormationEntityWrapper<?>[this.width];
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt, World level) {
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
		if (nbt.contains(TAG_UNITS, Constants.NBT.TAG_LIST)) {
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