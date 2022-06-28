package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.core.network.handlers.FirearmActionCHandlers;

public class FirearmActionMessages {

	public static class SInputAction {
		private Type type;
		
		public SInputAction() {}
		
		public SInputAction(Type type) {
			this.type = type;
		}
		
		public static void encode(SInputAction msg, FriendlyByteBuf buf) {
			buf.writeVarInt(msg.type.id);			
		}
		
		public static SInputAction decode(FriendlyByteBuf buf) {
			Type type = Type.fromId(buf.readVarInt());
			return new SInputAction(type);
		}
		
		public static void handle(SInputAction msg, Supplier<NetworkEvent.Context> ctx) {
			NetworkEvent.Context context = ctx.get();
			context.enqueueWork(() -> {
				ServerPlayer player = context.getSender();
				switch (msg.type) {
				case RELOADING:
					FirearmItem.tryReloadFirearm(player.getMainHandItem(), player);
					break;
				case PREVIOUS_STANCE:
					FirearmItem.tryPreviousStance(player.getMainHandItem(), player);
					break;
				default: break;
				}
			});
			context.setPacketHandled(true);
		}
		
		public static enum Type {
			NOTHING(0),
			RELOADING(1),
			PREVIOUS_STANCE(2);
			
			private int id;
			
			private Type(int id) {
				this.id = id;
			}
			
			public static Type fromId(int id) {
				return 0 <= id && id < values().length ? values()[id] : NOTHING;
			}
		}
	}
	
	public static class CNotifyHeadshot {
		public CNotifyHeadshot() {}		
		public static void encode(CNotifyHeadshot msg, FriendlyByteBuf buf) {}
		public static CNotifyHeadshot decode(FriendlyByteBuf buf) { return new CNotifyHeadshot(); }
		
		public static void handle(CNotifyHeadshot msg, Supplier<NetworkEvent.Context> ctx) {
			NetworkEvent.Context context = ctx.get();
			context.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FirearmActionCHandlers.handleCNotifyHeadshot(msg, ctx));
			});
			context.setPacketHandled(true);
		}
	}
	
}
