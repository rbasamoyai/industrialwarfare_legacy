package rbasamoyai.industrialwarfare.common.items;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.industrialwarfare.core.init.SoundEventInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class MatchCordItem extends Item {
	
	public static final String TAG_LAST_UPDATED_TICK = "lastUpdated";
	public static final String TAG_IS_LIT = "isLit";
	public static final String TAG_MAX_DAMAGE = "maxDamage";
	
	private static final int DEFAULT_MAX_DURABILITY = 20 * 60 * 20; // Lasts one Minecraft day
	
	private final Random random = new Random();
	
	public MatchCordItem(Item.Properties properties) {
		super(properties.stacksTo(1).tab(IWItemGroups.TAB_GENERAL));
	}
	
	public MatchCordItem() {
		this(new Item.Properties());
	}
	
	@Override
	public int getMaxDamage(ItemStack stack) {
		CompoundTag nbt = stack.getOrCreateTag();
		if (nbt.getInt(TAG_MAX_DAMAGE) <= 0) {
			nbt.putInt(TAG_MAX_DAMAGE, DEFAULT_MAX_DURABILITY);
		}
		return nbt.getInt(TAG_MAX_DAMAGE);
	}
	
	@Override
	public boolean isDamageable(ItemStack stack) {
		return this.getMaxDamage(stack) > 0;
	}
	
	@Override
	public boolean isDamaged(ItemStack stack) {
		return true;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide) return;
		
		CompoundTag nbt = stack.getOrCreateTag();
		if (isLit(stack)) {
			if (selected || entity instanceof LivingEntity && ((LivingEntity) entity).getOffhandItem() == stack) {
				((ServerLevel) level).sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.0d, entity.getZ(), 1, 0.0d, 0.0d, 0.0d, 0.01d);
			}
			
			long lastUpdated = nbt.contains(TAG_LAST_UPDATED_TICK, Tag.TAG_LONG) ? nbt.getLong(TAG_LAST_UPDATED_TICK) : level.getGameTime() - 1;
			int burnTime = (int)(level.getGameTime() - lastUpdated);
			if (entity instanceof LivingEntity) {
				if (stack.hurt(burnTime, this.random, null)) {
					stack.shrink(1);
					entity.level.playSound(null, entity.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundSource.NEUTRAL, 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
				}
			}
			
			BlockState bstate = level.getBlockState(entity.blockPosition());
			if (bstate.getBlock() == Blocks.WATER && (bstate.getValue(LiquidBlock.LEVEL) & 7) == 0) {
				lightMatch(stack, false);
				entity.level.playSound(null, entity.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundSource.NEUTRAL, 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
			}
		}
		nbt.putLong(TAG_LAST_UPDATED_TICK, level.getGameTime());
	}
	
	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		if (!entity.level.isClientSide) {
			CompoundTag nbt = stack.getOrCreateTag();
			if (isLit(stack)) {
				((ServerLevel) entity.level).sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY(), entity.getZ(), 1, 0.0d, 0.0d, 0.0d, 0.01d);
				long lastUpdated = nbt.contains(TAG_LAST_UPDATED_TICK, Tag.TAG_LONG) ? nbt.getLong(TAG_LAST_UPDATED_TICK) : entity.level.getGameTime() - 1;
				int burnTime = (int)(entity.level.getGameTime() - lastUpdated);
				if (stack.hurt(burnTime, this.random, null)) {
					stack.shrink(1);
					entity.level.playSound(null, entity.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundSource.NEUTRAL, 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
				}
				
				BlockState bstate = entity.level.getBlockState(entity.blockPosition());
				if (bstate.getBlock() == Blocks.WATER) {
					ItemStack newMatch = stack.copy();
					lightMatch(newMatch, false);
					stack.shrink(1);
					ItemEntity newItem = new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), newMatch);
					entity.level.addFreshEntity(newItem);
					
					newItem.setPickUpDelay(10);
					newItem.setDeltaMovement(entity.getDeltaMovement());
					
					entity.level.playSound(null, entity.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundSource.NEUTRAL, 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
				}
			}
			nbt.putLong(TAG_LAST_UPDATED_TICK, entity.level.getGameTime());
		}
		return super.onEntityItemUpdate(stack, entity);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
		if (!level.isClientSide && isLit(stack)) {
			ItemStack offhand = player.getOffhandItem();
			if (offhand.getItem() instanceof MatchCordItem) {
				lightMatch(offhand, true);
			} else {
				lightMatch(stack, false);
				level.playSound(null, player.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundSource.NEUTRAL, 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}
	
	@Override
	public int getBarColor(ItemStack pStack) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player.level == null) return super.getBarColor(pStack);
		
		CompoundTag tag = pStack.getOrCreateTag();
		
		if (isLit(pStack)) {
			float maxDamage = (float) pStack.getMaxDamage();
			float adjustedDamage = (float) Math.min(maxDamage, (int)(mc.player.level.getGameTime() - tag.getLong(TAG_LAST_UPDATED_TICK) + pStack.getDamageValue()));
			float f = Math.max(0.0f, (maxDamage - adjustedDamage) / maxDamage);
			return Mth.hsvToRgb(f / 3.0f, 1.0f, 1.0f);
		}
		
		return super.getBarColor(pStack);
	}
	
	@Override
	public int getBarWidth(ItemStack pStack) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player.level == null) return super.getBarColor(pStack);
		
		CompoundTag tag = pStack.getOrCreateTag();
		
		if (isLit(pStack)) {
			float adjustedDamage = (float) Math.min(pStack.getMaxDamage(), (int)(mc.player.level.getGameTime() - tag.getLong(TAG_LAST_UPDATED_TICK) + pStack.getDamageValue()));
			return Math.round(13.0f - adjustedDamage * 13.0f / (float) pStack.getMaxDamage());
		}
		
		return super.getBarWidth(pStack);
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || isLit(oldStack) ^ isLit(newStack);
	}
	
	public static void lightMatch(ItemStack stack, boolean isLit) {
		CompoundTag nbt = stack.getOrCreateTag();
		nbt.putBoolean(TAG_IS_LIT, isLit);
	}
	
	public static boolean isLit(ItemStack stack) {
		return stack.getOrCreateTag().getBoolean(TAG_IS_LIT);
	}
	
}
