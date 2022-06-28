package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import com.mojang.math.Constants;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationEntityWrapper;
import rbasamoyai.industrialwarfare.common.entityai.formation.MovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.FormationAttackTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class LineFormation extends UnitFormation {
	
	private int formationRank;
	private int width;
	private int depth;
	private int followSpacing;
	
	private List<List<FormationEntityWrapper<?>>> dynamicLines;
	
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
		this.dynamicLines = new ArrayList<>(this.depth);
	}
	
	private void validateDimensions() {
		if (this.depth <= 0 || this.width <= 0) {
			this.width = 1;
			this.depth = 1;
			this.dynamicLines = new ArrayList<>(this.depth);
			IndustrialWarfare.LOGGER.warn("Line Formation has dimension of zero or less; fixing dimensions to a line of 1 x 1");
		}
	}
	
	@Override
	public <E extends PathfinderMob & MovesInFormation> boolean addEntity(E entity) {
		if (!entity.isLowLevelUnit() || entity.getFormationRank() != this.formationRank) return false;
		if (this.insertEntityAtBack(entity)) return true;
		this.moveUpUnits();
		return this.insertEntityAtBack(entity);
	}
	
	private <E extends PathfinderMob & MovesInFormation> boolean insertEntityAtBack(E entity) {
		this.validateDimensions();
		
		if (this.dynamicLines.isEmpty()) {
			this.dynamicLines.add(Util.make(new ArrayList<>(), list -> list.add(new FormationEntityWrapper<>(entity))));
			return true;
		}
		
		List<FormationEntityWrapper<?>> unitLine = this.dynamicLines.get(this.dynamicLines.size() - 1);
		if (unitLine.isEmpty()) {
			unitLine.add(new FormationEntityWrapper<>(entity));
			return true;
		}
		
		for (int file = 0; file < unitLine.size(); ++file) {
			FormationEntityWrapper<?> wrapper = unitLine.get(file);
			if (UnitFormation.isSlotEmpty(wrapper)) {
				unitLine.set(file, new FormationEntityWrapper<>(entity));
				return true;
			}
		}
		if (unitLine.size() < this.width) {
			unitLine.add(new FormationEntityWrapper<>(entity));
			return true;
		}
		
		if (this.dynamicLines.size() == this.depth) return false;
		this.dynamicLines.add(Util.make(new ArrayList<>(), list -> list.add(new FormationEntityWrapper<>(entity))));
		return true;
	}
	
	@Override
	public void removeEntity(PathfinderMob entity) {
		for (int rank = 0; rank < this.dynamicLines.size(); ++rank) {
			List<FormationEntityWrapper<?>> unitLine = this.dynamicLines.get(rank);
			for (int file = 0; file < unitLine.size(); ++file) {
				if (UnitFormation.isSlotEmpty(unitLine.get(file)) || unitLine.get(file).getEntity() != entity) continue;
				unitLine.set(file, FormationEntityWrapper.EMPTY);
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
		int formationMiddle = (int) Math.ceil((double) this.width * 0.5d) - 1;
		Vec3 leaderForward = new Vec3(-Mth.sin(leader.getYRot() * Constants.DEG_TO_RAD), 0.0d, Mth.cos(leader.getYRot() * Constants.DEG_TO_RAD));
		Vec3 leaderRight = new Vec3(-leaderForward.z, 0.0d, leaderForward.x);
		Vec3 startPoint = leader.position().subtract(leaderRight.scale(formationMiddle));
		
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
		
		long time = leader.level.getGameTime() - leader.getLastOrderTime();
		
		int period = Mth.floor((double) time / (double) this.interval.getTime());
		
		int currentRank = period % (this.depth == 0 ? 1 : this.depth);
		int currentFile = period % (this.width == 0 ? 1 : this.width);
		boolean newPeriod = time % this.interval.getTime() == 0;
		
		for (int rank = 0; rank < this.dynamicLines.size(); ++rank) {
			List<FormationEntityWrapper<?>> unitLine = this.dynamicLines.get(rank);
			
			// Pruning back row
			if (rank == this.dynamicLines.size() - 1) {
				Iterator<FormationEntityWrapper<?>> startIter = unitLine.iterator();
				while (startIter.hasNext() && UnitFormation.isSlotEmpty(startIter.next())) {
					startIter.remove();
				}
				
				ListIterator<FormationEntityWrapper<?>> endIter = unitLine.listIterator(unitLine.size());
				while (endIter.hasPrevious() && UnitFormation.isSlotEmpty(endIter.previous())) {
					endIter.remove();
				}
				if (unitLine.isEmpty()) {
					this.dynamicLines.remove(rank);
					break;
				}
			}
			
			int middle = (int) Math.ceil((double) unitLine.size() * 0.5d) - 1;
			
			for (int file = 0; file < unitLine.size(); ++file) {
				FormationEntityWrapper<?> wrapper = unitLine.get(file);
				if (UnitFormation.isSlotEmpty(wrapper)) continue;
				PathfinderMob unit = wrapper.getEntity();
				if (!UnitFormation.checkMemoriesForMovement(unit) || !UnitFormation.checkMemoriesForSameGroup(commandGroup, unit)) {
					unitLine.set(file, FormationEntityWrapper.EMPTY);
					continue;
				}
				
				Brain<?> unitBrain = unit.getBrain();
				unitBrain.setMemory(MemoryModuleTypeInit.IN_FORMATION.get(), leader);
				
				if (unitBrain.checkMemory(MemoryModuleTypeInit.CAN_ATTACK.get(), MemoryStatus.VALUE_ABSENT)) {
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
				} else if (unitBrain.checkMemory(MemoryModuleTypeInit.CAN_ATTACK.get(), MemoryStatus.REGISTERED)) {
					if (this.attackType == FormationAttackTypeInit.NO_ATTACK.get() || !engagementFlag) {
						unitBrain.eraseMemory(MemoryModuleTypeInit.CAN_ATTACK.get());
					}
				}
				
				int posFile = file - middle + formationMiddle;
				
				Vec3 firingPos =
						startPoint
						.add(leaderForward)
						.add(leaderRight.scale(posFile))
						.add(0.0d, unit.getEyeHeight() + unit.getY() - startPoint.y, 0.0d);
				
				if (engagementFlag && UnitFormation.checkMemoriesForEngagement(unit)) {
					// Engagement
					if (unitBrain.getActiveNonCoreActivity().map(a -> a != Activity.FIGHT).orElse(true)) {
						unitBrain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), ActivityStatus.FIGHTING);
						unitBrain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), combatMode);
						unitBrain.setActiveActivityIfPossible(Activity.FIGHT);
					}
					
					if (unit instanceof IWeaponRangedAttackMob
						&& UnitFormation.canDoRangedAttack((PathfinderMob & IWeaponRangedAttackMob) unit, firingPos, MemoryModuleTypeInit.SHOOTING_POS.get())) {
						if (unitBrain.hasMemoryValue(MemoryModuleTypeInit.FINISHED_ATTACKING.get())) {
							unitBrain.eraseMemory(MemoryModuleTypeInit.SHOOTING_POS.get());
						} else if (unitBrain.hasMemoryValue(MemoryModuleTypeInit.CAN_ATTACK.get())) {
							unitBrain.setMemoryWithExpiry(MemoryModuleTypeInit.SHOOTING_POS.get(), firingPos, 40L);
						}
					} else {
						unitBrain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
						continue;
					}
					
				} else if (!engagementFlag) {
					unitBrain.setMemory(MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get(), true);
				}
				
				Vec3 precisePos =
						startPoint
						.subtract(leaderForward.scale(rank))
						.add(leaderRight.scale(posFile));
				
				// Position movement
				Vec3 possiblePos = this.tryFindingNewPosition(unit, precisePos);
				if (possiblePos == null) {
					possiblePos = this.tryFindingNewPosition(unit, precisePos.add(0.0d, unit.getY() - startPoint.y, 0.0d));
				}
				if (possiblePos == null) continue;
				if (this.formationState == State.FORMED && stopped && unit.position().closerThan(possiblePos, CLOSE_ENOUGH)) {
					// Stop and stay oriented if not attacking
					unit.setYRot(leader.getYRot());
					unit.yHeadRot = leader.getYRot();
					continue;
				}
				
				unitBrain.setMemoryWithExpiry(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos, 80L);
				unitBrain.setMemoryWithExpiry(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()), 80L);
			}
		}
		
		if (finishedForming) {
			this.formationState = State.FORMED;
		}
	}
	
	@Override
	public Vec3 getFollowPosition(FormationLeaderEntity leader) {
		Vec3 leaderForward = new Vec3(-Mth.sin(leader.getYRot() * Constants.DEG_TO_RAD), 0.0d, Mth.cos(leader.getYRot() * Constants.DEG_TO_RAD));
		return leader.position()
				.subtract(leaderForward.scale(this.depth + this.followSpacing))
				.add(0.0d, this.follower.getY() - leader.getY(), 0.0d);
	}
	
	@Override
	public float scoreOrientationAngle(float angle, Level level, PathfinderMob leader, Vec3 pos) {
		int formationMiddle = (int) Math.ceil((double) this.width * 0.5d) - 1;
		Vec3 forward = new Vec3(-Mth.sin(angle * Constants.DEG_TO_RAD), 0.0d, Mth.cos(angle * Constants.DEG_TO_RAD));
		Vec3 right = new Vec3(-forward.z, 0.0d, forward.x);
		Vec3 startPoint = pos.subtract(right.scale(formationMiddle));
		
		int score = 0;
		for (int rank = 0; rank < this.dynamicLines.size(); ++rank) {
			List<FormationEntityWrapper<?>> unitLine = this.dynamicLines.get(rank);
			int middle = (int) Math.ceil((double) unitLine.size() * 0.5d) - 1;
			
			for (int file = 0; file < unitLine.size(); ++file) {
				FormationEntityWrapper<?> wrapper = unitLine.get(file);
				if (UnitFormation.isSlotEmpty(wrapper)) continue;
				PathfinderMob unit = wrapper.getEntity();
				
				int posFile = file - middle + formationMiddle;
				
				Vec3 unitPos = startPoint.subtract(forward.scale(rank)).subtract(right.scale(posFile));
				BlockPos blockPos = (new BlockPos(unitPos)).below();
				if (level.loadedAndEntityCanStandOn(blockPos, unit) && level.noCollision(unit, unit.getBoundingBox().move(unitPos))) {
					++score;
				}
			}
		}
		
		return score;
	}
	
	private void moveUpUnits() {
		int formationMiddle = (int) Math.ceil((double) this.width * 0.5d) - 1;
		for (int rank = 0; rank < this.dynamicLines.size() - 1; ++rank) {
			List<FormationEntityWrapper<?>> unitLine = this.dynamicLines.get(rank);
			
			// Padding
			int ulsz = unitLine.size();
			if (ulsz < this.width) {
				int diff = this.width - ulsz;
				int lpad = diff / 2;
				int rpad = diff - lpad;
				for (int i = 0; i < lpad; ++i) {
					unitLine.add(0, FormationEntityWrapper.EMPTY);
				}
				for (int i = 0; i < rpad; ++i) {
					unitLine.add(FormationEntityWrapper.EMPTY);
				}
			}
			
			List<FormationEntityWrapper<?>> nextLine = this.dynamicLines.get(rank + 1);
			if (nextLine.isEmpty()) continue;
			int nextMiddle = (int) Math.ceil((double) nextLine.size() * 0.5d) - 1;
			for (int file = 0; file < this.width; ++file) {
				if (!UnitFormation.isSlotEmpty(unitLine.get(file))) continue;
				int nextFile = file - formationMiddle + nextMiddle;
				if (nextFile < 0 || nextFile >= nextLine.size() || UnitFormation.isSlotEmpty(nextLine.get(nextFile))) continue;
				FormationEntityWrapper<?> wrapper = nextLine.get(nextFile);
				unitLine.set(file, wrapper);
				nextLine.set(nextFile, FormationEntityWrapper.EMPTY);
			}
			
			ListIterator<FormationEntityWrapper<?>> startIter = unitLine.listIterator();
			ListIterator<FormationEntityWrapper<?>> fromLeftIter = nextLine.listIterator();
			while (startIter.hasNext() && UnitFormation.isSlotEmpty(startIter.next())) {
				if (!fromLeftIter.hasNext()) break;
				FormationEntityWrapper<?> wrapper = fromLeftIter.next();
				if (UnitFormation.isSlotEmpty(wrapper)) continue;
				fromLeftIter.set(FormationEntityWrapper.EMPTY);
				startIter.set(wrapper);
			}
			
			ListIterator<FormationEntityWrapper<?>> endIter = unitLine.listIterator(unitLine.size());
			ListIterator<FormationEntityWrapper<?>> fromRightIter = nextLine.listIterator(nextLine.size());
			while (endIter.hasPrevious() && UnitFormation.isSlotEmpty(endIter.previous())) {
				if (!fromRightIter.hasPrevious()) break;
				FormationEntityWrapper<?> wrapper = fromRightIter.previous();
				if (UnitFormation.isSlotEmpty(wrapper)) continue;
				fromRightIter.set(FormationEntityWrapper.EMPTY);
				endIter.set(wrapper);
			}
		}
		
		if (!this.dynamicLines.isEmpty() && !this.dynamicLines.get(this.dynamicLines.size() - 1).stream().anyMatch(w -> !UnitFormation.isSlotEmpty(w))) {
			this.dynamicLines.remove(this.dynamicLines.size() - 1);
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
	public CompoundTag serializeNBT() {
		CompoundTag nbt = super.serializeNBT();
		nbt.putInt(TAG_WIDTH, this.width);
		nbt.putInt(TAG_DEPTH, this.depth);
		nbt.putInt(TAG_FORMATION_RANK, this.formationRank);
		nbt.putInt(TAG_FOLLOW_SPACING, this.followSpacing);
		
		ListTag units = new ListTag();
		int formationMiddle = (int) Math.ceil((double) this.width * 0.5d) - 1;
		
		for (int rank = 0; rank < this.dynamicLines.size(); ++rank) {
			List<FormationEntityWrapper<?>> unitLine = this.dynamicLines.get(rank);
			int middle = (int) Math.ceil((double) unitLine.size() * 0.5d) - 1;
			
			for (int file = 0; file < unitLine.size(); ++file) {
				int posFile = file - middle + formationMiddle;
				
				FormationEntityWrapper<?> wrapper = unitLine.get(file);
				if (UnitFormation.isSlotEmpty(wrapper)) continue;
				CompoundTag unitTag = new CompoundTag();
				unitTag.putInt(TAG_RANK, rank);
				unitTag.putInt(TAG_FILE, posFile);
				unitTag.putUUID(TAG_UUID, wrapper.getEntity().getUUID());
				units.add(unitTag);
			}
		}
		nbt.put(TAG_UNITS, units);
		
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		this.width = nbt.getInt(TAG_WIDTH);
		this.depth = nbt.getInt(TAG_DEPTH);
		this.validateDimensions();
		this.formationRank = nbt.getInt(TAG_FORMATION_RANK);
		this.followSpacing = nbt.getInt(TAG_FOLLOW_SPACING);
		
		this.dynamicLines = new ArrayList<>(this.depth);
	}
	
	@Override
	protected void loadEntityData(CompoundTag nbt, Level level) {
		if (level.isClientSide) return;
		ServerLevel slevel = (ServerLevel) level;
		
		this.dynamicLines.clear();
		
		for (int i = 0; i < this.depth; ++i) {
			List<FormationEntityWrapper<?>> unitLine = new ArrayList<>(this.width);
			for (int j = 0; j < this.width; ++j) {
				unitLine.add(FormationEntityWrapper.EMPTY);
			}
			this.dynamicLines.add(unitLine);
		}
		
		ListTag units = nbt.getList(TAG_UNITS, Tag.TAG_COMPOUND);
		for (int i = 0; i < units.size(); ++i) {
			CompoundTag unitTag = units.getCompound(i);
			int rank = unitTag.getInt(TAG_RANK);
			int file = unitTag.getInt(TAG_FILE);
			if (!(0 <= rank && rank < this.depth && 0 <= file && file < this.width)) continue;
			Entity unit = slevel.getEntity(unitTag.getUUID(TAG_UUID));
			if (!(unit instanceof PathfinderMob && unit instanceof MovesInFormation)) continue;
			List<FormationEntityWrapper<?>> line = this.dynamicLines.get(rank);
			line.set(file, new FormationEntityWrapper<>((PathfinderMob & MovesInFormation) unit));
		}
	}
	
}
