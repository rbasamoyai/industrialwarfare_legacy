package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.core.init.SoundEventInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class MatchCordItem extends Item {
	
	public static final String TAG_LAST_UPDATED_TICK = "lastUpdated";
	public static final String TAG_IS_LIT = "isLit";
	public static final String TAG_MAX_DAMAGE = "maxDamage";
	
	private static final int DEFAULT_MAX_DURABILITY = 20 * 60 * 20; // Lasts one Minecraft day
	
	public MatchCordItem(Item.Properties properties) {
		super(properties.stacksTo(1).tab(IWItemGroups.TAB_GENERAL));
	}
	
	public MatchCordItem() {
		this(new Item.Properties());
	}
	
	@Override
	public int getMaxDamage(ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
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
	public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide) return;
		
		CompoundNBT nbt = stack.getOrCreateTag();
		if (isLit(stack)) {
			if (selected || entity instanceof LivingEntity && ((LivingEntity) entity).getOffhandItem() == stack) {
				((ServerWorld) level).sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.0d, entity.getZ(), 1, 0.0d, 0.0d, 0.0d, 0.01d);
			}
			
			long lastUpdated = nbt.contains(TAG_LAST_UPDATED_TICK, Constants.NBT.TAG_LONG) ? nbt.getLong(TAG_LAST_UPDATED_TICK) : level.getGameTime() - 1;
			int burnTime = (int)(level.getGameTime() - lastUpdated);
			if (entity instanceof LivingEntity) {
				if (stack.hurt(burnTime, random, null)) {
					stack.shrink(1);
					entity.level.playSound(null, entity.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundCategory.NEUTRAL, 1.0F, random.nextFloat() * 0.4F + 0.8F);
				}
			}
			
			BlockState bstate = level.getBlockState(entity.blockPosition());
			if (bstate.getBlock() == Blocks.WATER && (bstate.getValue(FlowingFluidBlock.LEVEL) & 7) == 0) {
				lightMatch(stack, false);
				entity.level.playSound(null, entity.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundCategory.NEUTRAL, 1.0F, random.nextFloat() * 0.4F + 0.8F);
			}
		}
		nbt.putLong(TAG_LAST_UPDATED_TICK, level.getGameTime());
	}
	
	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		if (!entity.level.isClientSide) {
			CompoundNBT nbt = stack.getOrCreateTag();
			if (isLit(stack)) {
				((ServerWorld) entity.level).sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY(), entity.getZ(), 1, 0.0d, 0.0d, 0.0d, 0.01d);
				long lastUpdated = nbt.contains(TAG_LAST_UPDATED_TICK, Constants.NBT.TAG_LONG) ? nbt.getLong(TAG_LAST_UPDATED_TICK) : entity.level.getGameTime() - 1;
				int burnTime = (int)(entity.level.getGameTime() - lastUpdated);
				if (stack.hurt(burnTime, random, null)) {
					stack.shrink(1);
					entity.level.playSound(null, entity.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundCategory.NEUTRAL, 1.0F, random.nextFloat() * 0.4F + 0.8F);
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
					
					entity.level.playSound(null, entity.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundCategory.NEUTRAL, 1.0F, random.nextFloat() * 0.4F + 0.8F);
				}
			}
			nbt.putLong(TAG_LAST_UPDATED_TICK, entity.level.getGameTime());
		}
		return super.onEntityItemUpdate(stack, entity);
	}
	
	@Override
	public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand != Hand.MAIN_HAND) return ActionResult.pass(stack);
		if (!level.isClientSide && isLit(stack)) {
			ItemStack offhand = player.getOffhandItem();
			if (offhand.getItem() instanceof MatchCordItem) {
				lightMatch(offhand, true);
			} else {
				lightMatch(stack, false);
				level.playSound(null, player.blockPosition(), SoundEventInit.EXTINGUISH_MATCH.get(), SoundCategory.NEUTRAL, 1.0F, random.nextFloat() * 0.4F + 0.8F);
			}
		}
		return ActionResult.sidedSuccess(stack, level.isClientSide);
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player.level == null) return super.getDurabilityForDisplay(stack);
		CompoundNBT nbt = stack.getOrCreateTag();
		
		if (isLit(stack)) {
			int adjustedDamage = Math.min(stack.getMaxDamage(), (int)(mc.player.level.getGameTime() - nbt.getLong(TAG_LAST_UPDATED_TICK) + stack.getDamageValue()));
			return (double) adjustedDamage / (double) stack.getMaxDamage();
		}
		
		return super.getDurabilityForDisplay(stack);
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || isLit(oldStack) ^ isLit(newStack);
	}
	
	public static void lightMatch(ItemStack stack, boolean isLit) {
		CompoundNBT nbt = stack.getOrCreateTag();
		nbt.putBoolean(TAG_IS_LIT, isLit);
	}
	
	public static boolean isLit(ItemStack stack) {
		return stack.getOrCreateTag().getBoolean(TAG_IS_LIT);
	}
	
}
