package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.items.ScheduleItem;

public class SEditScheduleSyncMessage {

	private InteractionHand hand;
	private List<Pair<Integer, Integer>> schedule;
	
	public SEditScheduleSyncMessage() {
	}
	
	public SEditScheduleSyncMessage(InteractionHand hand, List<Pair<Integer, Integer>> schedule) {
		this.hand = hand;
		this.schedule = schedule;
	}
	
	public static void encode(SEditScheduleSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.hand == InteractionHand.MAIN_HAND);
		buf.writeVarInt(msg.schedule.size());
		msg.schedule.forEach(shift -> {
			buf.writeInt(shift.getFirst());
			buf.writeInt(shift.getSecond());
		});
	}
	
	public static SEditScheduleSyncMessage decode(FriendlyByteBuf buf) {
		InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		List<Pair<Integer, Integer>> schedule = new ArrayList<>(7);
		int size = buf.readVarInt();
		
		for (int i = 0; i < size; i++) {
			int start = buf.readInt();
			int end = buf.readInt();
			schedule.add(Pair.of(start, end));
		}
		
		return new SEditScheduleSyncMessage(hand, schedule);
	}
	
	public static void handle(SEditScheduleSyncMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			ItemStack handItem = ctx.getSender().getItemInHand(msg.hand);
			ScheduleItem.getDataHandler(handItem).ifPresent(h -> {
				h.setSchedule(msg.schedule);
			});
		});
		ctx.setPacketHandled(true);
	}
	
}
