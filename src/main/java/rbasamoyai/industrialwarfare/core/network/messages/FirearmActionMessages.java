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

	public static class SReloadingFirearm {
		public SReloadingFirearm() {}
		public static void encode(SReloadingFirearm msg, PacketBuffer buf) {}
		public static SReloadingFirearm decode(PacketBuffer buf) { return new SReloadingFirearm(); }
		
		public static void handle(SReloadingFirearm msg, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				ServerPlayerEntity player = context.getSender();
				FirearmItem.tryReloadFirearm(player.getMainHandItem(), player);
			});
			context.setPacketHandled(true);
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
	
}
