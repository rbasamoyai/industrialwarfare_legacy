package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.items.LabelItem;

public class SEditLabelSyncMessage {

	private InteractionHand hand;
	private byte num;
	private UUID uuid;
	private Component name;
	
	public SEditLabelSyncMessage() {
	}
	
	public SEditLabelSyncMessage(InteractionHand hand, byte num, UUID uuid, Component name) {
		this.hand = hand;
		this.num = num;
		this.uuid = uuid;
		this.name = name;
	}
	
	public static void encode(SEditLabelSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.hand == InteractionHand.MAIN_HAND);
		buf.writeByte(msg.num);
		buf
				.writeUUID(msg.uuid)
				.writeUtf(Component.Serializer.toJson(msg.name));
	}
	
	public static SEditLabelSyncMessage decode(FriendlyByteBuf buf) {
		InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		byte num = buf.readByte();
		UUID uuid = buf.readUUID();
		Component name = Component.Serializer.fromJson(buf.readUtf());
		return new SEditLabelSyncMessage(hand, num, uuid, name);
	}
	
	public static void handle(SEditLabelSyncMessage msg, Supplier<NetworkEvent.Context> sup) {
		NetworkEvent.Context ctx = sup.get();
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();
			ItemStack handItem = player.getItemInHand(msg.hand);
			
			boolean split = handItem.getCount() > 1;
			ItemStack label = split ? handItem.split(1) : handItem;
			
			LabelItem.getDataHandler(label).ifPresent(h -> {
				h.setNumber(msg.num);
				h.setUUID(msg.uuid);
				h.cacheName(msg.name);
			});
			
			if (split) {
				if (!player.getInventory().add(label)) Containers.dropItemStack(player.level, player.getX(), player.getY(), player.getZ(), label);
			}
		});
		ctx.setPacketHandled(true);
	}
	
}
