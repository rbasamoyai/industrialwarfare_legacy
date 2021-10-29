package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomacySaveData;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.handlers.DiplomacyScreenCHandlers;

public class DiplomacyScreenMessages {
	
	public static class SRequestData {
		public boolean openScreen;
		
		public SRequestData() {}
		
		public SRequestData(boolean openScreen) {
			this.openScreen = openScreen;
		}
		
		public static void encode(SRequestData msg, PacketBuffer buf) {
			buf.writeBoolean(msg.openScreen);
		}
		
		public static SRequestData decode(PacketBuffer buf) {
			return new SRequestData(buf.readBoolean());
		}
		
		public static void handle(SRequestData msg, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				ServerPlayerEntity player = context.getSender();
				IWNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CSendData(player, msg.openScreen));
			});
			context.setPacketHandled(true);
		}
	}
	
	public static class CSendData {
		public Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> statusData; // First is player -> other, second is other -> player
		public Map<UUID, Byte> relationsData; // Not used for now
		public boolean openScreen;
		
		public CSendData() {}
		
		public CSendData(PlayerEntity player, boolean openScreen) {
			DiplomacySaveData saveData = DiplomacySaveData.get(player.level);
			PlayerIDTag pt = PlayerIDTag.of(player);
			
			this.statusData = new HashMap<>();
			saveData.getPlayers()
					.stream()
					.filter(o -> !o.equals(pt))
					.filter(o -> o.isPlayer() || true) // TODO: if npc and not met, continue
					.forEach(o -> this.statusData.put(o, Pair.of(saveData.getDiplomaticStatus(pt, o), saveData.getDiplomaticStatus(o, pt))));
			
			this.relationsData = new HashMap<>(); // TODO: implement npcs and then add known npcs
			
			this.openScreen = openScreen;
		}
		
		public CSendData(Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> statusData, Map<UUID, Byte> relationsData, boolean openScreen) {
			this.statusData = statusData;
			this.relationsData = relationsData;
			this.openScreen = openScreen;
		}
		
		public static void encode(CSendData msg, PacketBuffer buf) {
			buf.writeVarInt(msg.statusData.size());
			msg.statusData.forEach((tag, pds) -> {
				tag.toNetwork(buf);
				buf.writeByte(pds.getFirst().getValue());
				buf.writeByte(pds.getSecond().getValue());
			});
			
			buf.writeVarInt(msg.relationsData.size());
			msg.relationsData.forEach((npcFactionUuid, relationship) -> {
				buf
						.writeUUID(npcFactionUuid)
						.writeByte(relationship);
			});
			buf.writeBoolean(msg.openScreen);
		}
		
		public static CSendData decode(PacketBuffer buf) {
			Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> statusData = new HashMap<>();
			int sdSz = buf.readVarInt();
			for (int i = 0; i < sdSz; i++) {
				PlayerIDTag tag = PlayerIDTag.fromNetwork(buf);
				Pair<DiplomaticStatus, DiplomaticStatus> statuses = Pair.of(DiplomaticStatus.fromValue(buf.readByte()), DiplomaticStatus.fromValue(buf.readByte()));
				statusData.put(tag, statuses);
			}
			
			Map<UUID, Byte> relationsData = new HashMap<>();
			int rdSz = buf.readVarInt();
			for (int i = 0; i < rdSz; i++) {
				UUID npcFactionUuid = buf.readUUID();
				byte relationship = buf.readByte();
				relationsData.put(npcFactionUuid, relationship);
			}
			
			return new CSendData(statusData, relationsData, buf.readBoolean());
		}
		
		public static void handle(CSendData msg, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DiplomacyScreenCHandlers.handleCSendData(msg));
			});
			context.setPacketHandled(true);
		}
	}
	
	private DiplomacyScreenMessages() {}
	
}
