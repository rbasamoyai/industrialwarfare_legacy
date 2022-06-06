package rbasamoyai.industrialwarfare.common.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.tileentities.ResourceStationTileEntity;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;

public class ResourceStationContainer extends Container {

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
	
	private final IWorldPosCallable canUse;
	private final Block block;
	
	private final List<ToggleableSlotItemHandler> bufferSlots = new ArrayList<>();
	private final List<ToggleableSlotItemHandler> suppliesSlots = new ArrayList<>();
	private int selectedTab = 0;
	private final ItemStack icon;
	
	public static IContainerProvider getServerContainerProvider(ResourceStationTileEntity te, BlockPos activationPos) {
		return (windowId, playerInv, player) -> new ResourceStationContainer(ContainerInit.RESOURCE_STATION.get(),
				windowId, playerInv, activationPos, te.getBuffer(), te.getSupplies(), Optional.of(te), ItemStack.EMPTY);
	}
	
	public static ResourceStationContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		return new ResourceStationContainer(ContainerInit.RESOURCE_STATION.get(), windowId, playerInv,
				buf.readBlockPos(), new ItemStackHandler(27), new ItemStackHandler(27), Optional.empty(), buf.readItem());
	}
	
	protected ResourceStationContainer(ContainerType<? extends ResourceStationContainer> type, int windowId, PlayerInventory playerInv, BlockPos activationPos,
			IItemHandler bufferHandler, IItemHandler suppliesHandler, Optional<? extends ResourceStationTileEntity> optionalTE, ItemStack icon) {
		super(type, windowId);
		this.canUse = IWorldPosCallable.create(playerInv.player.level, activationPos);
		this.block = playerInv.player.level.getBlockState(activationPos).getBlock();
		this.icon = icon;
		
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
		
		this.setSelected(0);
	}
	
	public void setSelected(int tab) {
		this.selectedTab = 0 <= tab && tab < 3 ? tab : 0;
		this.suppliesSlots.forEach(s -> s.setActive(this.selectedTab == 1));
		this.bufferSlots.forEach(s -> s.setActive(this.selectedTab == 2));
	}
	public int getSelected() { return this.selectedTab; }
	
	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
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
	public boolean stillValid(PlayerEntity player) {
		return stillValid(this.canUse, player, this.block);
	}
	
	public ItemStack getIcon() { return this.icon; }

}
