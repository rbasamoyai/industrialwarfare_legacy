package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;

public interface ItemWithScreen {
	
	boolean canOpenScreen(ItemStack stack);
	MenuProvider getItemContainerProvider(ItemStack stack);
	default void writeContainerInfo(FriendlyByteBuf buf, ItemStack stack) {}
	
}
