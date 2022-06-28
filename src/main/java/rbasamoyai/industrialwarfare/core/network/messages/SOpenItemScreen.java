package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.common.items.ItemWithScreen;

public class SOpenItemScreen {
	public SOpenItemScreen() {}
	public static void encode(SOpenItemScreen msg, FriendlyByteBuf buf) {}
	public static SOpenItemScreen decode(FriendlyByteBuf buf) { return new SOpenItemScreen(); }
	
	public static void handle(SOpenItemScreen msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();
			ItemStack stack = player.getMainHandItem();
			Item item = stack.getItem();
			if (item instanceof ItemWithScreen) {
				ItemWithScreen attachmentsItem = (ItemWithScreen) item;
				if (!attachmentsItem.canOpenScreen(stack)) return;
				NetworkHooks.openGui(player, attachmentsItem.getItemContainerProvider(stack), buf -> attachmentsItem.writeContainerInfo(buf, stack));
			}
		});
		ctx.setPacketHandled(true);
	}
}

