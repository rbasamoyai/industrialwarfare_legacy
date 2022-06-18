package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationContainer;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.core.network.handlers.ResourceStationCHandlers;

public class ResourceStationMessages {
	
	public static class SSelectTab {
		private int selectedTab;
		
		public SSelectTab() {}
		
		public SSelectTab(int selectedTab) {
			this.selectedTab = selectedTab;
		}
		
		public static void encode(SSelectTab msg, PacketBuffer buf) {
			buf.writeVarInt(msg.selectedTab);
		}
		
		public static SSelectTab decode(PacketBuffer buf) {
			return new SSelectTab(buf.readVarInt());
		}
		
		public static void handle(SSelectTab msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayerEntity sender = ctx.getSender();
				Container ct = sender.containerMenu;
				if (!(ct instanceof ResourceStationContainer)) return;
				((ResourceStationContainer) ct).setSelected(msg.selectedTab);
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class CSyncRequests {
		private List<SupplyRequestPredicate> predicates;
		
		public CSyncRequests() {
			this.predicates = new ArrayList<>();
		}
		
		public CSyncRequests(List<SupplyRequestPredicate> predicates) {
			this.predicates = predicates;
		}
		
		public List<SupplyRequestPredicate> getPredicates() { return this.predicates; }
		
		public static void encode(CSyncRequests msg, PacketBuffer buf) {
			buf.writeVarInt(msg.predicates.size());
			msg.predicates.forEach(p -> p.toNetwork(buf));
		}
		
		public static CSyncRequests decode(PacketBuffer buf) {
			List<SupplyRequestPredicate> predicates =
					IntStream.range(0, buf.readVarInt()).boxed()
					.map(i -> SupplyRequestPredicate.fromNetwork(buf))
					.collect(Collectors.toCollection(ArrayList::new));
			return new CSyncRequests(predicates);
		}
		
		public static void handle(CSyncRequests msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ResourceStationCHandlers.handleCSyncRequests(msg));
			});
			ctx.setPacketHandled(true);
		}	
	}
	
	public static class SSetRunning {
		private boolean running;
		
		public SSetRunning() {}
		
		public SSetRunning(boolean running) {
			this.running = running;
		}
		
		public static void encode(SSetRunning msg, PacketBuffer buf) {
			buf.writeBoolean(msg.running);
		}
		
		public static SSetRunning decode(PacketBuffer buf) {
			return new SSetRunning(buf.readBoolean());
		}
		
		public static void handle(SSetRunning msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayerEntity sender = ctx.getSender();
				Container ct = sender.containerMenu;
				if (!(ct instanceof ResourceStationContainer)) return;
				((ResourceStationContainer) ct).setRunning(msg.running);
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class SSetExtraStock {
		private SupplyRequestPredicate extraStock;
		private int index;
		
		public SSetExtraStock() {}
		
		public SSetExtraStock(SupplyRequestPredicate extraSupplies, int index) {
			this.extraStock = extraSupplies;
			this.index = index;
		}
		
		public static void encode(SSetExtraStock msg, PacketBuffer buf) {
			msg.extraStock.toNetwork(buf);
			buf.writeVarInt(msg.index);
		}
		
		public static SSetExtraStock decode(PacketBuffer buf) {
			return new SSetExtraStock(SupplyRequestPredicate.fromNetwork(buf), buf.readVarInt());
		}
		
		public static void handle(SSetExtraStock msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayerEntity sender = ctx.getSender();
				Container ct = sender.containerMenu;
				if (!(ct instanceof ResourceStationContainer)) return;
				((ResourceStationContainer) ct).setOrAddExtraStock(msg.extraStock, msg.index);
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class SRemoveExtraStock {
		private int index;
		
		public SRemoveExtraStock() {}
		
		public SRemoveExtraStock(int index) {
			this.index = index;
		}
		
		public static void encode(SRemoveExtraStock msg, PacketBuffer buf) {
			buf.writeVarInt(msg.index);
		}
		
		public static SRemoveExtraStock decode(PacketBuffer buf) {
			return new SRemoveExtraStock(buf.readVarInt());
		}
		
		public static void handle(SRemoveExtraStock msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayerEntity sender = ctx.getSender();
				Container ct = sender.containerMenu;
				if (!(ct instanceof ResourceStationContainer)) return;
				((ResourceStationContainer) ct).removeExtraStock(msg.index);
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class CSyncExtraStock {
		private List<SupplyRequestPredicate> extraStock;
		
		public CSyncExtraStock() {}
		
		public CSyncExtraStock(List<SupplyRequestPredicate> extraSupplies) {
			this.extraStock = extraSupplies;
		}
		
		public List<SupplyRequestPredicate> getExtraStock() { return this.extraStock; }
		
		public static void encode(CSyncExtraStock msg, PacketBuffer buf) {
			buf.writeVarInt(msg.extraStock.size());
			msg.extraStock.forEach(p -> p.toNetwork(buf));
		}
		
		public static CSyncExtraStock decode(PacketBuffer buf) {
			int sz = buf.readVarInt();
			List<SupplyRequestPredicate> extraStock = new ArrayList<>(sz);
			for (int i = 0; i < sz; ++i) {
				extraStock.add(SupplyRequestPredicate.fromNetwork(buf));
			}
			return new CSyncExtraStock(extraStock);
		}
		
		public static void handle(CSyncExtraStock msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ResourceStationCHandlers.handleCSyncExtraStock(msg));
			});
			ctx.setPacketHandled(true);
		}
	}
	
}