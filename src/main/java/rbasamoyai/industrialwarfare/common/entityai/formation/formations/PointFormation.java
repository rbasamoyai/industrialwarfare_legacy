package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

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
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationEntityWrapper;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.Interval;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.FormationAttackTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public abstract class PointFormation extends UnitFormation {
	
	protected Map<Point, Integer> positions;
	protected Map<Point, FormationEntityWrapper<?>> units = new HashMap<>();
	protected List<UnitFormation> innerFormations;
	
	public PointFormation(UnitFormationType<? extends PointFormation> type, Map<Point, Integer> positions, List<UnitFormation> innerFormations) {
		super(type);
		this.positions = positions;
		this.innerFormations = innerFormations;
	}
	
	@Override
	public FormationLeaderEntity spawnInnerFormationLeaders(World level, Vector3d pos, float facing, UUID commandGroup, PlayerIDTag owner) {
		FormationLeaderEntity leader = super.spawnInnerFormationLeaders(level, pos, facing, commandGroup, owner);
		
		this.innerFormations
		.stream()
		.map(inner -> inner.spawnInnerFormationLeaders(level, pos, facing, commandGroup, owner))
		.forEach(leader::addEntity);
		
		return leader;
	}
	
	@Override
	public void killInnerFormationLeaders() {
		super.killInnerFormationLeaders();
		
		this.units.values()
		.stream()
		.map(FormationEntityWrapper::getEntity)
		.filter(e -> e.getType() == EntityTypeInit.FORMATION_LEADER.get())
		.forEach(Entity::kill);
	}
	
	@Override
	public void updateOrderTime() {
		super.updateOrderTime();
		this.streamLeadersFromUnits().forEach(FormationLeaderEntity::updateOrderTime);
	}
	
	@Override
	public <E extends CreatureEntity & IMovesInFormation> boolean addEntity(E entity) {
		int rank = entity.getFormationRank();
		
		for (Point p : this.positions.keySet()) {
			if (this.units.containsKey(p)) {
				CreatureEntity occupier = this.units.get(p).getEntity();
				if (occupier instanceof FormationLeaderEntity && ((FormationLeaderEntity) occupier).addEntity(entity)) {
					return true;
				}
			} else if (this.positions.get(p).intValue() == rank) {
				this.units.put(p, new FormationEntityWrapper<>(entity));
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void removeEntity(CreatureEntity entity) {
		this.units.keySet()
		.stream()
		.filter(p -> this.units.get(p).getEntity() == entity)
		.findFirst()
		.map(this.units::remove);
	}
	
	@Override
	public boolean hasMatchingFormationLeader(FormationLeaderEntity inFormationWith) {
		return this.streamLeadersFromUnits().anyMatch(f -> f.hasMatchingFormationLeader(inFormationWith));
	}
	
	@Override
	public void setAttackInterval(Interval interval) {
		super.setAttackInterval(interval);
		this.streamLeadersFromUnits().forEach(f -> f.setAttackInterval(interval));
	}
	
	@Override
	public void setAttackType(FormationAttackType type) {
		super.setAttackType(type);	
		this.streamLeadersFromUnits().forEach(f -> f.setAttackType(type));
	}
	
	@Override
	public void tick(FormationLeaderEntity leader) {
		if (this.formationState == null || this.formationState == State.BROKEN) return;
		
		boolean finishedForming = this.formationState == State.FORMING;
		boolean stopped = UnitFormation.isStopped(leader);
		
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * DEG_TO_RAD), 0.0d, MathHelper.cos(leader.yRot * DEG_TO_RAD));
		Vector3d leaderRight = new Vector3d(-leaderForward.z, 0.0d, leaderForward.x);
		
		Brain<?> leaderBrain = leader.getBrain();
		
		boolean engagementFlag =
				leaderBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
				&& leaderBrain.hasMemoryValue(MemoryModuleTypeInit.ENGAGING_COMPLETED.get())
				&& leaderBrain.getMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get()).get();
		
		CombatMode combatMode = leaderBrain.getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).orElse(CombatMode.DONT_ATTACK);
		
		LivingEntity target = engagementFlag ? leaderBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get() : null;
		engagementFlag &= target != null && target.isAlive() && combatMode != CombatMode.DONT_ATTACK;
		
		if (!leaderBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) return;
		UUID commandGroup = leaderBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
		
		this.attackType = FormationAttackTypeInit.FIRE_AT_WILL.get();
		
		for (Point p : this.positions.keySet()) {
			if (!this.units.containsKey(p)) continue;

			FormationEntityWrapper<?> wrapper = this.units.get(p);
			if (UnitFormation.isSlotEmpty(wrapper)) {
				this.units.remove(p);
				continue;
			}
			CreatureEntity unit = wrapper.getEntity();
			if (!UnitFormation.checkMemoriesForMovement(unit)) {
				this.units.remove(p);
				continue;
			}
			
			Brain<?> unitBrain = unit.getBrain();
			
			if (!(unit instanceof FormationLeaderEntity) && !UnitFormation.checkMemoriesForSameGroup(commandGroup, unit)) {
				this.units.remove(p);
				continue;
			}
			
			unitBrain.setMemory(MemoryModuleTypeInit.IN_FORMATION.get(), leader);
			
			if (unit.getType() == EntityTypeInit.FORMATION_LEADER.get()) {
				unitBrain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
				unitBrain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), combatMode);
			} else if (engagementFlag && UnitFormation.checkMemoriesForEngagement(unit)) {
				// Engagement
				if (unitBrain.getActiveNonCoreActivity().map(a -> a != Activity.FIGHT).orElse(true)) {
					unitBrain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), ActivityStatus.FIGHTING);
					unitBrain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), combatMode);
					unitBrain.setActiveActivityIfPossible(Activity.FIGHT);
				}
				
				if (unit instanceof IWeaponRangedAttackMob
					&& UnitFormation.canDoRangedAttack((CreatureEntity & IWeaponRangedAttackMob) unit, target.getEyePosition(1.0f), MemoryModuleTypeInit.SHOOTING_POS.get())) {
					unitBrain.setMemoryWithExpiry(MemoryModuleTypeInit.SHOOTING_POS.get(), target.getEyePosition(1.0f), 40L);
				} else {
					unitBrain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
					continue;
				}
			} else if (!engagementFlag) {
				unitBrain.setMemory(MemoryModuleTypeInit.SHOULD_PREPARE_ATTACK.get(), true);
			}
			
			Vector3d precisePos =
					leader.position()
					.add(leaderForward.scale(p.z))
					.add(leaderRight.scale(p.x));
			
			boolean closeEnough = unit.position().closerThan(precisePos, CLOSE_ENOUGH);
			
			if (unitBrain.checkMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), MemoryModuleStatus.REGISTERED) && closeEnough) {
				unitBrain.setMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), engagementFlag);
			}
			
			if (this.formationState == State.FORMED && stopped && closeEnough && !engagementFlag) {
				// Stop and stay oriented
				unit.yRot = leader.yRot;
				unit.yHeadRot = leader.yRot;
				continue;
			}
			
			// Move to position
			if (unitBrain.hasMemoryValue(MemoryModuleType.MEETING_POINT) && !unitBrain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
				unitBrain.eraseMemory(MemoryModuleType.MEETING_POINT);
			} else {
				Vector3d possiblePos = this.tryFindingNewPosition(unit, precisePos);
				if (possiblePos == null) {
					possiblePos = this.tryFindingNewPosition(unit, precisePos.add(0.0d, unit.getY() - leader.getY(), 0.0d));
				}
				
				if (possiblePos != null && !unit.position().closerThan(possiblePos, CLOSE_ENOUGH)) {
					unitBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
					unitBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()));
				}
			}
		}
		
		if (finishedForming) {
			this.formationState = State.FORMED;
		}
	}
	
	private Stream<FormationLeaderEntity> streamLeadersFromUnits() {
		return this.units.values()
				.stream()
				.filter(w -> !UnitFormation.isSlotEmpty(w))
				.map(FormationEntityWrapper::getEntity)
				.filter(FormationLeaderEntity.class::isInstance)
				.map(FormationLeaderEntity.class::cast);
	}
	
	@Override
	public float scoreOrientationAngle(float angle, World level, CreatureEntity leader, Vector3d pos) {
		Vector3d forward = new Vector3d(-MathHelper.sin(leader.yRot * DEG_TO_RAD), 0.0d, MathHelper.cos(leader.yRot * DEG_TO_RAD));
		Vector3d right = new Vector3d(-forward.z, 0.0d, forward.x);
		
		return this.units.entrySet()
				.stream()
				.filter(e -> !UnitFormation.isSlotEmpty(e.getValue()))
				.map(e -> {
					CreatureEntity unit = e.getValue().getEntity();
					if (unit instanceof FormationLeaderEntity) {
						return ((FormationLeaderEntity) unit).scoreOrientationAngle(angle, pos);
					}
					Point p = e.getKey();
					Vector3d unitPos = pos.add(forward.scale(p.z)).add(right.scale(p.x));
					BlockPos blockPos = (new BlockPos(unitPos)).below();
					return level.loadedAndEntityCanStandOn(blockPos, unit) && level.noCollision(unit, unit.getBoundingBox().move(unitPos)) ? 1.0f : 0.0f;
				})
				.reduce(Float::sum)
				.orElse(0.0f);
	}
	
	private static final String TAG_POINTS = "points";
	private static final String TAG_X = "x";
	private static final String TAG_Z = "z";
	private static final String TAG_RANK = "rank";
	private static final String TAG_UUID = "uuid";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		
		ListNBT points = new ListNBT();
		for (Map.Entry<Point, Integer> entry : this.positions.entrySet()) {
			Point point = entry.getKey();
			
			CompoundNBT pointTag = new CompoundNBT();
			pointTag.putInt(TAG_X, point.x);
			pointTag.putInt(TAG_Z, point.z);
			pointTag.putInt(TAG_RANK, entry.getValue());
			if (this.units.containsKey(point)) {
				FormationEntityWrapper<?> wrapper = this.units.get(point);
				if (!isSlotEmpty(wrapper)) pointTag.putUUID(TAG_UUID, wrapper.getEntity().getUUID());
			}
			
			points.add(pointTag);
		}
		nbt.put(TAG_POINTS, points);
		
		return nbt;
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt, World level) {
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
		ListNBT points = nbt.getList(TAG_POINTS, Constants.NBT.TAG_COMPOUND);
		this.positions.clear();
		this.units.clear();
		for (int i = 0; i < points.size(); ++i) {
			CompoundNBT pointTag = points.getCompound(i);
			
			int x = pointTag.getInt(TAG_X);
			int z = pointTag.getInt(TAG_Z);
			Point point = new UnitFormation.Point(x, z);			
			int rank = pointTag.getInt(TAG_RANK);
			
			this.positions.put(point, rank);
			
			if (!pointTag.contains(TAG_UUID)) continue;
			Entity unit = slevel.getEntity(pointTag.getUUID(TAG_UUID));
			if (!(unit instanceof CreatureEntity && unit instanceof IMovesInFormation)) continue;
			this.units.put(point, new FormationEntityWrapper<>((CreatureEntity & IMovesInFormation) unit));
		}
	}
	
	/**
	 * Based on <a href="https://egalluzzo.blogspot.com/2010/06/using-inheritance-with-fluent.html">this post.</a>	
	 */
	public static abstract class Builder<T extends PointFormation, B extends Builder<T, B>> {
		protected final Map<Point, Integer> positions = new HashMap<>();
		protected final List<UnitFormation> innerFormations = new ArrayList<>();
		
		private final B thisObj;
		
		public Builder() {
			this.thisObj = this.getThis();
		}
		
		public B addRegularPoint(UnitFormation.Point point, int rank) {
			this.positions.put(point, rank);
			return this.thisObj;
		}
		
		public B addFormationPoint(UnitFormation.Point point, UnitFormation formation) {
			this.positions.put(point, formation.getLeaderRank());
			this.innerFormations.add(formation);
			return this.thisObj;
		}
		
		public abstract T build();
		public abstract B getThis();
	}

}
