package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.common.items.IItemWithAttachments;

public class SOpenAttachmentsScreen {
	public SOpenAttachmentsScreen() {}
	public static void encode(SOpenAttachmentsScreen msg, PacketBuffer buf) {}
	public static SOpenAttachmentsScreen decode(PacketBuffer buf) { return new SOpenAttachmentsScreen(); }
	
	public static void handle(SOpenAttachmentsScreen msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ServerPlayerEntity player = context.getSender();
			ItemStack stackToCustomize = player.getMainHandItem();
			Item item = stackToCustomize.getItem();
			if (item instanceof IItemWithAttachments) {
				IItemWithAttachments attachmentsItem = (IItemWithAttachments) item;
				if (!attachmentsItem.canCustomize(stackToCustomize)) return;
				NetworkHooks.openGui(player, attachmentsItem.getAttachmentsContainerProvider(stackToCustomize), attachmentsItem::writeContainerInfo);
			}
		});
		context.setPacketHandled(true);
	}
}

