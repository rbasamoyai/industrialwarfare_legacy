package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.matchcoil.MatchCoilMenu;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;

public class MatchCoilItem extends BlockItem {

	public static final String TAG_MAX_DAMAGE = "maxDamage";
	
	/* 6000000 ticks is equivalent to 250 cords that last 1 Minecraft day (20 minutes). */
	public static final int DEFAULT_MAX_DAMAGE = 20 * 60 * 20 * 250;
	
	private static final Component TITLE = new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".match_coil");
	
	public MatchCoilItem() {
		super(BlockInit.MATCH_COIL.get(), new Item.Properties().defaultDurability(DEFAULT_MAX_DAMAGE).tab(IWItemGroups.TAB_GENERAL));
	}
	
	@Override
	public int getMaxDamage(ItemStack stack) {
		CompoundTag nbt = stack.getOrCreateTag();
		if (nbt.getInt(TAG_MAX_DAMAGE) <= 0) {
			nbt.putInt(TAG_MAX_DAMAGE, DEFAULT_MAX_DAMAGE);
		}
		return nbt.getInt(TAG_MAX_DAMAGE);
	}
	
	@Override public boolean isDamaged(ItemStack stack) { return true; }
	@Override public boolean isBarVisible(ItemStack stack) { return false; }
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (hand == InteractionHand.OFF_HAND || player.getOffhandItem().getItem() != Items.SHEARS) {
			return InteractionResultHolder.pass(stack);
		}
		if (!level.isClientSide) {
			NetworkHooks.openGui((ServerPlayer) player, new SimpleMenuProvider(MatchCoilMenu.getServerContainerProvider(stack), TITLE),
					buf -> {
						buf.writeVarInt(stack.getMaxDamage() - stack.getDamageValue());
					});
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

}
