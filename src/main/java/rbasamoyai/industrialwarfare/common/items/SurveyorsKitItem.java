package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.blockentities.ConfigurableBounds;

public class SurveyorsKitItem extends Item {

	public static final String TAG_SET_POS = "settingPos";
	public static final String TAG_FIRST_CORNER = "firstCorner";
	public static final String TAG_SETTING_DIMENSION = "settingDimension";
	
	private static final Component DIFFERENT_DIMENSION_TEXT = new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".different_dimensions").withStyle(ChatFormatting.RED);
	
	public SurveyorsKitItem() {
		super(new Item.Properties().stacksTo(1));
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			CompoundTag nbt = stack.getOrCreateTag();
			nbt.remove(TAG_SET_POS);
			nbt.remove(TAG_FIRST_CORNER);
			nbt.remove(TAG_SETTING_DIMENSION);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		BlockPos pos = ctx.getClickedPos();
		ItemStack stack = ctx.getItemInHand();
		CompoundTag nbt = stack.getOrCreateTag();
		
		if (!nbt.contains(TAG_SET_POS)) {
			if (!(level.getBlockEntity(pos) instanceof ConfigurableBounds)) {
				return InteractionResult.FAIL;
			}
			nbt.putIntArray(TAG_SET_POS, new int[] { pos.getX(), pos.getY(), pos.getZ() } );
			nbt.remove(TAG_FIRST_CORNER);
			nbt.putString(TAG_SETTING_DIMENSION, level.dimension().location().toString());
			ctx.getPlayer().getCooldowns().addCooldown(this, 20);
			return InteractionResult.CONSUME;
		}
		if (!this.isSameDimension(level, stack)) {
			nbt.remove(TAG_SET_POS);
			nbt.remove(TAG_FIRST_CORNER);
			nbt.remove(TAG_SETTING_DIMENSION);
			ctx.getPlayer().displayClientMessage(DIFFERENT_DIMENSION_TEXT, true);
			ctx.getPlayer().getCooldowns().addCooldown(this, 20);
			return InteractionResult.FAIL;
		}
		if (!nbt.contains(TAG_FIRST_CORNER)) {
			nbt.putIntArray(TAG_FIRST_CORNER, new int[] { pos.getX(), pos.getY(), pos.getZ() } );
			ctx.getPlayer().getCooldowns().addCooldown(this, 20);
			return InteractionResult.CONSUME;
		}
		int[] arr = nbt.getIntArray(TAG_SET_POS);
		int[] arr1 = nbt.getIntArray(TAG_FIRST_CORNER);
		
		nbt.remove(TAG_SET_POS);
		nbt.remove(TAG_FIRST_CORNER);
		
		if (arr.length != 3 || arr1.length != 3) {
			return InteractionResult.FAIL;
		}
		
		BlockPos setPos = new BlockPos(arr[0], arr[1], arr[2]);
		BlockPos firstCorner = new BlockPos(arr1[0], arr1[1], arr1[2]);
		
		BlockEntity be = level.getBlockEntity(setPos);
		if (!(be instanceof ConfigurableBounds)) {
			return InteractionResult.FAIL;
		}
		((ConfigurableBounds) be).trySettingBounds(ctx.getPlayer(), stack, firstCorner, pos);
		ctx.getPlayer().getCooldowns().addCooldown(this, 20);
		return InteractionResult.CONSUME;
	}
	
	private boolean isSameDimension(Level level, ItemStack stack) {
		CompoundTag nbt = stack.getOrCreateTag();
		if (!nbt.contains(TAG_SETTING_DIMENSION, Tag.TAG_STRING)) return false;
		ResourceLocation loc = new ResourceLocation(nbt.getString(TAG_SETTING_DIMENSION));
		return level.dimension().location().equals(loc);
	}
	
}
