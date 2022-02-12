package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.function.Consumer;

import javax.annotation.Nullable;

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
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;

public class LineFormation extends UnitFormation {
	
	private static final double CLOSE_ENOUGH = 0.1d; 
	
	private int formationRank;
	private int width;
	private int depth;
	
	private FormationEntityWrapper<?>[][] lines;
	
	public LineFormation(World level, int formationRank, int width, int depth) {
		super(UnitFormationTypeInit.LINE.get(), level);
		this.formationRank = formationRank;
		this.width = width;
		this.depth = depth;
		this.lines = new FormationEntityWrapper<?>[this.depth][this.width];
	}
	
	@Override
	public boolean addEntity(CreatureEntity entity) {
		if (!UnitFormation.checkMemoriesForMovement(entity) || !(entity instanceof IMovesInFormation)) return false;
		IMovesInFormation unit = (IMovesInFormation) entity;
		if (unit.isSpecialUnit() || unit.getFormationRank() != this.formationRank) return false;
		if (this.insertEntityAtBack(entity)) return true;
		this.moveUpUnits();
		return this.insertEntityAtBack(entity);
	}
	
	private boolean insertEntityAtBack(CreatureEntity entity) {
		for (int file = 0; file < this.width; ++file) {
			FormationEntityWrapper<?> wrapper = this.lines[this.depth - 1][file];
			if (UnitFormation.isSlotEmpty(wrapper)) {
				this.lines[this.depth - 1][file] = new FormationEntityWrapper<>((CreatureEntity & IMovesInFormation) entity);
				return true;
			}
		}
		return false;
	}
	
	private static final float RAD_TO_DEG = (float) Math.PI / 180.0f;

	@Override
	protected void tick(FormationLeaderEntity leader) {
		if (this.formationState == null || this.formationState == State.BROKEN || leader == null || leader.level.isClientSide) return;
		
		this.moveUpUnits();
		this.moveUpUnits();

		boolean finishedForming = this.formationState == State.FORMING;
		boolean stopped = leader.getDeltaMovement().length() < 8.0e-2d;
		
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * RAD_TO_DEG), 0.0d, MathHelper.cos(leader.yRot * RAD_TO_DEG));
		Vector3d leaderRight = new Vector3d(-leaderForward.z, 0.0d, leaderForward.x);
		Vector3d startPoint = leader.position().subtract(leaderForward).subtract(leaderRight.scale(Math.ceil((double) this.width * 0.5d)));
		
		for (int rank = 0; rank < this.depth; ++rank) {
			for (int file = 0; file < this.width; ++file) {
				FormationEntityWrapper<?> wrapper = this.lines[rank][file];
				if (UnitFormation.isSlotEmpty(wrapper)) {
					this.lines[rank][file] = null;
					continue;
				}
				CreatureEntity unit = wrapper.getEntity();
				if (!UnitFormation.checkMemoriesForMovement(unit)) {
					this.lines[rank][file] = null;
					continue;
				}
				
				if (this.formationState == State.FORMING) {
					// Move to layout spot
					Vector3d precisePos = startPoint.subtract(leaderForward.scale(rank)).add(leaderRight.scale(file)).add(0.0d, unit.getY() - startPoint.y, 0.0d);
					Vector3d possiblePos = this.tryFindingNewPosition(unit, precisePos);
					if (possiblePos == null || unit.position().closerThan(possiblePos, CLOSE_ENOUGH)) continue;
					Brain<?> brain = unit.getBrain();
					brain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(this.level.dimension(), (new BlockPos(possiblePos)).below()));
					brain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
					finishedForming = false;
				} else if (this.formationState == State.FORMED) {
					Brain<?> brain = unit.getBrain();
					Vector3d precisePos = startPoint.subtract(leaderForward.scale(rank)).add(leaderRight.scale(file)).add(0.0d, unit.getY() - startPoint.y, 0.0d);
					if (!unit.position().closerThan(precisePos, CLOSE_ENOUGH)) {
						Vector3d possiblePos = this.tryFindingNewPosition(unit, precisePos);
						if (possiblePos == null) continue;
						brain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(this.level.dimension(), (new BlockPos(possiblePos)).below()));
						brain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
					} else if (stopped && !brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) { // TODO: no action as well
						unit.yRot = leader.yRot;
						unit.yHeadRot = leader.yRot;
					}
				}
			}
		}
		
		if (finishedForming) {
			this.formationState = State.FORMED;
		}
	}
	
	private static final double[] Y_CHECKS = new double[] {0.0d, 1.0d, -1.0d};
	
	@Nullable
	private Vector3d tryFindingNewPosition(CreatureEntity unit, Vector3d precisePos) {
		for (double y : Y_CHECKS) {
			Vector3d newPos = precisePos.add(0.0d, y, 0.0d);
			if (unit.level.loadedAndEntityCanStandOn((new BlockPos(newPos)).below(), unit)) return newPos;
		}
		return null;
	}
	
	private void moveUpUnits() {
		for (int rank = 0; rank < this.depth - 1; ++rank) {
			for (int file = 0; file < this.width; ++file) {
				if (UnitFormation.isSlotEmpty(this.lines[rank][file]) && !UnitFormation.isSlotEmpty(this.lines[rank + 1][file])) {
					this.lines[rank][file] = this.lines[rank + 1][file];
					this.lines[rank + 1][file] = null;
				}
			}
		}
	}
	
	@Override
	public void executeGroupAction(int group, Consumer<CreatureEntity> action) {
		
	}
	
	@Override public float getWidth() { return this.width + 1; }
	@Override public float getDepth() { return this.depth; }

	private static final String TAG_WIDTH = "width";
	private static final String TAG_DEPTH = "depth";
	private static final String TAG_FORMATION_RANK = "formationRank";
	private static final String TAG_UNITS = "units";
	private static final String TAG_RANK = "rank";
	private static final String TAG_FILE = "file";
	private static final String TAG_UUID = "uuid";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		nbt.putInt(TAG_WIDTH, this.width);
		nbt.putInt(TAG_DEPTH, this.depth);
		nbt.putInt(TAG_FORMATION_RANK, this.formationRank);
		
		ListNBT units = new ListNBT();
		for (int rank = 0; rank < this.depth; ++rank) {
			for (int file = 0; file < this.width; ++file) {
				FormationEntityWrapper<?> wrapper = this.lines[rank][file];
				if (UnitFormation.isSlotEmpty(wrapper)) continue;
				CompoundNBT unitTag = new CompoundNBT();
				unitTag.putInt(TAG_RANK, rank);
				unitTag.putInt(TAG_FILE, file);
				unitTag.putUUID(TAG_UUID, wrapper.getEntity().getUUID());
				units.add(unitTag);
			}
		}
		nbt.put(TAG_UNITS, units);
		
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		super.deserializeNBT(nbt);
		this.width = nbt.getInt(TAG_WIDTH);
		this.depth = nbt.getInt(TAG_DEPTH);
		this.formationRank = nbt.getInt(TAG_FORMATION_RANK);
		
		this.lines = new FormationEntityWrapper<?>[this.depth][this.width];
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt) {
		if (this.level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) this.level;
		
		ListNBT units = nbt.getList(TAG_UNITS, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < units.size(); ++i) {
			CompoundNBT unitTag = units.getCompound(i);
			int rank = unitTag.getInt(TAG_RANK);
			int file = unitTag.getInt(TAG_FILE);
			if (!(0 <= rank && rank < this.depth && 0 <= file && file < this.width)) continue;
			Entity unit = slevel.getEntity(unitTag.getUUID(TAG_UUID));
			if (!(unit instanceof CreatureEntity && unit instanceof IMovesInFormation)) continue;
			this.lines[rank][file] = new FormationEntityWrapper<>((CreatureEntity & IMovesInFormation) unit);
		}
	}
	
}
