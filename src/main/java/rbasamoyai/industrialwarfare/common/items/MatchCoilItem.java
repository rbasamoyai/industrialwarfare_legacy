package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.MatchCoilContainer;
import rbasamoyai.industrialwarfare.common.tileentities.MatchCoilTileEntity;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class MatchCoilItem extends BlockItem {

	public static final String TAG_MAX_DAMAGE = "maxDamage";
	
	/* 6000000 ticks is equivalent to 250 cords that last 1 Minecraft day (20 minutes). */
	public static final int DEFAULT_MAX_DAMAGE = 20 * 60 * 20 * 250;
	
	private static final ITextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".match_coil");
	
	public MatchCoilItem() {
		super(BlockInit.MATCH_COIL.get(), new Item.Properties().defaultDurability(DEFAULT_MAX_DAMAGE).tab(IWItemGroups.TAB_GENERAL));
	}
	
	@Override
	public int getMaxDamage(ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		if (nbt.getInt(TAG_MAX_DAMAGE) <= 0) {
			nbt.putInt(TAG_MAX_DAMAGE, DEFAULT_MAX_DAMAGE);
		}
		return nbt.getInt(TAG_MAX_DAMAGE);
	}
	
	@Override public boolean isDamaged(ItemStack stack) { return true; }
	@Override public boolean showDurabilityBar(ItemStack stack) { return false; }
	
	@Override
	public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand == Hand.OFF_HAND || player.getOffhandItem().getItem() != Items.SHEARS) {
			return ActionResult.pass(stack);
		}
		if (!level.isClientSide) {
			NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(MatchCoilContainer.getServerContainerProvider(stack), TITLE),
					buf -> {
						buf.writeVarInt(stack.getMaxDamage() - stack.getDamageValue());
					});
		}
		return ActionResult.sidedSuccess(stack, level.isClientSide);
	}
	
	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, World level, PlayerEntity player,
			ItemStack stack, BlockState state) {
		boolean flag = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
		if (!level.isClientSide && !flag) {
			TileEntity te = level.getBlockEntity(pos);
			if (te instanceof MatchCoilTileEntity) {
				MatchCoilTileEntity coil = (MatchCoilTileEntity) te;
				coil.setMaxDamage(this.getMaxDamage(stack));
				coil.setCoilDamage(stack.getDamageValue());
				te.setChanged();
			} else {
				flag = false;
			}
			
		}
		return flag;
	}

}
