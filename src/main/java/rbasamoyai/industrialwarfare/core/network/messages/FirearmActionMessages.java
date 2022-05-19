package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.items.firearms.FirearmItem;
import rbasamoyai.industrialwarfare.core.network.handlers.FirearmActionCHandlers;

public class FirearmActionMessages {

	public static class SInputAction {
		private Type type;
		
		public SInputAction() {}
		
		public SInputAction(Type type) {
			this.type = type;
		}
		
		public static void encode(SInputAction msg, PacketBuffer buf) {
			buf.writeVarInt(msg.type.id);			
		}
		
		public static SInputAction decode(PacketBuffer buf) {
			Type type = Type.fromId(buf.readVarInt());
			return new SInputAction(type);
		}
		
		public static void handle(SInputAction msg, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				ServerPlayerEntity player = context.getSender();
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
	
	public static class CApplyRecoil {
		public float xRot;
		public float yRot;
		
		public CApplyRecoil() {}
		
		public CApplyRecoil(float xRot, float yRot) {
			this.xRot = xRot;
			this.yRot = yRot;
		}
		
		public static void encode(CApplyRecoil msg, PacketBuffer buf) {
			buf.writeFloat(msg.xRot);
			buf.writeFloat(msg.yRot);
		}
		
		public static CApplyRecoil decode(PacketBuffer buf) {
			return new CApplyRecoil(buf.readFloat(), buf.readFloat());
		}
		
		public static void handle(CApplyRecoil msg, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FirearmActionCHandlers.handleCApplyRecoil(msg, contextSupplier));
			});
			context.setPacketHandled(true);
		}
	}
	
	public static class CNotifyHeadshot {
		public CNotifyHeadshot() {}		
		public static void encode(CNotifyHeadshot msg, PacketBuffer buf) {}
		public static CNotifyHeadshot decode(PacketBuffer buf) { return new CNotifyHeadshot(); }
		
		public static void handle(CNotifyHeadshot msg, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FirearmActionCHandlers.handleCNotifyHeadshot(msg, contextSupplier));
			});
			context.setPacketHandled(true);
		}
	}
	
}
