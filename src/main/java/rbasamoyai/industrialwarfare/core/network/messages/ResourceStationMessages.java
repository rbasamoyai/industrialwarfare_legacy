package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.LivestockPenMenu;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationMenu;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.core.network.handlers.ResourceStationCHandlers;

public class ResourceStationMessages {
	
	public static class SSelectTab {
		private int selectedTab;
		
		public SSelectTab(int selectedTab) {
			this.selectedTab = selectedTab;
		}
		
		public static void encode(SSelectTab msg, FriendlyByteBuf buf) {
			buf.writeVarInt(msg.selectedTab);
		}
		
		public static SSelectTab decode(FriendlyByteBuf buf) {
			return new SSelectTab(buf.readVarInt());
		}
		
		public static void handle(SSelectTab msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				AbstractContainerMenu ct = ctx.getSender().containerMenu;
				if (!(ct instanceof ResourceStationMenu)) return;
				((ResourceStationMenu) ct).setSelected(msg.selectedTab);
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
		
		public static void encode(CSyncRequests msg, FriendlyByteBuf buf) {
			buf.writeVarInt(msg.predicates.size());
			msg.predicates.forEach(p -> p.toNetwork(buf));
		}
		
		public static CSyncRequests decode(FriendlyByteBuf buf) {
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
		
		public static void encode(SSetRunning msg, FriendlyByteBuf buf) {
			buf.writeBoolean(msg.running);
		}
		
		public static SSetRunning decode(FriendlyByteBuf buf) {
			return new SSetRunning(buf.readBoolean());
		}
		
		public static void handle(SSetRunning msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				AbstractContainerMenu ct = ctx.getSender().containerMenu;
				if (!(ct instanceof ResourceStationMenu)) return;
				((ResourceStationMenu) ct).setRunning(msg.running);
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
		
		public static void encode(SSetExtraStock msg, FriendlyByteBuf buf) {
			msg.extraStock.toNetwork(buf);
			buf.writeVarInt(msg.index);
		}
		
		public static SSetExtraStock decode(FriendlyByteBuf buf) {
			return new SSetExtraStock(SupplyRequestPredicate.fromNetwork(buf), buf.readVarInt());
		}
		
		public static void handle(SSetExtraStock msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				AbstractContainerMenu ct = ctx.getSender().containerMenu;
				if (!(ct instanceof ResourceStationMenu)) return;
				((ResourceStationMenu) ct).setOrAddExtraStock(msg.extraStock, msg.index);
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
		
		public static void encode(SRemoveExtraStock msg, FriendlyByteBuf buf) {
			buf.writeVarInt(msg.index);
		}
		
		public static SRemoveExtraStock decode(FriendlyByteBuf buf) {
			return new SRemoveExtraStock(buf.readVarInt());
		}
		
		public static void handle(SRemoveExtraStock msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				AbstractContainerMenu ct = ctx.getSender().containerMenu;
				if (!(ct instanceof ResourceStationMenu)) return;
				((ResourceStationMenu) ct).removeExtraStock(msg.index);
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
		
		public static void encode(CSyncExtraStock msg, FriendlyByteBuf buf) {
			buf.writeVarInt(msg.extraStock.size());
			msg.extraStock.forEach(p -> p.toNetwork(buf));
		}
		
		public static CSyncExtraStock decode(FriendlyByteBuf buf) {
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
	
	public static class SSyncLivestockCount {
		private final int count;
		
		public SSyncLivestockCount(int count) {
			this.count = count;
		}
		
		public SSyncLivestockCount(FriendlyByteBuf buf) {
			this.count = buf.readVarInt();
		}
		
		public void encode(FriendlyByteBuf buf) {
			buf.writeVarInt(this.count);
		}
		
		public void handle(Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				AbstractContainerMenu ct = ctx.getSender().containerMenu;
				if (!(ct instanceof LivestockPenMenu)) return;
				((LivestockPenMenu) ct).setMinimumLivestock(this.count);
			});
			ctx.setPacketHandled(true);
		}
	}
	
}