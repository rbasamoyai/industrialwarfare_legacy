package rbasamoyai.industrialwarfare.common.containers.resourcestation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.blockentities.ResourceStationBlockEntity;
import rbasamoyai.industrialwarfare.common.containers.ToggleableSlotItemHandler;
import rbasamoyai.industrialwarfare.common.entityai.SupplyRequestPredicate;
import rbasamoyai.industrialwarfare.core.init.MenuInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.SRemoveExtraStock;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.SSetExtraStock;

public class ResourceStationMenu extends AbstractContainerMenu {

	private static final int SLOT_SPACING = 18;
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 86;
	private static final int INVENTORY_ROWS = 3;
	private static final int INVENTORY_COLUMNS = 9;
	private static final int HOTBAR_SLOT_Y = 144;
	private static final int HOTBAR_COUNT = INVENTORY_COLUMNS;
	private static final int BLOCK_INVENTORY_ROWS = 3;
	private static final int BLOCK_INVENTORY_COLUMNS = 9;
	private static final int BLOCK_INVENTORY_START_X = 8;
	private static final int BLOCK_INVENTORY_START_Y = 18;
	
	private static final int BUFFER_SLOTS_INDEX = 0;
	private static final int SUPPLIES_SLOTS_INDEX = BUFFER_SLOTS_INDEX + 27;
	private static final int PLAYER_INVENTORY_INDEX = SUPPLIES_SLOTS_INDEX + 27;
	private static final int INVENTORY_INDEX_END = PLAYER_INVENTORY_INDEX + 36;
	
	private final ContainerLevelAccess canUse;
	private final Block block;
	
	private final Optional<? extends ResourceStationBlockEntity> optionalTE;
	private final List<ToggleableSlotItemHandler> bufferSlots = new ArrayList<>();
	private final List<ToggleableSlotItemHandler> suppliesSlots = new ArrayList<>();
	private int selectedTab = 0;
	private final ItemStack icon;
	private final List<SupplyRequestPredicate> requests = new ArrayList<>();
	private final List<SupplyRequestPredicate> extraStock = new ArrayList<>();
	private final ContainerData data;
	
	private boolean changed = false;
	
	public static MenuConstructor getServerContainerProvider(ResourceStationBlockEntity te, BlockPos activationPos) {
		return (windowId, playerInv, player) -> new ResourceStationMenu(MenuInit.RESOURCE_STATION.get(),
				windowId, playerInv, activationPos, te.getBuffer(), te.getSupplies(), new ResourceStationData(te),
				Optional.of(te), ItemStack.EMPTY);
	}
	
	public static ResourceStationMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		ResourceStationMenu ct = new ResourceStationMenu(MenuInit.RESOURCE_STATION.get(), windowId,
				playerInv, buf.readBlockPos(), new ItemStackHandler(27), new ItemStackHandler(27), new SimpleContainerData(2),
				Optional.empty(), buf.readItem());
		
		ct.setRunning(buf.readBoolean());
		
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
	
	protected ResourceStationMenu(MenuType<? extends ResourceStationMenu> type, int windowId,
			Inventory playerInv, BlockPos activationPos, IItemHandler bufferHandler, IItemHandler suppliesHandler,
			ContainerData data, Optional<? extends ResourceStationBlockEntity> optionalTE, ItemStack icon) {
		super(type, windowId);
		this.canUse = ContainerLevelAccess.create(playerInv.player.level, activationPos);
		this.block = playerInv.player.level.getBlockState(activationPos).getBlock();
		this.icon = icon;
		this.data = data;
		this.optionalTE = optionalTE;
		
		for (int i = 0; i < BLOCK_INVENTORY_ROWS; ++i) {
			for (int j = 0; j < BLOCK_INVENTORY_COLUMNS; ++j) {
				int x = BLOCK_INVENTORY_START_X + j * SLOT_SPACING;
				int y = BLOCK_INVENTORY_START_Y + i * SLOT_SPACING;
				int index = i * BLOCK_INVENTORY_COLUMNS + j;
				ToggleableSlotItemHandler slot = new ToggleableSlotItemHandler(bufferHandler, index, x, y, true);
				this.bufferSlots.add(slot);
				this.addSlot(slot);
			}
		}
		
		for (int i = 0; i < BLOCK_INVENTORY_ROWS; ++i) {
			for (int j = 0; j < BLOCK_INVENTORY_COLUMNS; ++j) {
				int x = BLOCK_INVENTORY_START_X + j * SLOT_SPACING;
				int y = BLOCK_INVENTORY_START_Y + i * SLOT_SPACING;
				int index = i * BLOCK_INVENTORY_COLUMNS + j;
				ToggleableSlotItemHandler slot = new ToggleableSlotItemHandler(suppliesHandler, index, x, y, true);
				this.suppliesSlots.add(slot);
				this.addSlot(slot);
			}
		}
		
		for (int i = 0; i < INVENTORY_ROWS; ++i) {
			for (int j = 0; j < INVENTORY_COLUMNS; ++j) {
				int x = INVENTORY_START_X + j * SLOT_SPACING;
				int y = INVENTORY_START_Y + i * SLOT_SPACING;
				int index = i * INVENTORY_COLUMNS + j + HOTBAR_COUNT;
				this.addSlot(new Slot(playerInv, index, x, y));
			}
		}
		
		for (int i = 0; i < HOTBAR_COUNT; ++i) {
			int x = INVENTORY_START_X + i * SLOT_SPACING;
			this.addSlot(new Slot(playerInv, i, x, HOTBAR_SLOT_Y));
		}
		
		this.addDataSlots(data);
		
		this.setSelected(0);
		this.setChanged(true);
	}
	
	public void setSelected(int tab) {
		this.selectedTab = 0 <= tab && tab < 4 ? tab : 0;
		this.suppliesSlots.forEach(s -> s.setActive(this.selectedTab == 1));
		this.bufferSlots.forEach(s -> s.setActive(this.selectedTab == 2));
	}
	public int getSelected() { return this.selectedTab; }
	
	public void setRequests(List<SupplyRequestPredicate> requests) {
		this.requests.clear();
		this.requests.addAll(requests);
		this.setChanged(true);
	}
	
	public List<SupplyRequestPredicate> getRequests() { return this.requests; }
	
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	public void setOrAddExtraStock(SupplyRequestPredicate request, int index) {
		if (this.optionalTE.isPresent()) {
			this.optionalTE.get().setOrAddExtraStock(request, index);
		} else {
			IWNetwork.CHANNEL.sendToServer(new SSetExtraStock(request, index));
		}
	}
	
	public void removeExtraStock(int index) {
		if (this.optionalTE.isPresent()) {
			this.optionalTE.get().removeExtraStock(index);
		} else {
			IWNetwork.CHANNEL.sendToServer(new SRemoveExtraStock(index));
		}
	}
	
	public void setExtraStock(List<SupplyRequestPredicate> extraSupplies) {
		this.extraStock.clear();
		this.extraStock.addAll(extraSupplies);
		this.setChanged(true);
	}
	
	public List<SupplyRequestPredicate> getExtraStock() { return this.extraStock; }
	
	public boolean isChanged() { return this.changed; }
	
	public void setRunning(boolean running) { this.data.set(0, running ? 1 : 0); }
	public boolean isRunning() { return this.data.get(0) != 0; }
	
	public boolean isFinished() { return this.data.get(1) != 0; }
	
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (this.selectedTab == 0) return ItemStack.EMPTY;
		
		ItemStack slotCopy = ItemStack.EMPTY;
		Slot slot = this.getSlot(index);
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotCopy = slotStack.copy();
			
			if (index < PLAYER_INVENTORY_INDEX) {
				if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_INDEX, INVENTORY_INDEX_END, true)) {
					return ItemStack.EMPTY;
				}
			} else if (this.selectedTab == 1 && !this.moveItemStackTo(slotStack, SUPPLIES_SLOTS_INDEX, PLAYER_INVENTORY_INDEX, false)) {
				return ItemStack.EMPTY;
			} else if (!this.moveItemStackTo(slotStack, BUFFER_SLOTS_INDEX, SUPPLIES_SLOTS_INDEX, false)) {
				return ItemStack.EMPTY;
			}
			
			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			
			slot.onTake(player, slotStack);
		}
		
		return slotCopy;
	}
	
	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.canUse, player, this.block);
	}
	
	public ItemStack getIcon() { return this.icon; }

}
