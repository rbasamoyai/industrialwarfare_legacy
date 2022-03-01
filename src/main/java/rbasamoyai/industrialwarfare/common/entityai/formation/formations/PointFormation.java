package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entityai.ActivityStatus;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationEntityWrapper;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class PointFormation extends UnitFormation {

	private static final double CLOSE_ENOUGH = 0.1d;
	
	private Map<Point, Integer> positions;
	private Map<Point, FormationEntityWrapper<?>> units = new HashMap<>();
	
	public PointFormation(World level, Map<Point, Integer> positions) {
		super(null, level);
		this.positions = positions;
	}
	
	@Override
	public boolean addEntity(CreatureEntity entity) {
		if (!UnitFormation.checkMemoriesForMovement(entity) || !(entity instanceof IMovesInFormation)) return false;
		for (Point p : this.positions.keySet()) {
			if (!this.units.containsKey(p)) {
				this.units.put(p, new FormationEntityWrapper<>((CreatureEntity & IMovesInFormation) entity));
				return true;
			}
		}
		return false;
	}

	private static final float RAD_TO_DEG = (float) Math.PI / 180.0f;
	
	@Override
	public void tick(FormationLeaderEntity leader) {
		
		boolean finishedForming = this.formationState == State.FORMING;
		boolean stopped = leader.getDeltaMovement().lengthSqr() < 0.0064; // 0.08^2
		
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * RAD_TO_DEG), 0.0d, MathHelper.cos(leader.yRot * RAD_TO_DEG));
		Vector3d leaderRight = new Vector3d(-leaderForward.z, 0.0d, leaderForward.x);
		
		Brain<?> leaderBrain = leader.getBrain();
		
		if (leaderBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) return;
		UUID commandGroup = leaderBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
		UUID leaderUUID = leader.getUUID();
		
		boolean engagementFlag =
				leaderBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
				&& leaderBrain.hasMemoryValue(MemoryModuleTypeInit.ENGAGING_COMPLETED.get())
				&& leaderBrain.getMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get()).get();
		
		CombatMode combatMode = leaderBrain.getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).orElse(CombatMode.DONT_ATTACK);
		
		LivingEntity target = engagementFlag ? leaderBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get() : null;
		engagementFlag &= target != null && target.isAlive() && combatMode != CombatMode.DONT_ATTACK;
		
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
			
			unitBrain.setMemory(MemoryModuleTypeInit.IN_FORMATION.get(), leaderUUID);
			
			Vector3d precisePos = leader.position().subtract(leaderForward.scale(p.z)).add(leaderRight.scale(p.x)).add(0.0d, unit.getY() - leader.getY(), 0.0d);
			
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
			} else {
				if (this.formationState == State.FORMED && stopped && unit.position().closerThan(precisePos, CLOSE_ENOUGH)) {
					// Stop and stay oriented
					unit.yRot = leader.yRot;
					unit.yHeadRot = leader.yRot;
				} else {
					// Move to position
					Vector3d possiblePos = this.tryFindingNewPosition(unit, precisePos);
					if (possiblePos == null || unit.position().closerThan(possiblePos, CLOSE_ENOUGH)) continue;
					unitBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(this.level.dimension(), (new BlockPos(possiblePos)).below()));
					unitBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
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
	
	@Override
	public UnitFormationType<?> getType() {
		return null;
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt) {
		
	}
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = super.serializeNBT();
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		super.deserializeNBT(nbt);
	}
	
	public static class Point {
		public final int x;
		public final int z;
		
		public Point(int x, int z) {
			this.x = x;
			this.z = z;
		}
		
		@Override
		public int hashCode() {
			return this.x ^ (this.z << 16 | this.z >> 16);
		}
	}

}
