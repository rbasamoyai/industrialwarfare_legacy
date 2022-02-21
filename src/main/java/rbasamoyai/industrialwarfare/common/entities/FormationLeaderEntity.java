package rbasamoyai.industrialwarfare.common.entities;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Brain.BrainCodec;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.LineFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation;
import rbasamoyai.industrialwarfare.common.entityai.formation.formations.UnitFormation.State;
import rbasamoyai.industrialwarfare.common.entityai.tasks.MoveToEngagementDistance;
import rbasamoyai.industrialwarfare.common.entityai.tasks.PreciseWalkToPositionTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.WalkToTargetSpecialTask;
import rbasamoyai.industrialwarfare.common.entityai.tasks.WalkTowardsPosNoDelayTask;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

public class FormationLeaderEntity extends CreatureEntity {

	protected static final Supplier<List<MemoryModuleType<?>>> MEMORY_TYPES = () -> ImmutableList.of(
			MemoryModuleType.ATTACK_TARGET,
			MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
			MemoryModuleType.MEETING_POINT,
			MemoryModuleType.PATH,
			MemoryModuleType.WALK_TARGET,
			MemoryModuleTypeInit.COMBAT_MODE.get(),
			MemoryModuleTypeInit.ENGAGING_COMPLETED.get(),
			MemoryModuleTypeInit.PRECISE_POS.get()
			);
	
	private UnitFormation formation;
	@Nullable
	private PlayerIDTag owner;
	
	public FormationLeaderEntity(EntityType<? extends FormationLeaderEntity> type, World level) {
		this(type, level, new LineFormation(level, -1, 0, 0));
	}
	
	public FormationLeaderEntity(EntityType<? extends FormationLeaderEntity> type, World level, UnitFormation formation) {
		super(type, level);
		this.formation = formation;
		this.setPersistenceRequired();
		this.setInvulnerable(true);
	}
	
	public static AttributeModifierMap.MutableAttribute setAttributes() {
		return CreatureEntity.createMobAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.1d)
				.add(Attributes.MAX_HEALTH, 20.0d)
				.add(Attributes.FOLLOW_RANGE, 100.0d);
	}
	
	/*
	 * AI METHODS
	 */
	
	@Override
	protected BrainCodec<FormationLeaderEntity> brainProvider() {
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
	
	private static ImmutableList<Pair<Integer, ? extends Task<? super FormationLeaderEntity>>> getCorePackage() {
		return ImmutableList.of(
				Pair.of(0, new WalkToTargetSpecialTask()),
				Pair.of(0, new WalkTowardsPosNoDelayTask(MemoryModuleType.MEETING_POINT, 2.0f, 1, 100)),
				Pair.of(0, new PreciseWalkToPositionTask(2.0f, 1.5d, 0.125d)),
				Pair.of(1, new MoveToEngagementDistance(14))
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
		if (this.formation.getState() == State.FORMED) {
			brain.tick((ServerWorld) this.level, this);
		}
		super.customServerAiStep();
	}
	
	@Override
	public void tick() {
		super.tick();
		if (this.level.isClientSide) {
			this.level.addParticle(new RedstoneParticleData(0.0f, 1.0f, 0.0f, 1.0f), this.getX(), this.getY() + this.getBbHeight() + 0.25d, this.getZ(), 0.0d, 0.0d, 0.0d);
			this.level.addParticle(new RedstoneParticleData(1.0f, 0.0f, 0.0f, 1.0f), this.getX() - Math.sin(Math.toRadians(this.yRot)), this.getY() + this.getBbHeight() + 0.25d, this.getZ() + Math.cos(Math.toRadians(this.yRot)), 0.0d, 0.0d, 0.0d);
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
	public void addAdditionalSaveData(CompoundNBT nbt) {
		super.addAdditionalSaveData(nbt);
		CompoundNBT formationData = new CompoundNBT();
		formationData.putString(TAG_TYPE, this.formation.getType().getRegistryName().toString());
		formationData.put(TAG_DATA, this.formation.serializeNBT());
		nbt.put(TAG_FORMATION, formationData);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundNBT nbt) {
		super.readAdditionalSaveData(nbt);
		CompoundNBT formationData = nbt.getCompound(TAG_FORMATION);
		UnitFormationType<?> type = IWModRegistries.UNIT_FORMATION_TYPES.getValue(new ResourceLocation(formationData.getString(TAG_TYPE)));
		this.formation = type.getFormation(this.level);
		this.formation.deserializeNBT(formationData.getCompound(TAG_DATA));
	}
	
	/*
	 * FORMATION METHODS
	 */
	
	public boolean addEntity(CreatureEntity entity) {
		return this.formation.addEntity(entity);
	}
	
	public void setState(UnitFormation.State state) {
		this.formation.setState(state);	
	}
	
	/* "Decreaturefying" the formation leader */
	
	@Override protected void pushEntities() {}
	@Override public boolean isPushable() { return false; }
	@Override protected SoundEvent getDeathSound() { return null; }
	@Override protected SoundEvent getFallDamageSound(int dist) { return null; }
	@Override protected SoundEvent getSwimSound() { return null; }
	@Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
	@Override public boolean canBeAffected(EffectInstance effect) { return false; }
	@Override public void knockback(float a, double b, double c) {}
	@Override public boolean canSpawnSprintParticle() { return false; }
	@Override public boolean isInvulnerable() { return true; }
	@Override protected void tickDeath() { this.remove(false); }
	@Override public boolean isPickable() { return false; }
	
}
