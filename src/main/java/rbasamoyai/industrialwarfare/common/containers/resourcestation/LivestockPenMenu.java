package rbasamoyai.industrialwarfare.common.containers.resourcestation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.blockentities.LivestockPenBlockEntity;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.core.init.MenuInit;

public class LivestockPenMenu extends ResourceStationMenu {

	public static LivestockPenMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		LivestockPenMenu ct = new LivestockPenMenu(MenuInit.LIVESTOCK_PEN.get(), windowId,
				playerInv, buf.readBlockPos(), new ItemStackHandler(27),
				new ItemStackHandler(27), new SimpleContainerData(3),
				Optional.empty(), buf.readItem());
		
		ct.setRunning(buf.readBoolean());
		ct.setMinimumLivestock(buf.readVarInt());
		
		List<SupplyRequestPredicate> predicates =
				IntStream.range(0, buf.readVarInt()).boxed()
				.map(i -> SupplyRequestPredicate.fromNetwork(buf))
				.collect(Collectors.toCollection(ArrayList::new));
		ct.setRequests(predicates);
		
		List<SupplyRequestPredicate> extraSupplies =
				IntStream.range(0, buf.readVarInt()).boxed()
				.map(i -> SupplyRequestPredicate.fromNetwork(buf))
				.collect(Collectors.toCollection(ArrayList::new));
		ct.setExtraStock(extraSupplies);
		
		return ct;
	}
	
	public static MenuConstructor getServerContainerProvider(LivestockPenBlockEntity be, BlockPos activationPos) {
		return (windowId, playerInv, player) -> new LivestockPenMenu(
				MenuInit.LIVESTOCK_PEN.get(), windowId, playerInv,
				activationPos, be.getBuffer(), be.getSupplies(),
				new LivestockPenData(be), Optional.of(be), ItemStack.EMPTY);
	}
	
	protected LivestockPenMenu(MenuType<? extends LivestockPenMenu> type, int windowId, Inventory playerInv, BlockPos activationPos,
			IItemHandler bufferHandler, IItemHandler suppliesHandler, ContainerData data,
			Optional<? extends LivestockPenBlockEntity> optionalTE, ItemStack icon) {
		super(type, windowId, playerInv, activationPos, bufferHandler, suppliesHandler, data, optionalTE, icon);
	}

	public void setMinimumLivestock(int count) { this.setData(2, count); }
	public int getMinimumLivestock() { return this.data.get(2); }
	
}
