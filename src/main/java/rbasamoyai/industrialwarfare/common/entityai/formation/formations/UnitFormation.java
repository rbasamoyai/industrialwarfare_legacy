package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationEntityWrapper;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public abstract class UnitFormation implements INBTSerializable<CompoundNBT> {

	protected static final float RAD_TO_DEG = (float) Math.PI / 180.0f;
	protected static final double CLOSE_ENOUGH = 0.1d;
	protected static final double ORIENTATION_CALC_DIST = 1.0d;
	
	private final UnitFormationType<?> type;
	protected State formationState = State.BROKEN;
	
	private boolean loadDataOnNextTick = false;
	private CompoundNBT dataToDeserialize;
	
	protected CreatureEntity follower;
	
	public UnitFormation(UnitFormationType<?> type) {
		this.type = type;
	}
	
	public void setState(State formationState) { this.formationState = formationState; } 
	public State getState() { return this.formationState; }
	
	public abstract <E extends CreatureEntity & IMovesInFormation> boolean addEntity(E entity);
	
	protected abstract void tick(FormationLeaderEntity leader);
	protected abstract void loadEntityData(CompoundNBT nbt, World level);
	
	public void setFollower(CreatureEntity entity) { this.follower = entity; }
	
	public UnitFormationType<?> getType() {
		return this.type;
	}
	
	public final void doTick(FormationLeaderEntity leader) {
		if (this.loadDataOnNextTick) {
			this.loadFollowerData(dataToDeserialize, leader.level);
			this.loadEntityData(this.dataToDeserialize, leader.level);
			this.loadDataOnNextTick = false;
			this.dataToDeserialize = new CompoundNBT();
		}
		this.tick(leader);
	}
	
	public abstract float scoreOrientationAngle(float angle, World level, CreatureEntity leader);
	
	public FormationLeaderEntity spawnInnerFormationLeaders(World level, Vector3d pos, float facing, UUID commandGroup, PlayerIDTag owner) {
		FormationLeaderEntity leader = new FormationLeaderEntity(EntityTypeInit.FORMATION_LEADER.get(), level, this);
		leader.setPos(pos.x, pos.y, pos.z);
		leader.yRot = facing;
		leader.setState(UnitFormation.State.FORMED);
		leader.setOwner(owner);
		leader.getBrain().setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
		
		level.addFreshEntity(leader);
		
		return leader;
	}
	
	private static final double[] Y_CHECKS = new double[] {0.0d, 1.0d, -1.0d};
	
	@Nullable
	protected Vector3d tryFindingNewPosition(CreatureEntity unit, Vector3d precisePos) {
		for (double y : Y_CHECKS) {
			Vector3d newPos = precisePos.add(0.0d, y, 0.0d);
			if (unit.level.loadedAndEntityCanStandOn((new BlockPos(newPos)).below(), unit)) return newPos;
		}
		return null;
	}
	
	public int getLeaderRank() {
		return this.type.getFormationRank();
	}
	
	public void killInnerFormationLeaders() {
		if (this.follower != null) this.follower.kill();
	}
	
	public static boolean isSlotEmpty(FormationEntityWrapper<?> wrapper) {
		if (wrapper == null) return true;
		CreatureEntity entity = wrapper.getEntity();
		return entity == null || !entity.isAlive();
	}
	
	public static boolean checkMemoriesForSameGroup(UUID leaderGroup, CreatureEntity entity) {
		Brain<?> brain = entity.getBrain();
		return brain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())
			&& brain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get().equals(leaderGroup);
	}
	
	public static boolean checkMemoriesForMovement(CreatureEntity entity) {
		Brain<?> brain = entity.getBrain();
		return brain.checkMemory(MemoryModuleType.MEETING_POINT, MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.PRECISE_POS.get(), MemoryModuleStatus.REGISTERED);
	}
	
	public static boolean checkMemoriesForEngagement(CreatureEntity entity) {
		Brain<?> brain = entity.getBrain();
		return brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.ACTIVITY_STATUS.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), MemoryModuleStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.CAN_ATTACK.get(), MemoryModuleStatus.REGISTERED);
	}
	
	public static <E extends CreatureEntity & IWeaponRangedAttackMob> boolean canDoRangedAttack(E unit, LivingEntity target) {
		return unit.canDoRangedAttack()
				&& BrainUtil.canSee(unit, target)
				&& BrainUtil.isWithinAttackRange(unit, target, 0)
				&& !unit.getBrain().hasMemoryValue(MemoryModuleTypeInit.FINISHED_ATTACKING.get());
	}
	
	private static final String TAG_STATE = "state";
	private static final String TAG_FOLLOWER = "follower";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt(TAG_STATE, this.formationState.getId());
		if (this.follower != null) {
			nbt.putUUID(TAG_FOLLOWER, this.follower.getUUID());
		}
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.formationState = State.fromId(nbt.getInt(TAG_STATE));
		this.loadDataOnNextTick = true;
		this.dataToDeserialize = nbt;
	}
	
	protected void loadFollowerData(CompoundNBT nbt, World level) {
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
		if (nbt.hasUUID(TAG_FOLLOWER)) {
			Entity e = slevel.getEntity(nbt.getUUID(TAG_FOLLOWER));
			if (!(e instanceof CreatureEntity)) return;
			this.setFollower((CreatureEntity) e);
		}
	}
	
	public static enum State {
		BROKEN(0),
		FORMING(1),
		FORMED(2);
		
		private static final State[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(State::getId)).toArray(sz -> new State[sz]);
		
		private final int id;
		
		private State(int id) {
			this.id = id;
		}
		
		public int getId() { return this.id; }
		public static State fromId(int id) {
			return 0 <= id && id < BY_ID.length ? BY_ID[id] : BROKEN;
		}
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
