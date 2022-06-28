package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

/*
 * Sync client task scroll data to the server. Called during onClose.
 */

public class STaskScrollSyncMessage {
	
	private InteractionHand hand;
	private List<TaskScrollOrder> orders;
	private ItemStack labelItem;
	
	public STaskScrollSyncMessage() {	
	}
	
	public STaskScrollSyncMessage(InteractionHand hand, List<TaskScrollOrder> orders, ItemStack labelItem) {
		this.hand = hand;
		this.orders = orders;
		this.labelItem = labelItem;
	}
	
	public static void encode(STaskScrollSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.hand == InteractionHand.MAIN_HAND);
		buf
				.writeItem(msg.labelItem)
				.writeVarInt(msg.orders.size());
		msg.orders.forEach(o -> o.toNetwork(buf));
	}
	
	public static STaskScrollSyncMessage decode(FriendlyByteBuf buf) {
		InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		ItemStack labelItem = buf.readItem();
		
		List<TaskScrollOrder> orders = new ArrayList<>();
		int size = buf.readVarInt();
		for (int i = 0; i < size; i++) {
			orders.add(TaskScrollOrder.fromNetwork(buf));
		}
		
		return new STaskScrollSyncMessage(hand, orders, labelItem);
	}
	
	public static void handle(STaskScrollSyncMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			ItemStack handItem = ctx.getSender().getItemInHand(msg.hand);
			
			TaskScrollItem.getDataHandler(handItem).ifPresent(h -> {
				h.setList(msg.orders);
				h.setLabel(msg.labelItem);
			});
		});
		ctx.setPacketHandled(true);
	}
	
}
