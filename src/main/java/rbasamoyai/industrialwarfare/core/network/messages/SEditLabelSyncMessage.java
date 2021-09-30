package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.items.LabelItem;

public class SEditLabelSyncMessage {

	public Hand hand;
	public byte num;
	public UUID uuid;
	public ITextComponent name;
	
	public SEditLabelSyncMessage() {
	}
	
	public SEditLabelSyncMessage(Hand hand, byte num, UUID uuid, ITextComponent name) {
		this.hand = hand;
		this.num = num;
		this.uuid = uuid;
		this.name = name;
	}
	
	public static void encode(SEditLabelSyncMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.hand == Hand.MAIN_HAND);
		buf.writeByte(msg.num);
		buf
				.writeUUID(msg.uuid)
				.writeUtf(ITextComponent.Serializer.toJson(msg.name));
	}
	
	public static SEditLabelSyncMessage decode(PacketBuffer buf) {
		Hand hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
		byte num = buf.readByte();
		UUID uuid = buf.readUUID();
		ITextComponent name = ITextComponent.Serializer.fromJson(buf.readUtf());
		return new SEditLabelSyncMessage(hand, num, uuid, name);
	}
	
	public static void handle(SEditLabelSyncMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			ServerPlayerEntity player = context.getSender();
			ItemStack handItem = player.getItemInHand(msg.hand);
			
			ItemStack label = handItem.getCount() > 1 ? handItem.split(1) : handItem;
			
			LabelItem.getDataHandler(label).ifPresent(h -> {
				h.setNumber(msg.num);
				h.setUUID(msg.uuid);
				h.cacheName(msg.name);
			});
		});
		context.setPacketHandled(true);
	}
	
}
