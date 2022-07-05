package rbasamoyai.industrialwarfare.common.entities;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;
import rbasamoyai.industrialwarfare.core.init.items.PartItemInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.FirearmActionMessages;

public class BulletEntity extends ThrowableItemProjectile {

	private static final String DAMAGE_SOURCE_KEY = IndustrialWarfare.MOD_ID + ".bullet";
	
	private static final String TAG_DAMAGE = "damage";
	private static final String TAG_HEADSHOT_MULTIPLIER = "headshotMultiplier";
	private static final String TAG_ORIGIN = "origin";
	
	private float damage;
	private float headshotMultiplier;
	private Vec3 origin;
	
	public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
		this(type, level, 0.0f, 0.0f, Vec3.ZERO);
	}
	
	public BulletEntity(Level level, LivingEntity owner, float damage, float headshotMultiplier) {
		this(EntityTypeInit.BULLET.get(), level, damage, headshotMultiplier, new Vec3(owner.getX(), owner.getEyeY(), owner.getZ()));
		this.setOwner(owner);
		this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
	}
	
	public BulletEntity(EntityType<? extends BulletEntity> type, Level level, float damage, float headshotMultiplier, Vec3 origin) {
		super(type, level);
		this.damage = damage;
		this.headshotMultiplier = headshotMultiplier;
		this.origin = origin; 
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	private static final int HIT_NULL_DISTANCE = 5;
	
	@Override
	protected boolean canHitEntity(Entity entity) {
		Entity owner = this.getOwner();
		
		if (owner != null && owner instanceof LivingEntity && owner.position().closerThan(entity.position(), HIT_NULL_DISTANCE)) {
			Brain<?> ownerBrain = ((LivingEntity) owner).getBrain();
			if (ownerBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())) {
				UUID ownerCommandGroup = ownerBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get();
				if (entity instanceof LivingEntity) {
					Brain<?> hitBrain = ((LivingEntity) entity).getBrain();
					if (hitBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())
						&& hitBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get().equals(ownerCommandGroup)) {
						return false;
					}
				}
				Entity controller = entity.getControllingPassenger();
				if (controller != null && controller instanceof LivingEntity) {
					Brain<?> controllerBrain = ((LivingEntity) controller).getBrain();
					if (controllerBrain.hasMemoryValue(MemoryModuleTypeInit.IN_COMMAND_GROUP.get())
						&& controllerBrain.getMemory(MemoryModuleTypeInit.IN_COMMAND_GROUP.get()).get().equals(ownerCommandGroup)) {
						return false;
					}
				}
			}
		}
		return super.canHitEntity(entity);
	}
	
	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		Entity hit = result.getEntity();
		Entity owner = this.getOwner();
		float damage = this.damage;
		if (Mth.abs((float)(this.getEyeY() - hit.getEyeY())) < 0.2f) {
			damage *= this.headshotMultiplier;
			if (!this.level.isClientSide && owner instanceof ServerPlayer) {
				IWNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) owner), new FirearmActionMessages.CNotifyHeadshot());
			}
		}
		hit.invulnerableTime = 0;
		hit.hurt(new IndirectEntityDamageSource(DAMAGE_SOURCE_KEY, this, this.getOwner()), damage);
		this.discard();
	}
	
	@Override
	protected void onHitBlock(BlockHitResult result) {
		BlockPos pos = result.getBlockPos();
		BlockState blockstate = this.level.getBlockState(pos);
		Block block = blockstate.getBlock();
		
		boolean shouldRemove = true;
		
		if (blockstate.is(Blocks.TNT)) {
			Entity owner = this.getOwner();
			blockstate.onCaughtFire(this.level, pos, result.getDirection(), owner instanceof LivingEntity ? (LivingEntity) owner : null);
			this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		} else if (blockstate.is(rbasamoyai.industrialwarfare.common.ModTags.Blocks.SHATTERABLE)) {
			this.level.destroyBlock(pos, false);
			shouldRemove = false;
		} else if (blockstate.is(Blocks.MELON)) {
			this.level.destroyBlock(pos, true);
			shouldRemove = false;
		} else {
			if (!this.level.isClientSide) {
				blockstate.onProjectileHit(level, blockstate, result, this);
				SoundType type = block.getSoundType(blockstate, this.level, pos, this);
				float pitch = 0.9f + 0.2f * this.random.nextFloat();
				this.level.playSound(null, this.getX(), this.getY(), this.getZ(), type.getBreakSound(), this.getSoundSource(), 0.5f, pitch);
			}
		}
		
		if (shouldRemove && !this.level.isClientSide) {
			Vec3 hitPos = result.getLocation();
			int count = 20 + random.nextInt(21);
			
			((ServerLevel) this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), hitPos.x, hitPos.y, hitPos.z, count, 0.0d, 0.0d, 0.0d, 0.02d);
			this.discard();
		}
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat(TAG_DAMAGE, this.damage);
		tag.putFloat(TAG_HEADSHOT_MULTIPLIER, this.headshotMultiplier);
		
		CompoundTag originTag = new CompoundTag();
		originTag.putDouble("x", this.origin.x);
		originTag.putDouble("y", this.origin.y);
		originTag.putDouble("z", this.origin.z);
		tag.put(TAG_ORIGIN, originTag);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		this.damage = tag.getFloat(TAG_DAMAGE);
		this.headshotMultiplier = tag.getFloat(TAG_HEADSHOT_MULTIPLIER);
		
		CompoundTag originTag = tag.getCompound(TAG_ORIGIN);
		this.origin = new Vec3(originTag.getDouble("x"), originTag.getDouble("y"), originTag.getDouble("z"));
		
		super.readAdditionalSaveData(tag);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	@Override
	protected float getGravity() {
		return 0.01f;
	}

	@Override
	protected Item getDefaultItem() {
		return PartItemInit.PART_BULLET.get();
	}

}
