package rbasamoyai.industrialwarfare.common.entities;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.MovesInFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;
import rbasamoyai.industrialwarfare.common.entityai.tasks.MoveToEngagementDistance;
import rbasamoyai.industrialwarfare.common.entityai.tasks.PreciseWalkToPositionTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.WalkToTargetSpecialTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.WalkTowardsPosNoDelayTask;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.Interval;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;

public class FormationLeaderEntity extends PathfinderMob implements MovesInFormation {

	protected static final Supplier<List<MemoryModuleType<?>>> MEMORY_TYPES = () -> ImmutableList.of(
			MemoryModuleType.ATTACK_TARGET,
			MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
			MemoryModuleType.LOOK_TARGET,
			MemoryModuleType.MEETING_POINT,
			MemoryModuleType.PATH,
			MemoryModuleType.WALK_TARGET,
			MemoryModuleTypeInit.COMBAT_MODE.get(),
			MemoryModuleTypeInit.ENGAGING_COMPLETED.get(),
			MemoryModuleTypeInit.IN_COMMAND_GROUP.get(),
			MemoryModuleTypeInit.PRECISE_POS.get()
			);
	
	private UnitFormation formation;
	@Nullable
	private PlayerIDTag owner;
	@Nullable
	private long orderLastReceived;
	
	public FormationLeaderEntity(EntityType<? extends FormationLeaderEntity> type, Level level) {
		this(type, level, UnitFormationTypeInit.LINE.get().getFormation(-1));
	}
	
	public FormationLeaderEntity(EntityType<? extends FormationLeaderEntity> type, Level level, UnitFormation formation) {
		super(type, level);
		this.formation = formation;
		this.updateOrderTime();
		this.setPersistenceRequired();
		this.setInvulnerable(true);
	}
	
	public static AttributeSupplier.Builder setAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.1d)
				.add(Attributes.MAX_HEALTH, 20.0d)
				.add(Attributes.FOLLOW_RANGE, 100.0d);
	}
	
	/*
	 * AI METHODS
	 */
	
	@Override
	protected Brain.Provider<FormationLeaderEntity> brainProvider() {
		return Brain.provider(MEMORY_TYPES.get(), ImmutableList.of());
	}
	
	@Override
	protected Brain<?> makeBrain(Dynamic<?> input) {
		Brain<FormationLeaderEntity> brain = this.brainProvider().makeBrain(input);
		brain.addActivity(Activity.CORE, getCorePackage());
		brain.setDefaultActivity(Activity.CORE);
		brain.setActiveActivityIfPossible(Activity.CORE);
		return brain;
	}
	
	private static ImmutableList<Pair<Integer, ? extends Behavior<? super FormationLeaderEntity>>> getCorePackage() {
		return ImmutableList.of(
				Pair.of(0, new WalkToTargetSpecialTask()),
				Pair.of(0, new PreciseWalkToPositionTask(1.5f, 1.5d, 0.07d, true)),
				Pair.of(0, new LookAtTargetSink(45, 90)),
				Pair.of(1, new WalkTowardsPosNoDelayTask(MemoryModuleType.MEETING_POINT, 2.0f, 1, 100)),
				Pair.of(2, new MoveToEngagementDistance(50))
				);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Brain<FormationLeaderEntity> getBrain() {
		return (Brain<FormationLeaderEntity>) super.getBrain();
	}
	
	@Override
	protected void customServerAiStep() {
		Brain<FormationLeaderEntity> brain = this.getBrain();
		brain.tick((ServerLevel) this.level, this);
		super.customServerAiStep();
	}
	
	@Override
	public void tick() {
		super.tick();
		if (this.level.isClientSide) {
			this.level.addParticle(new DustParticleOptions(new Vector3f(0.0f, 1.0f, 0.0f), 1.0f), this.getX(), this.getY() + this.getBbHeight() + 0.25d, this.getZ(), 0.0d, 0.0d, 0.0d);
			this.level.addParticle(new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f), this.getX() - Math.sin(Math.toRadians(this.getYRot())), this.getY() + this.getBbHeight() + 0.25d, this.getZ() + Math.cos(Math.toRadians(this.getYRot())), 0.0d, 0.0d, 0.0d);
		} else {
			this.formation.doTick(this);
		}
	}
	
	public void setOwner(PlayerIDTag owner) { this.owner = owner; }
	public PlayerIDTag getOwner() { return this.owner; }
	
	/*
	 * DATA METHODS
	 */
	
	private static final String TAG_FORMATION = "formation";
	private static final String TAG_TYPE = "type";
	private static final String TAG_DATA = "data";
	
	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		CompoundTag formationData = new CompoundTag();
		formationData.putString(TAG_TYPE, this.formation.getType().getRegistryName().toString());
		formationData.put(TAG_DATA, this.formation.serializeNBT());
		nbt.put(TAG_FORMATION, formationData);
		if (this.owner != null) {
			nbt.put("owner", this.owner.serializeNBT());
		}
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		CompoundTag formationData = nbt.getCompound(TAG_FORMATION);
		UnitFormationType<?> type = IWModRegistries.UNIT_FORMATION_TYPES.get().getValue(new ResourceLocation(formationData.getString(TAG_TYPE)));
		this.formation = type.getFormation(-1);
		this.formation.deserializeNBT(formationData.getCompound(TAG_DATA));
		if (nbt.contains("owner", Tag.TAG_COMPOUND)) {
			this.owner = PlayerIDTag.fromNBT(nbt.getCompound("owner"));
		}
	}
	
	/*
	 * FORMATION METHODS
	 */
	
	public UnitFormation getFormation() {
		return this.formation;
	}
	
	public <E extends PathfinderMob & MovesInFormation> boolean addEntity(E entity) {
		return this.formation.addEntity(entity);
	}
	
	public void removeEntity(PathfinderMob entity) {
		this.formation.removeEntity(entity);
	}
	
	public boolean hasMatchingFormationLeader(FormationLeaderEntity inFormationWith) {
		return this.equals(inFormationWith) || this.formation.isInFormationWith(inFormationWith);
	}
	
	public void setFollower(PathfinderMob entity) {
		this.formation.setFollower(entity);
	}
	
	public void setState(UnitFormation.State state) {
		this.formation.setState(state);	
	}
	
	public void setAttackInterval(Interval interval) {
		this.formation.setAttackInterval(interval);
	}
	
	public void setAttackType(FormationAttackType attackType) {
		this.formation.setAttackType(attackType);
	}
	
	public float scoreOrientationAngle(float angle, Vec3 pos) {
		return this.formation.scoreOrientationAngle(angle, this.level, this, pos);
	}
	
	public Vec3 getFollowPosition() {
		return this.formation.getFollowPosition(this);
	}
	
	public void updateOrderTime() {
		this.orderLastReceived = this.level.getGameTime();
		if (this.formation != null) this.formation.updateOrderTime();
	}
	
	public long getLastOrderTime() {
		return this.orderLastReceived;
	}
	
	@Override
	public int getFormationRank() {
		return this.formation == null ? -1 : this.formation.getLeaderRank();
	}
	
	@Override
	public boolean isLowLevelUnit() {
		return false;
	}
	
	@Override
	public void kill() {
		super.kill();
		this.formation.killInnerFormationLeaders();
	}
	
	/*
	 * "Decreaturefying" the formation leader
	 */
	
	@Override protected void pushEntities() {}
	@Override public boolean isPushable() { return false; }
	@Override public boolean isSilent() { return true; }
	@Override public boolean canBeAffected(MobEffectInstance effect) { return false; }
	@Override public void knockback(double a, double b, double c) {}
	@Override public boolean canSpawnSprintParticle() { return false; }
	@Override public boolean isInvulnerable() { return true; }
	@Override protected void tickDeath() { this.discard();; }
	@Override public boolean isPickable() { return false; }
	
}
