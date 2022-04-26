package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.UUID;
import java.util.stream.IntStream;

import com.google.common.collect.Streams;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
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
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationEntityWrapper;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.FormationAttackTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class LineFormation extends UnitFormation {
	
	private int formationRank;
	private int width;
	private int depth;
	private int followSpacing;
	
	private FormationEntityWrapper<?>[][] lines;
	
	public LineFormation(UnitFormationType<? extends LineFormation> type, int formationRank) {
		this(type, formationRank, 0, 0, 0);
	}
	
	public LineFormation(UnitFormationType<? extends LineFormation> type, int formationRank, int width, int depth) {
		this(type, formationRank, width, depth, 3);
	}
	
	public LineFormation(UnitFormationType<? extends LineFormation> type, int formationRank, int width, int depth, int followSpacing) {
		super(type);
		this.formationRank = formationRank;
		this.width = width;
		this.depth = depth;
		this.followSpacing = followSpacing;
		this.lines = new FormationEntityWrapper<?>[this.depth][this.width];
	}
	
	@Override
	public <E extends CreatureEntity & IMovesInFormation> boolean addEntity(E entity) {
		if (!entity.isLowLevelUnit() || entity.getFormationRank() != this.formationRank) return false;
		if (this.insertEntityAtBack(entity)) return true;
		this.moveUpUnits();
		return this.insertEntityAtBack(entity);
	}
	
	private <E extends CreatureEntity & IMovesInFormation> boolean insertEntityAtBack(E entity) {
		for (int file = 0; file < this.width; ++file) {
			FormationEntityWrapper<?> wrapper = this.lines[this.depth - 1][file];
			if (UnitFormation.isSlotEmpty(wrapper)) {
				this.lines[this.depth - 1][file] = new FormationEntityWrapper<>(entity);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void removeEntity(CreatureEntity entity) {
		for (int rank = 0; rank < this.depth; ++rank) {
			for (int file = 0; file < this.width; ++file) {
				if (UnitFormation.isSlotEmpty(this.lines[rank][file]) || this.lines[rank][file].getEntity() != entity) continue;
				this.lines[rank][file] = null;
				return;
			}
		}
	}
	
	@Override
	public boolean hasMatchingFormationLeader(FormationLeaderEntity inFormationWith) {
		return false;
	}

	@Override
	protected void tick(FormationLeaderEntity leader) {
		if (this.formationState == null || this.formationState == State.BROKEN) return;
		
		this.moveUpUnits();

		boolean stopped = UnitFormation.isStopped(leader);
		
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * DEG_TO_RAD), 0.0d, MathHelper.cos(leader.yRot * DEG_TO_RAD));
		Vector3d leaderRight = new Vector3d(-leaderForward.z, 0.0d, leaderForward.x);
		Vector3d startPoint = leader.position().subtract(leaderRight.scale(Math.ceil((double) this.width * 0.5d) - 1));
		
		Brain<?> leaderBrain = leader.getBrain();
		
		if (!leaderBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) return;
		UUID commandGroup = leaderBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
		
		boolean engagementFlag =
				leaderBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
				&& leaderBrain.hasMemoryValue(MemoryModuleTypeInit.ENGAGING_COMPLETED.get())
				&& leaderBrain.getMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get()).get();
		
		CombatMode combatMode = leaderBrain.getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).orElse(CombatMode.DONT_ATTACK);
		
		LivingEntity target = engagementFlag ? leaderBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get() : null;
		engagementFlag &= target != null && target.isAlive() && combatMode != CombatMode.DONT_ATTACK;
		
		boolean finishedForming = this.formationState == State.FORMING && !engagementFlag;
		
		int period = MathHelper.floor((double) leader.tickCount / (double) this.interval.getTime());
		
		int currentRank = period % (this.depth == 0 ? 1 : this.depth);
		int currentFile = period % (this.width == 0 ? 1 : this.width);
		boolean newPeriod = leader.tickCount % this.interval.getTime() == 0;
		
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
				
				Brain<?> unitBrain = unit.getBrain();
				
				if (!UnitFormation.checkMemoriesForSameGroup(commandGroup, unit)) {
					this.lines[rank][file] = null;
					continue;
				}
				
				unitBrain.setMemory(MemoryModuleTypeInit.IN_FORMATION.get(), leader);
				
				if (unitBrain.checkMemory(MemoryModuleTypeInit.CAN_ATTACK.get(), MemoryModuleStatus.VALUE_ABSENT)) {
					if (this.attackType == FormationAttackTypeInit.FIRE_AT_WILL.get()) {
						unitBrain.eraseMemory(MemoryModuleTypeInit.FINISHED_ATTACKING.get());
						unitBrain.setMemoryWithExpiry(MemoryModuleTypeInit.CAN_ATTACK.get(), true, 80L);
					} else if (newPeriod) {
						if (this.attackType == FormationAttackTypeInit.FIRE_BY_RANK.get() && rank == currentRank) {
							unitBrain.eraseMemory(MemoryModuleTypeInit.FINISHED_ATTACKING.get());
							unitBrain.setMemoryWithExpiry(MemoryModuleTypeInit.CAN_ATTACK.get(), false, 80L);
						} else if (this.attackType == FormationAttackTypeInit.FIRE_BY_FILE.get() && file == currentFile) {
							unitBrain.eraseMemory(MemoryModuleTypeInit.FINISHED_ATTACKING.get());
							unitBrain.setMemoryWithExpiry(MemoryModuleTypeInit.CAN_ATTACK.get(), false, 80L);
						} else if (this.attackType == FormationAttackTypeInit.FIRE_BY_COMPANY.get()) {
							unitBrain.eraseMemory(MemoryModuleTypeInit.FINISHED_ATTACKING.get());
							unitBrain.setMemoryWithExpiry(MemoryModuleTypeInit.CAN_ATTACK.get(), false, 80L);
						}
					}
				} else if (unitBrain.checkMemory(MemoryModuleTypeInit.CAN_ATTACK.get(), MemoryModuleStatus.REGISTERED)) {
					if (this.attackType == FormationAttackTypeInit.NO_ATTACK.get() || !engagementFlag) {
						unitBrain.eraseMemory(MemoryModuleTypeInit.CAN_ATTACK.get());
					}
				}
				
				Vector3d firingPos =
						startPoint
						.add(leaderForward)
						.add(leaderRight.scale(file))
						.add(0.0d, unit.getEyeHeight(), 0.0d);				
				
				if (engagementFlag && UnitFormation.checkMemoriesForEngagement(unit)) {
					// Engagement
					if (unitBrain.getActiveNonCoreActivity().map(a -> a != Activity.FIGHT).orElse(true)) {
						unitBrain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), ActivityStatus.FIGHTING);
						unitBrain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), combatMode);
						unitBrain.setActiveActivityIfPossible(Activity.FIGHT);
					}
					
					if (unit instanceof IWeaponRangedAttackMob
						&& UnitFormation.canDoRangedAttack((CreatureEntity & IWeaponRangedAttackMob) unit, firingPos, MemoryModuleTypeInit.SHOOTING_POS.get())) {
						if (unitBrain.hasMemoryValue(MemoryModuleTypeInit.FINISHED_ATTACKING.get())) {
							unitBrain.eraseMemory(MemoryModuleTypeInit.SHOOTING_POS.get());
						} else if (unitBrain.hasMemoryValue(MemoryModuleTypeInit.CAN_ATTACK.get())) {
							unitBrain.setMemoryWithExpiry(MemoryModuleTypeInit.SHOOTING_POS.get(), firingPos, 40L);
						}
					} else {
						unitBrain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
						continue;
					}
					
				} else if (unitBrain.hasMemoryValue(MemoryModuleTypeInit.FINISHED_ATTACKING.get())
						&& unitBrain.checkMemory(MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get(), MemoryModuleStatus.VALUE_ABSENT)) {
					unitBrain.setMemory(MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get(), true);
				}
				
				Vector3d precisePos =
						startPoint
						.subtract(leaderForward.scale(rank))
						.add(leaderRight.scale(file))
						.add(0.0d, unit.getY() - startPoint.y, 0.0d);
				
				if (this.formationState == State.FORMED && stopped && unit.position().closerThan(precisePos, CLOSE_ENOUGH)) {
					// Stop and stay oriented if not attacking
					unit.yRot = leader.yRot;
					unit.yHeadRot = leader.yRot;
					continue;
				}
				
				// Position movement
				Vector3d possiblePos = this.tryFindingNewPosition(unit, precisePos);
				if (possiblePos == null || unit.position().closerThan(possiblePos, CLOSE_ENOUGH)) continue;
				unitBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
				unitBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()));
			}
		}
		
		if (finishedForming) {
			this.formationState = State.FORMED;
		}
	}
	
	@Override
	public Vector3d getFollowPosition(FormationLeaderEntity leader) {
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * DEG_TO_RAD), 0.0d, MathHelper.cos(leader.yRot * DEG_TO_RAD));
		return leader.position()
				.subtract(leaderForward.scale(this.depth + this.followSpacing))
				.add(0.0d, this.follower.getY() - leader.getY(), 0.0d);
	}
	
	@Override
	public float scoreOrientationAngle(float angle, World level, CreatureEntity leader, Vector3d pos) {
		Vector3d forward = new Vector3d(-MathHelper.sin(angle * DEG_TO_RAD), 0.0d, MathHelper.cos(angle * DEG_TO_RAD));
		Vector3d right = new Vector3d(-forward.z, 0.0d, forward.x);
		Vector3d startPoint = pos.subtract(right.scale(Math.ceil((double) this.width * 0.5d)));
		
		IntStream ranks = IntStream.range(0, this.depth);
		IntStream files = IntStream.range(0, this.width);
		
		return Streams.zip(ranks.boxed(), files.boxed(), (a, b) -> {
			if (UnitFormation.isSlotEmpty(this.lines[a][b])) return 0;
			CreatureEntity unit = this.lines[a][b].getEntity();
			Vector3d unitPos = startPoint.subtract(forward.scale(a)).subtract(right.scale(b));
			BlockPos blockPos = (new BlockPos(unitPos)).below();
			return level.loadedAndEntityCanStandOn(blockPos, unit) && level.noCollision(unit, unit.getBoundingBox().move(unitPos)) ? 1 : 0;
		}).reduce(Integer::sum).get();
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

	private static final String TAG_WIDTH = "width";
	private static final String TAG_DEPTH = "depth";
	private static final String TAG_FORMATION_RANK = "formationRank";
	private static final String TAG_UNITS = "units";
	private static final String TAG_RANK = "rank";
	private static final String TAG_FILE = "file";
	private static final String TAG_UUID = "uuid";
	private static final String TAG_FOLLOW_SPACING = "followSpacing";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		nbt.putInt(TAG_WIDTH, this.width);
		nbt.putInt(TAG_DEPTH, this.depth);
		nbt.putInt(TAG_FORMATION_RANK, this.formationRank);
		nbt.putInt(TAG_FOLLOW_SPACING, this.followSpacing);
		
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
		this.followSpacing = nbt.getInt(TAG_FOLLOW_SPACING);
		
		this.lines = new FormationEntityWrapper<?>[this.depth][this.width];
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt, World level) {
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
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
