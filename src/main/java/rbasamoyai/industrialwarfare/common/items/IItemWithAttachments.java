package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public interface IItemWithAttachments {
	
	boolean canCustomize(ItemStack stack);
	INamedContainerProvider getAttachmentsContainerProvider(ItemStack stack);
	default void writeContainerInfo(PacketBuffer buf) {}
	
}
