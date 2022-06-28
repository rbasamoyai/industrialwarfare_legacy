package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.DiplomacyMenu;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.core.network.handlers.DiplomacyScreenCHandlers;

public class DiplomacyScreenMessages {
	
	public static class SOpenScreen {
		private static final Component TITLE = new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".diplomacy");
		
		public SOpenScreen() {}
		public static void encode(SOpenScreen msg, FriendlyByteBuf buf) {}
		public static SOpenScreen decode(FriendlyByteBuf buf) { return new SOpenScreen(); }
		
		public static void handle(SOpenScreen msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				MenuConstructor containerProvider = DiplomacyMenu.getServerContainerProvider();
				MenuProvider namedAbstractContainerMenuProvider = new SimpleMenuProvider(containerProvider, TITLE);
				NetworkHooks.openGui(ctx.getSender(), namedAbstractContainerMenuProvider);
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class SRequestUpdate {
		public SRequestUpdate() {}
		public static void encode(SRequestUpdate msg, FriendlyByteBuf buf) {}
		public static SRequestUpdate decode(FriendlyByteBuf buf) { return new SRequestUpdate(); }
		
		public static void handle(SRequestUpdate msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayer player = ctx.getSender();
				AbstractContainerMenu ct = player.containerMenu;
				if (ct == null) return;
				if (!(ct instanceof DiplomacyMenu)) return;
				((DiplomacyMenu) ct).updateData();
				ct.broadcastChanges();
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class CBroadcastChanges {
		public Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> diplomaticStatuses; // first status is other -> player, second is player -> other 
		public Map<UUID, Byte> npcFactionRelationships;
		
		public CBroadcastChanges() {}
		
		public CBroadcastChanges(Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> diplomaticStatuses, Map<UUID, Byte> npcFactionRelationships) {
			this.diplomaticStatuses = diplomaticStatuses;
			this.npcFactionRelationships = npcFactionRelationships;
		}
		
		public static void encode(CBroadcastChanges msg, FriendlyByteBuf buf) {
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
		
		public static CBroadcastChanges decode(FriendlyByteBuf buf) {
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
		
		public static void handle(CBroadcastChanges msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DiplomacyScreenCHandlers.handleCBroadcastChanges(msg, sup));
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class SDiplomaticStatusChangeSync {
		public Map<PlayerIDTag, DiplomaticStatus> statusChanges;
		
		public SDiplomaticStatusChangeSync() {}
		
		public SDiplomaticStatusChangeSync(Map<PlayerIDTag, DiplomaticStatus> statusChanges) {
			this.statusChanges = statusChanges;
		}
		
		public static void encode(SDiplomaticStatusChangeSync msg, FriendlyByteBuf buf) {
			buf.writeVarInt(msg.statusChanges.size());
			msg.statusChanges.forEach((tag, status) -> {
				tag.toNetwork(buf);
				buf.writeByte(status.getValue());
			});
		}
		
		public static SDiplomaticStatusChangeSync decode(FriendlyByteBuf buf) {
			int sz = buf.readVarInt();
			Map<PlayerIDTag, DiplomaticStatus> statuses = new HashMap<>();
			for (int i = 0; i < sz; i++) {
				PlayerIDTag tag = PlayerIDTag.fromNetwork(buf);
				DiplomaticStatus status = DiplomaticStatus.fromValue(buf.readByte());
				statuses.put(tag, status);
			}
			return new SDiplomaticStatusChangeSync(statuses);
		}
		
		public static void handle(SDiplomaticStatusChangeSync msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayer player = ctx.getSender();
				DiplomacySaveData saveData = DiplomacySaveData.get(player.level);
				PlayerIDTag playerTag = PlayerIDTag.of(player);
				msg.statusChanges.forEach((tag, status) -> {
					saveData.setDiplomaticStatus(playerTag, tag, status);
				});
			});
			ctx.setPacketHandled(true);
		}
	}
	
	private DiplomacyScreenMessages() {}
	
}
