package rbasamoyai.industrialwarfare.common.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.tags.IWBlockTags;
import rbasamoyai.industrialwarfare.core.init.EntityTypeInit;

public class BulletEntity extends ThrowableEntity {

	private static final String DAMAGE_SOURCE_KEY = IndustrialWarfare.MOD_ID + ".bullet";
	
	private static final String TAG_DAMAGE = "damage";
	
	public float damage;
	public float headshotMultiplier;
	
	public BulletEntity(EntityType<? extends BulletEntity> type, World world) {
		this(type, world, 0.0f, 0.0f);
	}
	
	public BulletEntity(World world, LivingEntity owner, float damage, float headshotMultiplier) {
		this(EntityTypeInit.BULLET.get(), world, damage, headshotMultiplier);
		this.setOwner(owner);
		this.setPos(owner.getX(), owner.getEyeY() - 0.1d, owner.getZ());
	}
	
	public BulletEntity(EntityType<? extends BulletEntity> type, World world, float damage, float headshotMultiplier) {
		super(type, world);
		this.damage = damage;
		this.headshotMultiplier = headshotMultiplier; 
	}
	
	@Override
	protected void defineSynchedData() {	
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	@Override
	protected void onHitEntity(EntityRayTraceResult result) {
		super.onHitEntity(result);
		Entity hit = result.getEntity();
		float damage = this.damage;
		if (MathHelper.abs((float)(this.getEyeY() - hit.getEyeY())) < 0.2f) {
			damage *= this.headshotMultiplier;
			Entity owner = this.getOwner();
			if (owner instanceof PlayerEntity) {
				owner.level.playSound(null, owner.blockPosition(), SoundEvents.ARROW_HIT_PLAYER, SoundCategory.MASTER, 1.0f, 1.0f);
			}
		}
		hit.hurt(new IndirectEntityDamageSource(DAMAGE_SOURCE_KEY, this, this.getOwner()), damage);
		hit.invulnerableTime = 0;
		this.remove();
	}
	
	@Override
	protected void onHitBlock(BlockRayTraceResult result) {
		BlockPos pos = result.getBlockPos();
		BlockState blockstate = this.level.getBlockState(pos);
		Block block = blockstate.getBlock();
		
		if (block == Blocks.TNT) {
			Entity owner = this.getOwner();
			blockstate.catchFire(this.level, pos, result.getDirection(), owner instanceof LivingEntity ? (LivingEntity) owner : null);
			this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		} else if (block.is(IWBlockTags.SHATTERABLE)) {
			this.level.destroyBlock(pos, false);
		} else if (block == Blocks.MELON) {
			this.level.destroyBlock(pos, true);
		}
		
		this.remove();
	}
	
	@Override
	public void addAdditionalSaveData(CompoundNBT tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat(TAG_DAMAGE, this.damage);
		tag.putFloat("headshotMultiplier", this.headshotMultiplier);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundNBT tag) {
		this.damage = tag.getFloat(TAG_DAMAGE);
		this.headshotMultiplier = tag.getFloat("headshotMultiplier");
		super.readAdditionalSaveData(tag);
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	@Override
	protected float getGravity() {
		return 0.01f;
	}

}
