package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.common.items.IItemWithScreen;

public class SOpenItemScreen {
	public SOpenItemScreen() {}
	public static void encode(SOpenItemScreen msg, PacketBuffer buf) {}
	public static SOpenItemScreen decode(PacketBuffer buf) { return new SOpenItemScreen(); }
	
	public static void handle(SOpenItemScreen msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ServerPlayerEntity player = context.getSender();
			ItemStack stack = player.getMainHandItem();
			Item item = stack.getItem();
			if (item instanceof IItemWithScreen) {
				IItemWithScreen attachmentsItem = (IItemWithScreen) item;
				if (!attachmentsItem.canOpen(stack)) return;
				NetworkHooks.openGui(player, attachmentsItem.getItemContainerProvider(stack), buf -> attachmentsItem.writeContainerInfo(buf, stack));
			}
		});
		context.setPacketHandled(true);
	}
}

