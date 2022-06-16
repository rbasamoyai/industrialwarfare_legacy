package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.tileentities.IConfigurableBounds;

public class SurveyorsKitItem extends Item {

	public static final String TAG_SET_POS = "settingPos";
	public static final String TAG_FIRST_CORNER = "firstCorner";
	public static final String TAG_SETTING_DIMENSION = "settingDimension";
	
	private static final ITextComponent DIFFERENT_DIMENSION_TEXT = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".different_dimensions").withStyle(TextFormatting.RED);
	
	public SurveyorsKitItem() {
		super(new Item.Properties().stacksTo(1));
	}
	
	@Override
	public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			CompoundNBT nbt = stack.getOrCreateTag();
			nbt.remove(TAG_SET_POS);
			nbt.remove(TAG_FIRST_CORNER);
			nbt.remove(TAG_SETTING_DIMENSION);
		}
		return ActionResult.sidedSuccess(stack, level.isClientSide);
	}
	
	@Override
	public ActionResultType useOn(ItemUseContext ctx) {
		World level = ctx.getLevel();
		if (level.isClientSide) {
			return ActionResultType.SUCCESS;
		}
		BlockPos pos = ctx.getClickedPos();
		ItemStack stack = ctx.getItemInHand();
		CompoundNBT nbt = stack.getOrCreateTag();
		
		if (!nbt.contains(TAG_SET_POS)) {
			if (!(level.getBlockEntity(pos) instanceof IConfigurableBounds)) {
				return ActionResultType.FAIL;
			}
			nbt.putIntArray(TAG_SET_POS, new int[] { pos.getX(), pos.getY(), pos.getZ() } );
			nbt.remove(TAG_FIRST_CORNER);
			nbt.putString(TAG_SETTING_DIMENSION, level.dimension().location().toString());
			ctx.getPlayer().getCooldowns().addCooldown(this, 20);
			return ActionResultType.CONSUME;
		}
		if (!this.isSameDimension(level, stack)) {
			nbt.remove(TAG_SET_POS);
			nbt.remove(TAG_FIRST_CORNER);
			nbt.remove(TAG_SETTING_DIMENSION);
			ctx.getPlayer().displayClientMessage(DIFFERENT_DIMENSION_TEXT, true);
			ctx.getPlayer().getCooldowns().addCooldown(this, 20);
			return ActionResultType.FAIL;
		}
		if (!nbt.contains(TAG_FIRST_CORNER)) {
			nbt.putIntArray(TAG_FIRST_CORNER, new int[] { pos.getX(), pos.getY(), pos.getZ() } );
			ctx.getPlayer().getCooldowns().addCooldown(this, 20);
			return ActionResultType.CONSUME;
		}
		int[] arr = nbt.getIntArray(TAG_SET_POS);
		int[] arr1 = nbt.getIntArray(TAG_FIRST_CORNER);
		
		nbt.remove(TAG_SET_POS);
		nbt.remove(TAG_FIRST_CORNER);
		
		if (arr.length != 3 || arr1.length != 3) {
			return ActionResultType.FAIL;
		}
		
		BlockPos setPos = new BlockPos(arr[0], arr[1], arr[2]);
		BlockPos firstCorner = new BlockPos(arr1[0], arr1[1], arr1[2]);
		
		TileEntity te = level.getBlockEntity(setPos);
		if (!(te instanceof IConfigurableBounds)) {
			return ActionResultType.FAIL;
		}
		((IConfigurableBounds) te).trySettingBounds(ctx.getPlayer(), stack, firstCorner, pos);
		ctx.getPlayer().getCooldowns().addCooldown(this, 20);
		return ActionResultType.CONSUME;
	}
	
	private boolean isSameDimension(World level, ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		if (!nbt.contains(TAG_SETTING_DIMENSION, Constants.NBT.TAG_STRING)) return false;
		ResourceLocation loc = new ResourceLocation(nbt.getString(TAG_SETTING_DIMENSION));
		return level.dimension().location().equals(loc);
	}
	
}
