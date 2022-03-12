package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

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
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;

public class PointFormation extends UnitFormation {

	public static final int RANK = 0xF00BA002; // FUBAR #002
	
	private static final double CLOSE_ENOUGH = 0.1d;
	
	private Map<UnitFormation.Point, Integer> positions;
	private Map<UnitFormation.Point, FormationEntityWrapper<?>> units = new HashMap<>();
	private List<UnitFormation> innerFormations;
	
	public PointFormation(Map<UnitFormation.Point, Integer> positions, List<UnitFormation> innerFormations) {
		super(UnitFormationTypeInit.POINTS.get());
		this.positions = positions;
		this.innerFormations = innerFormations;
	}
	
	@Override
	public List<UnitFormation> getInnerFormations() {
		return this.innerFormations;
	}
	
	@Override
	public void killInnerFormationLeaders() {
		this.units.values()
		.stream()
		.map(FormationEntityWrapper::getEntity)
		.filter(e -> e instanceof FormationLeaderEntity)
		.forEach(Entity::kill);
	}
	
	@Override
	public boolean addEntity(CreatureEntity entity) {
		if (!UnitFormation.checkMemoriesForMovement(entity) || !(entity instanceof IMovesInFormation)) return false;
		IMovesInFormation unit = (IMovesInFormation) entity;
		int rank = unit.getFormationRank();
		
		for (UnitFormation.Point p : this.positions.keySet()) {
			if (this.units.containsKey(p)) {
				CreatureEntity occupier = this.units.get(p).getEntity();
				if (occupier instanceof FormationLeaderEntity && ((FormationLeaderEntity) occupier).addEntity(entity)) {
					return true;
				}
			} else if (this.positions.get(p).intValue() == rank) {
				this.units.put(p, new FormationEntityWrapper<>((CreatureEntity & IMovesInFormation) entity));
				return true;
			}
		}
		return false;
	}

	private static final float RAD_TO_DEG = (float) Math.PI / 180.0f;
	
	@Override
	public void tick(FormationLeaderEntity leader) {
		if (this.formationState == null || this.formationState == State.BROKEN || leader == null) return;
		
		boolean finishedForming = this.formationState == State.FORMING;
		boolean stopped = leader.getDeltaMovement().lengthSqr() < 0.0064; // 0.08^2
		
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * RAD_TO_DEG), 0.0d, MathHelper.cos(leader.yRot * RAD_TO_DEG));
		Vector3d leaderRight = new Vector3d(-leaderForward.z, 0.0d, leaderForward.x);
		
		Brain<?> leaderBrain = leader.getBrain();
		
		if (!leaderBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) return;
		UUID commandGroup = leaderBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
		UUID leaderUUID = leader.getUUID();
		
		boolean engagementFlag =
				leaderBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
				&& leaderBrain.hasMemoryValue(MemoryModuleTypeInit.ENGAGING_COMPLETED.get())
				&& leaderBrain.getMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get()).get();
		
		CombatMode combatMode = leaderBrain.getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).orElse(CombatMode.DONT_ATTACK);
		
		LivingEntity target = engagementFlag ? leaderBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get() : null;
		engagementFlag &= target != null && target.isAlive() && combatMode != CombatMode.DONT_ATTACK;
		
		for (UnitFormation.Point p : this.positions.keySet()) {
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
			
			unitBrain.setMemory(MemoryModuleTypeInit.IN_FORMATION.get(), leaderUUID);
			
			Vector3d precisePos = leader.position().add(leaderForward.scale(p.z)).add(leaderRight.scale(p.x)).add(0.0d, unit.getY() - leader.getY(), 0.0d);
			
			if (engagementFlag && UnitFormation.checkMemoriesForEngagement(unit)) {
				// Engagement
				if (unit instanceof FormationLeaderEntity
					&& unitBrain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)
					&& unitBrain.checkMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), MemoryModuleStatus.REGISTERED)) {
					unitBrain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
					unitBrain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), combatMode);
				} else if (!(unit instanceof IWeaponRangedAttackMob)
					|| UnitFormation.canDoRangedAttack((CreatureEntity & IWeaponRangedAttackMob) unit, target)) {
					
					if (!(unit instanceof IWeaponRangedAttackMob)) {
						this.units.remove(p);
					}
					unitBrain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
					unitBrain.setMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), ActivityStatus.FIGHTING);
					unitBrain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), combatMode);
					unitBrain.setActiveActivityIfPossible(Activity.FIGHT);
				}
			} else if (this.formationState == State.FORMED && stopped && unit.position().closerThan(precisePos, CLOSE_ENOUGH)) {
				// Stop and stay oriented
				unit.yRot = leader.yRot;
				unit.yHeadRot = leader.yRot;
				continue;
			}
			
			// Move to position
			Vector3d possiblePos = this.tryFindingNewPosition(unit, precisePos);
			if (possiblePos == null || unit.position().closerThan(possiblePos, CLOSE_ENOUGH)) continue;
			unitBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
			unitBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()));
		}
		
		if (finishedForming) {
			this.formationState = State.FORMED;
		}
	}
	
	@Override
	public int getLeaderRank() {
		return RANK;
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

	@Override
	public void consumeGroupAction(int group, Consumer<CreatureEntity> action) {
		
	}

	@Override
	public float getWidth() {
		return 0;
	}

	@Override
	public float getDepth() {
		return 0;
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
		for (Map.Entry<UnitFormation.Point, Integer> entry : this.positions.entrySet()) {
			UnitFormation.Point point = entry.getKey();
			
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
			UnitFormation.Point point = new UnitFormation.Point(x, z);			
			int rank = pointTag.getInt(TAG_RANK);
			
			this.positions.put(point, rank);
			
			if (!pointTag.contains(TAG_UUID)) continue;
			Entity unit = slevel.getEntity(pointTag.getUUID(TAG_UUID));
			if (!(unit instanceof CreatureEntity && unit instanceof IMovesInFormation)) continue;
			this.units.put(point, new FormationEntityWrapper<>((CreatureEntity & IMovesInFormation) unit));
		}
	}
	
	public static class Builder {
		private final Map<UnitFormation.Point, Integer> positions = new HashMap<>();
		private final List<UnitFormation> innerFormations = new ArrayList<>();
		
		public Builder addRegularPoint(UnitFormation.Point point, int rank) {
			this.positions.put(point, rank);
			return this;
		}
		
		public Builder addFormationPoint(UnitFormation.Point point, UnitFormation formation) {
			this.positions.put(point, formation.getLeaderRank());
			this.innerFormations.add(formation);
			return this;
		}
		
		public PointFormation build() {
			return new PointFormation(this.positions, this.innerFormations);
		}
	}

}
