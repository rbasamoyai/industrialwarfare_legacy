package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.DiplomacyContainer;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.core.network.handlers.DiplomacyScreenCHandlers;

public class DiplomacyScreenMessages {
	
	public static class SOpenScreen {
		private static final ITextComponent TITLE = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".diplomacy");
		
		public SOpenScreen() {}
		public static void encode(SOpenScreen msg, PacketBuffer buf) {}
		public static SOpenScreen decode(PacketBuffer buf) { return new SOpenScreen(); }
		
		public static void handle(SOpenScreen msg, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				IContainerProvider containerProvider = DiplomacyContainer.getServerContainerProvider();
				INamedContainerProvider namedContainerProvider = new SimpleNamedContainerProvider(containerProvider, TITLE);
				NetworkHooks.openGui(context.getSender(), namedContainerProvider);
			});
			context.setPacketHandled(true);
		}
	}
	
	public static class SRequestUpdate {
		public SRequestUpdate() {}
		public static void encode(SRequestUpdate msg, PacketBuffer buf) {}
		public static SRequestUpdate decode(PacketBuffer buf) { return new SRequestUpdate(); }
		
		public static void handle(SRequestUpdate msg, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				ServerPlayerEntity player = context.getSender();
				Container ct = player.containerMenu;
				if (ct == null) return;
				if (!(ct instanceof DiplomacyContainer)) return;
				((DiplomacyContainer) ct).updateData();
				ct.broadcastChanges();
			});
			context.setPacketHandled(true);
		}
	}
	
	public static class CBroadcastChanges {
		public Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> diplomaticStatuses; // first status is player -> other, second is other -> player
		public Map<UUID, Byte> npcFactionRelationships;
		
		public CBroadcastChanges() {}
		
		public CBroadcastChanges(Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> diplomaticStatuses, Map<UUID, Byte> npcFactionRelationships) {
			this.diplomaticStatuses = diplomaticStatuses;
			this.npcFactionRelationships = npcFactionRelationships;
		}
		
		public static void encode(CBroadcastChanges msg, PacketBuffer buf) {
			buf.writeVarInt(msg.diplomaticStatuses.size());
			
			msg.diplomaticStatuses
					.entrySet()
					.forEach(e -> {
						e.getKey().toNetwork(buf);
						Pair<DiplomaticStatus, DiplomaticStatus> statuses = e.getValue();
						buf
								.writeByte(statuses.getFirst().getValue())
								.writeByte(statuses.getSecond().getValue());
					});
			
			buf.writeVarInt(msg.npcFactionRelationships.size());
			
			msg.npcFactionRelationships
					.entrySet().forEach(e -> {
						buf.writeUUID(e.getKey());
						buf.writeByte(e.getValue());
					});
		}
		
		public static CBroadcastChanges decode(PacketBuffer buf) {
			int dsSz = buf.readVarInt();
			Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> diplomaticStatuses = new HashMap<>(dsSz);
			for (int i = 0; i < dsSz; i++) {
				PlayerIDTag tag = PlayerIDTag.fromNetwork(buf);
				DiplomaticStatus p2oStatus = DiplomaticStatus.fromValue(buf.readByte());
				DiplomaticStatus o2pStatus = DiplomaticStatus.fromValue(buf.readByte());
				diplomaticStatuses.put(tag, Pair.of(p2oStatus, o2pStatus));
			}
			
			int rsSz = buf.readVarInt();
			Map<UUID, Byte> relationships = new HashMap<>(rsSz);
			for (int i = 0; i < rsSz; i++) {
				UUID uuid = buf.readUUID();
				byte relationship = buf.readByte();
				relationships.put(uuid, relationship);
			}
			
			return new CBroadcastChanges(diplomaticStatuses, relationships);
		}
		
		public static void handle(CBroadcastChanges msg, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DiplomacyScreenCHandlers.handleCBroadcastChanges(msg, contextSupplier));
			});
			context.setPacketHandled(true);
		}
	}
	
	private DiplomacyScreenMessages() {}
	
}
