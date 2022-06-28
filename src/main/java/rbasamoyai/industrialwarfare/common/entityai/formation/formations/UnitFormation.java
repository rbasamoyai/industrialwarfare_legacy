package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entities.IWeaponRangedAttackMob;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationEntityWrapper;
import rbasamoyai.industrialwarfare.common.entityai.formation.MovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.Interval;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.FormationAttackTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public abstract class UnitFormation implements INBTSerializable<CompoundTag> {

	protected static final double CLOSE_ENOUGH = 0.1d;
	protected static final double ORIENTATION_CALC_DIST = 1.0d;
	
	private final UnitFormationType<?> type;
	protected State formationState = State.BROKEN;
	
	private boolean loadDataOnNextTick = false;
	private CompoundTag dataToDeserialize = new CompoundTag();
	
	protected FormationAttackType attackType = FormationAttackTypeInit.FIRE_AT_WILL.get();
	
	protected PathfinderMob follower;
	protected Float cachedAngle;
	
	protected Interval interval = Interval.T_1S;
	
	public UnitFormation(UnitFormationType<?> type) {
		this.type = type;
	}
	
	public void setState(State formationState) { this.formationState = formationState; } 
	public State getState() { return this.formationState; }
	
	public abstract <E extends PathfinderMob & MovesInFormation> boolean addEntity(E entity);
	public abstract void removeEntity(PathfinderMob entity);
	
	public boolean isInFormationWith(FormationLeaderEntity leader) {
		return this.hasMatchingFormationLeader(leader)
			|| this.follower instanceof FormationLeaderEntity && ((FormationLeaderEntity) this.follower).hasMatchingFormationLeader(leader);
	}
	
	public abstract boolean hasMatchingFormationLeader(FormationLeaderEntity inFormationWith);
	
	protected abstract void tick(FormationLeaderEntity leader);
	protected abstract void loadEntityData(CompoundTag nbt, Level level);
	
	public void setFollower(PathfinderMob entity) { this.follower = entity; }
	
	public void updateOrderTime() {
		if (this.follower instanceof FormationLeaderEntity) {
			((FormationLeaderEntity) this.follower).updateOrderTime();
		}		
	}
	
	public void setAttackType(FormationAttackType type) { 
		if (this.type.checkAttackType(type)) this.attackType = type;
	}
	
	public void setAttackInterval(Interval interval) { this.interval = interval; }
	
	public UnitFormationType<?> getType() {
		return this.type;
	}
	
	public final void doTick(FormationLeaderEntity leader) {
		boolean loadedData = false;
		if (this.loadDataOnNextTick && this.dataToDeserialize != null) {
			this.loadFollowerData(this.dataToDeserialize, leader.level);
			this.loadEntityData(this.dataToDeserialize, leader.level);
			this.loadDataOnNextTick = false;
			this.dataToDeserialize = new CompoundTag();
			loadedData = true;
		}
		this.tick(leader);
		if (!loadedData) this.tickFollower(leader);
	}
	
	protected void tickFollower(FormationLeaderEntity leader) {
		if (this.follower == null) return;
		if (!this.follower.isAlive() || !checkMemoriesForMovement(this.follower)) {
			this.follower = null;
			return;
		}
		
		boolean stopped = isStopped(leader);
		Vec3 followPos = this.getFollowPosition(leader);
		
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
								float angle1 = leader.getYRot() + angle.floatValue();
								float score = followerLeader.scoreOrientationAngle(angle1, followPos) * weight.floatValue();
								PathfinderMob nextFollower = followerLeader.getFormation().follower;
								if (!(nextFollower instanceof FormationLeaderEntity)) {
									return new Tuple<>(score, angle1);
								}
								FormationLeaderEntity nextLeader = (FormationLeaderEntity) nextFollower;
								
								// Adjusting the follow position to be relative to followPos
								Vec3 adjustedPos =
										nextLeader.getFollowPosition()
										.subtract(nextLeader.position())
										.add(followPos);
								
								float score1 = score + nextLeader.scoreOrientationAngle(angle1, adjustedPos) * weight.floatValue();
								return new Tuple<>(score1, angle1);
							})
							.sorted((a, b) -> -Float.compare(a.getA(), b.getA()))
							.map(Tuple::getB)
							.findFirst()
							.get();
				}
				
				followerLeader.setYRot(this.cachedAngle);
				followerLeader.yHeadRot = this.cachedAngle;
			}
		} else {
			this.cachedAngle = null;
			Brain<?> followerBrain = this.follower.getBrain();
			if (followerBrain.hasMemoryValue(MemoryModuleType.MEETING_POINT) && !followerBrain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
				followerBrain.eraseMemory(MemoryModuleType.MEETING_POINT);
			} else {
				Vec3 possiblePos = this.tryFindingNewPosition(this.follower, followPos);
				if (possiblePos != null && !closeEnough) {
					followerBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
					followerBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()));
				}
			}	
		}
	}
	
	public abstract Vec3 getFollowPosition(FormationLeaderEntity leader);
	
	public abstract float scoreOrientationAngle(float angle, Level level, PathfinderMob leader, Vec3 pos);
	
	public FormationLeaderEntity spawnInnerFormationLeaders(Level level, Vec3 pos, UUID commandGroup, PlayerIDTag owner) {
		FormationLeaderEntity leader = new FormationLeaderEntity(EntityTypeInit.FORMATION_LEADER.get(), level, this);
		leader.setPos(pos.x, pos.y, pos.z);
		leader.setState(UnitFormation.State.FORMED);
		leader.setOwner(owner);
		leader.getBrain().setMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get(), commandGroup);
		
		level.addFreshEntity(leader);
		
		return leader;
	}
	
	private static final double[] Y_CHECKS = new double[] {0.0d, 1.0d, -1.0d};
	
	@Nullable
	protected Vec3 tryFindingNewPosition(PathfinderMob unit, Vec3 precisePos) {
		for (double y : Y_CHECKS) {
			Vec3 newPos = precisePos.add(0.0d, y, 0.0d);
			if (unit.level.loadedAndEntityCanStandOn((new BlockPos(newPos)).below(), unit)
				&& unit.level.noCollision(unit, unit.getBoundingBox().move(newPos.subtract(unit.position())))) {
				return newPos;
			}
		}
		return null;
	}
	
	public int getLeaderRank() {
		return this.type.getFormationRank();
	}
	
	public void killInnerFormationLeaders() {
		if (this.follower != null && this.follower.getType() == EntityTypeInit.FORMATION_LEADER.get()) this.follower.kill();
	}
	
	public static boolean isSlotEmpty(FormationEntityWrapper<?> wrapper) {
		if (wrapper == null || wrapper == FormationEntityWrapper.EMPTY) return true;
		PathfinderMob entity = wrapper.getEntity();
		return entity == null || !entity.isAlive();
	}
	
	public static boolean checkMemoriesForSameGroup(UUID leaderGroup, PathfinderMob entity) {
		Brain<?> brain = entity.getBrain();
		return brain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())
			&& brain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get().equals(leaderGroup);
	}
	
	public static boolean checkMemoriesForMovement(PathfinderMob entity) {
		Brain<?> brain = entity.getBrain();
		return brain.checkMemory(MemoryModuleType.MEETING_POINT, MemoryStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.PRECISE_POS.get(), MemoryStatus.REGISTERED);
	}
	
	public static boolean checkMemoriesForEngagement(PathfinderMob entity) {
		Brain<?> brain = entity.getBrain();
		return brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)
			&& brain.checkMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), MemoryStatus.REGISTERED);
	}
	
	public static <E extends PathfinderMob & IWeaponRangedAttackMob> boolean canDoRangedAttack(E unit, LivingEntity target) {
		return unit.canDoRangedAttack()
				&& BehaviorUtils.canSee(unit, target)
				&& BehaviorUtils.isWithinAttackRange(unit, target, 0)
				&& unit.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED);
	}
	
	public static <E extends PathfinderMob & IWeaponRangedAttackMob> boolean canDoRangedAttack(E unit, Position target, MemoryModuleType<Position> type ) {
		if (!unit.canDoRangedAttack() || !unit.getBrain().checkMemory(type, MemoryStatus.REGISTERED)) return false;
		
		Item item = unit.getMainHandItem().getItem();
		if (!(item instanceof ProjectileWeaponItem)) return false;
		int range = ((ProjectileWeaponItem) item).getDefaultProjectileRange();
		return unit.position().closerThan(target, range);
	}
	
	public static boolean isStopped(Entity e) {
		return e.getDeltaMovement().lengthSqr() < 0.0064d; // 0.08 ^ 2
	}
	
	protected static final String TAG_STATE = "state";
	protected static final String TAG_FOLLOWER = "follower";
	protected static final String TAG_ATTACK_TYPE = "attackType";
	protected static final String TAG_INTERVAL = "interval";
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt(TAG_STATE, this.formationState.getId());
		if (this.follower != null) {
			nbt.putUUID(TAG_FOLLOWER, this.follower.getUUID());
		}
		nbt.putInt(TAG_INTERVAL, this.interval.getId());
		nbt.putString(TAG_ATTACK_TYPE, this.attackType.getRegistryName().toString());
		return nbt;
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.formationState = State.fromId(nbt.getInt(TAG_STATE));
		ResourceLocation typeLoc = nbt.contains(TAG_ATTACK_TYPE)
				? new ResourceLocation(nbt.getString(TAG_ATTACK_TYPE))
				: IWModRegistries.FORMATION_ATTACK_TYPES.get().getDefaultKey();
		this.interval = Interval.fromId(nbt.getInt(TAG_INTERVAL));
	this.attackType = IWModRegistries.FORMATION_ATTACK_TYPES.get().getValue(typeLoc);
		this.loadDataOnNextTick = true;
		this.dataToDeserialize = nbt;
	}
	
	protected void loadFollowerData(CompoundTag nbt, Level level) {
		if (level.isClientSide) return;
		ServerLevel slevel = (ServerLevel) level;
		
		if (nbt.hasUUID(TAG_FOLLOWER)) {
			Entity e = slevel.getEntity(nbt.getUUID(TAG_FOLLOWER));
			if (!(e instanceof PathfinderMob)) return;
			this.setFollower((PathfinderMob) e);
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
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj instanceof Point) {
				return this.hashCode() == obj.hashCode();
			} else {
				return false;
			}
		}
		
		@Override
		public String toString() {
			return "(" + this.x + ", " + this.z + ")";
		}
	}
	
}
