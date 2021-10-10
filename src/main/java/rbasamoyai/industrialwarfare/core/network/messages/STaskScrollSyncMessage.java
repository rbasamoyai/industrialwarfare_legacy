package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;

/*
 * Sync client task scroll data to the server. Called during onClose.
 */

public class STaskScrollSyncMessage {
	
	public Hand hand;
	public List<TaskScrollOrder> orders;
	public ItemStack labelItem;
	
	public STaskScrollSyncMessage() {	
	}
	
	public STaskScrollSyncMessage(Hand hand, List<TaskScrollOrder> orders, ItemStack labelItem) {
		this.hand = hand;
		this.orders = orders;
		this.labelItem = labelItem;
	}
	
	public static void encode(STaskScrollSyncMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.hand == Hand.MAIN_HAND);
		buf
				.writeItem(msg.labelItem)
				.writeVarInt(msg.orders.size());
		msg.orders.forEach(o -> o.toNetwork(buf));
	}
	
	public static STaskScrollSyncMessage decode(PacketBuffer buf) {
		Hand hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
		ItemStack labelItem = buf.readItem();
		
		List<TaskScrollOrder> orders = new ArrayList<>();
		int size = buf.readVarInt();
		for (int i = 0; i < size; i++) {
			orders.add(TaskScrollOrder.fromNetwork(buf));
		}
		
		return new STaskScrollSyncMessage(hand, orders, labelItem);
	}
	
	public static void handle(STaskScrollSyncMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ServerPlayerEntity player = context.getSender();
			ItemStack handItem = player.getItemInHand(msg.hand);
			
			TaskScrollItem.getDataHandler(handItem).ifPresent(h -> {
				h.setList(msg.orders);
				h.setLabel(msg.labelItem);
			});
		});
		context.setPacketHandled(true);
	}
	
}
