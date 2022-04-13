package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public interface IItemWithScreen {
	
	boolean canOpenScreen(ItemStack stack);
	INamedContainerProvider getItemContainerProvider(ItemStack stack);
	default void writeContainerInfo(PacketBuffer buf, ItemStack stack) {}
	
}
