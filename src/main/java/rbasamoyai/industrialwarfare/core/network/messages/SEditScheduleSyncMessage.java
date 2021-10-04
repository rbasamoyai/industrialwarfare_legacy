package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.items.ScheduleItem;

public class SEditScheduleSyncMessage {

	public Hand hand;
	public List<Pair<Integer, Integer>> schedule;
	
	public SEditScheduleSyncMessage() {
	}
	
	public SEditScheduleSyncMessage(Hand hand, List<Pair<Integer, Integer>> schedule) {
		this.hand = hand;
		this.schedule = schedule;
	}
	
	public static void encode(SEditScheduleSyncMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.hand == Hand.MAIN_HAND);
		buf.writeVarInt(msg.schedule.size());
		msg.schedule.forEach(shift -> {
			buf.writeInt(shift.getFirst());
			buf.writeInt(shift.getSecond());
		});
	}
	
	public static SEditScheduleSyncMessage decode(PacketBuffer buf) {
		Hand hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
		List<Pair<Integer, Integer>> schedule = new ArrayList<>(7);
		int size = buf.readVarInt();
		
		for (int i = 0; i < size; i++) {
			int start = buf.readInt();
			int end = buf.readInt();
			schedule.add(Pair.of(start, end));
		}
		
		return new SEditScheduleSyncMessage(hand, schedule);
	}
	
	public static void handle(SEditScheduleSyncMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ServerPlayerEntity player = context.getSender();
			ItemStack handItem = player.getItemInHand(msg.hand);
			ScheduleItem.getDataHandler(handItem).ifPresent(h -> {
				h.setSchedule(msg.schedule);
			});
		});
		context.setPacketHandled(true);
	}
	
}
