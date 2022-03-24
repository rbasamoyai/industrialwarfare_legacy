package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.whistle.WhistleContainer;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.IWModRegistries;

public class WhistleScreenMessages {
	
	public static class SWhistleScreenSync {
		public CombatMode mode;
		public UnitFormationType<?> type;
		
		public SWhistleScreenSync() {}
		
		public SWhistleScreenSync(CombatMode mode, UnitFormationType<?> type) {
			this.mode = mode;
			this.type = type;
		}
		
		public static void encode(SWhistleScreenSync msg, PacketBuffer buf) {
			buf
			.writeVarInt(msg.mode.getId())
			.writeRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES, msg.type);
		}
		
		public static SWhistleScreenSync decode(PacketBuffer buf) {
			CombatMode mode = CombatMode.fromId(buf.readVarInt());
			UnitFormationType<?> type = buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES);
			return new SWhistleScreenSync(mode, type);
		}
		
		public static void handle(SWhistleScreenSync msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayerEntity player = ctx.getSender();
				Container ct = player.containerMenu;
				if (!(ct instanceof WhistleContainer)) return;
				WhistleContainer whistleCt = (WhistleContainer) ct;
				whistleCt.setMode(msg.mode);
				whistleCt.setFormation(msg.type);
				whistleCt.updateItem(player);
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class SStopAction {
		public SStopAction() {}
		
		public static void encode(SStopAction msg, PacketBuffer buf) {}
		public static SStopAction decode(PacketBuffer buf) { return new SStopAction(); }
		
		public static void handle(SStopAction msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayerEntity player = ctx.getSender();
				Container ct = player.containerMenu;
				if (!(ct instanceof WhistleContainer)) return;
				WhistleContainer whistleCt = (WhistleContainer) ct;
				whistleCt.stopWhistle(player);
			});
			ctx.setPacketHandled(false);
		}
	}
	
}
