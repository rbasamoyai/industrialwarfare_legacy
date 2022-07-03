package rbasamoyai.industrialwarfare.common.containers;

import java.util.Optional;

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
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationMenu;
import rbasamoyai.industrialwarfare.core.init.MenuInit;

public class LivestockPenMenu extends ResourceStationMenu {

	public static LivestockPenMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		return new LivestockPenMenu(MenuInit.LIVESTOCK_PEN.get(), windowId,
				playerInv, buf.readBlockPos(), new ItemStackHandler(27),
				new ItemStackHandler(27), new SimpleContainerData(3),
				Optional.empty(), buf.readItem());
	}
	
	public static MenuConstructor getServerContainerProvider(LivestockPenBlockEntity be, BlockPos activationPos) {
		return (windowId, playerInv, player) -> new LivestockPenMenu(
				MenuInit.LIVESTOCK_PEN.get(), windowId, playerInv,
				activationPos, be.getBuffer(), be.getSupplies(),
				new SimpleContainerData(3), Optional.of(be), ItemStack.EMPTY);
	}
	
	protected LivestockPenMenu(MenuType<? extends LivestockPenMenu> type, int windowId, Inventory playerInv, BlockPos activationPos,
			IItemHandler bufferHandler, IItemHandler suppliesHandler, ContainerData data,
			Optional<? extends LivestockPenBlockEntity> optionalTE, ItemStack icon) {
		super(type, windowId, playerInv, activationPos, bufferHandler, suppliesHandler, data, optionalTE, icon);
	}

	
	
}
