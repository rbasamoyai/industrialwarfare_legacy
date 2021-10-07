package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.utils.ArgUtils;

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
		
		msg.orders.forEach(order -> {
			buf
					.writeResourceLocation(order.getCommand().getRegistryName())
					.writeBlockPos(order.getPos())
					.writeItemStack(order.getFilter(), false)
					.writeByteArray(ArgUtils.unbox(order.getArgs()));
		});
	}
	
	public static STaskScrollSyncMessage decode(PacketBuffer buf) {
		Hand hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
		ItemStack labelItem = buf.readItem();
		
		List<TaskScrollOrder> orders = new LinkedList<>();
		int size = buf.readVarInt();
		
		for (int i = 0; i < size; i++) {
			ResourceLocation loc = buf.readResourceLocation();
			BlockPos pos = buf.readBlockPos();
			ItemStack stack = buf.readItem();
			byte[] unboxedArgs = buf.readByteArray();
			List<Byte> boxedArgs = unboxedArgs.length == 0 ? new ArrayList<>() : ArgUtils.box(unboxedArgs);
			
			orders.add(new TaskScrollOrder(IWModRegistries.TASK_SCROLL_COMMANDS.getValue(loc), pos, stack, boxedArgs));
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
