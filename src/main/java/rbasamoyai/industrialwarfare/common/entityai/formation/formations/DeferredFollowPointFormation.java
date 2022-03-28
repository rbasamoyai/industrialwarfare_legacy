package rbasamoyai.industrialwarfare.common.entityai.formation.formations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entities.FormationLeaderEntity;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.IMovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;

public class DeferredFollowPointFormation extends PointFormation {
	
	private Point deferredPoint;
	private UnitFormation deferredFormation;
	private FormationLeaderEntity deferredLeader;
	
	public DeferredFollowPointFormation(UnitFormationType<? extends DeferredFollowPointFormation> type, int formationRank) {
		this(type, new HashMap<>(), new ArrayList<>(), new Point(0, 0), null);
	}
	
	public DeferredFollowPointFormation(UnitFormationType<? extends DeferredFollowPointFormation> type, Map<Point, Integer> positions, List<UnitFormation> innerFormations, Point deferredPoint, UnitFormation deferredFormation) {
		super(type, positions, innerFormations);
		this.deferredPoint = deferredPoint;
		this.deferredFormation = deferredFormation;
	}
	
	@Override
	public FormationLeaderEntity spawnInnerFormationLeaders(World level, Vector3d pos, float facing, UUID commandGroup, PlayerIDTag owner) {
		if (this.deferredFormation != null) {
			this.deferredLeader = this.deferredFormation.spawnInnerFormationLeaders(level, pos, facing, commandGroup, owner);
		}
		return super.spawnInnerFormationLeaders(level, pos, facing, commandGroup, owner);
	}
	
	@Override
	public void killInnerFormationLeaders() {
		super.killInnerFormationLeaders();
		if (this.deferredLeader != null) this.deferredLeader.kill();
	}
	
	public void setDeferredLeader(FormationLeaderEntity entity) {
		this.deferredLeader = entity;
		if (this.deferredLeader != null) {
			this.deferredFormation = this.deferredLeader.getFormation();
		}
	}
	
	@Override
	public void setFollower(CreatureEntity entity) {
		if (this.deferredLeader != null && this.deferredLeader.isAlive()) {
			this.deferredLeader.setFollower(entity);
		}
	}
	
	@Override
	public <E extends CreatureEntity & IMovesInFormation> boolean addEntity(E entity) {
		if (super.addEntity(entity)) return true;
		return this.deferredLeader == null ? false : this.deferredLeader.addEntity(entity);
	}
	
	@Override
	public void removeEntity(CreatureEntity entity) {
		super.removeEntity(entity);
		if (this.deferredLeader != null) {
			this.deferredLeader.removeEntity(entity);
		}
	}
	
	@Override
	public void tick(FormationLeaderEntity leader) {
		super.tick(leader);
		if (this.formationState == null || this.formationState == State.BROKEN) return;
		
		boolean stopped = UnitFormation.isStopped(leader);
		
		Vector3d leaderForward = new Vector3d(-MathHelper.sin(leader.yRot * RAD_TO_DEG), 0.0d, MathHelper.cos(leader.yRot * RAD_TO_DEG));
		Vector3d leaderRight = new Vector3d(-leaderForward.z, 0.0d, leaderForward.x);
		
		Brain<?> leaderBrain = leader.getBrain();
		
		boolean engagementFlag =
				leaderBrain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
				&& leaderBrain.hasMemoryValue(MemoryModuleTypeInit.ENGAGING_COMPLETED.get())
				&& leaderBrain.getMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get()).get();
		
		CombatMode combatMode = leaderBrain.getMemory(MemoryModuleTypeInit.COMBAT_MODE.get()).orElse(CombatMode.DONT_ATTACK);
		
		LivingEntity target = engagementFlag ? leaderBrain.getMemory(MemoryModuleType.ATTACK_TARGET).get() : null;
		engagementFlag &= target != null && target.isAlive() && combatMode != CombatMode.DONT_ATTACK;
		
		if (this.deferredLeader != null && this.deferredLeader.isAlive()) {
			if (UnitFormation.checkMemoriesForMovement(this.deferredLeader)) {
				Vector3d followPos =
						leader.position()
						.add(leaderForward.scale(this.deferredPoint.z))
						.add(leaderRight.scale(this.deferredPoint.x))
						.add(0.0d, this.deferredLeader.getY() - leader.getY(), 0.0d);
				
				Brain<?> dlBrain = this.deferredLeader.getBrain();
				
				dlBrain.setMemory(MemoryModuleTypeInit.IN_FORMATION.get(), leader);			
				
				if (dlBrain.checkMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), MemoryModuleStatus.REGISTERED)) {
					dlBrain.setMemory(MemoryModuleTypeInit.ENGAGING_COMPLETED.get(), engagementFlag);
				}
				
				if (UnitFormation.checkMemoriesForEngagement(this.deferredLeader) && engagementFlag) {
					dlBrain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
					dlBrain.setMemory(MemoryModuleTypeInit.COMBAT_MODE.get(), combatMode);
				}
				
				if (this.formationState == State.FORMED && stopped && this.deferredLeader.position().closerThan(followPos, CLOSE_ENOUGH)) {
					// Stop and stay oriented
					this.deferredLeader.yRot = leader.yRot;
					this.deferredLeader.yHeadRot = leader.yRot;
				} else {
					Vector3d possiblePos = this.tryFindingNewPosition(this.deferredLeader, followPos);
					if (possiblePos != null && !this.deferredLeader.position().closerThan(possiblePos, CLOSE_ENOUGH)) {
						Brain<?> followerBrain = this.deferredLeader.getBrain();
						followerBrain.setMemory(MemoryModuleTypeInit.PRECISE_POS.get(), possiblePos);
						followerBrain.setMemory(MemoryModuleType.MEETING_POINT, GlobalPos.of(leader.level.dimension(), (new BlockPos(possiblePos)).below()));
					}
				}
			} else {
				this.setFollower(null);
			}
		}
	}
	
	@Override
	public Vector3d getFollowPosition(FormationLeaderEntity leader) {
		return leader.position();
	}
	
	@Override
	public float scoreOrientationAngle(float angle, World level, CreatureEntity leader, Vector3d pos) {
		float score = super.scoreOrientationAngle(angle, level, leader, pos);
		if (this.deferredLeader == null) return score;
		
		Vector3d forward = new Vector3d(-MathHelper.sin(angle * RAD_TO_DEG), 0.0d, MathHelper.cos(angle * RAD_TO_DEG));
		Vector3d right = new Vector3d(-forward.z, 0.0d, forward.x);
		
		Vector3d adjustedPos =
				pos
				.add(forward.scale(this.deferredPoint.z))
				.add(right.scale(this.deferredPoint.x))
				.add(0.0d, this.deferredLeader.getY() - leader.getY(), 0.0d);
		return score + this.deferredLeader.scoreOrientationAngle(angle, adjustedPos);
	}
	
	private static final String TAG_DEFERRED_LEADER = "deferredLeader";
	private static final String TAG_DEFERRED_POINT = "deferredPoint";
	private static final String TAG_X = "x";
	private static final String TAG_Z = "z";
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = super.serializeNBT();
		
		CompoundNBT followPointTag = new CompoundNBT();
		followPointTag.putInt(TAG_X, this.deferredPoint.x);
		followPointTag.putInt(TAG_Z, this.deferredPoint.z);
		nbt.put(TAG_DEFERRED_POINT, followPointTag);
		
		if (this.deferredLeader != null) {
			nbt.putUUID(TAG_DEFERRED_LEADER, this.deferredLeader.getUUID());
		}
		
		return nbt;
	}
	
	@Override
	protected void loadEntityData(CompoundNBT nbt, World level) {
		super.loadEntityData(nbt, level);
		if (level.isClientSide) return;
		ServerWorld slevel = (ServerWorld) level;
		
		CompoundNBT deferredPointTag = nbt.getCompound(TAG_DEFERRED_POINT);
		this.deferredPoint = new Point(deferredPointTag.getInt(TAG_X), deferredPointTag.getInt(TAG_Z));
		
		if (nbt.contains(TAG_DEFERRED_LEADER)) {
			Entity e = slevel.getEntity(nbt.getUUID(TAG_DEFERRED_LEADER));
			if (!(e instanceof FormationLeaderEntity)) return;
			this.setDeferredLeader((FormationLeaderEntity) e);
		}
	}
	
	public static class Builder extends PointFormation.Builder<DeferredFollowPointFormation, Builder> {
		private final Point deferredPoint;
		private final UnitFormation deferredFormation;
		
		public Builder(Point deferredPoint, UnitFormation deferredFormation) {
			super();
			this.deferredPoint = deferredPoint;
			this.deferredFormation = deferredFormation;
		}
		
		@Override
		public Builder getThis() {
			return this;
		}
		
		@Override
		public DeferredFollowPointFormation build() {
			return new DeferredFollowPointFormation(UnitFormationTypeInit.DEFERRED_FOLLOW.get(), this.positions, this.innerFormations, this.deferredPoint, this.deferredFormation);
		}
		
	}
	
}
