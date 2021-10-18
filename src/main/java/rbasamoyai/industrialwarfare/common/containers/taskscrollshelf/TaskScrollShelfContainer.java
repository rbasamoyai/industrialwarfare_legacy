package rbasamoyai.industrialwarfare.common.containers.taskscrollshelf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.common.tileentities.TaskScrollShelfTileEntity;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;

public class TaskScrollShelfContainer extends Container {

	private static final int SLOT_SPACING = 18;
	
	public static final int SHELF_ROWS = 4;
	public static final int SHELF_COLUMNS = 4;
	public static final int SHELF_SLOT_COUNT = SHELF_ROWS * SHELF_COLUMNS;
	private static final int SHELF_SLOTS_START_X = 53;
	private static final int SHELF_SLOTS_START_Y = 18;
	
	private static final int INVENTORY_ROWS = 3;
	private static final int INVENTORY_COLUMNS = 9;
	private static final int INVENTORY_SLOT_COUNT = INVENTORY_ROWS * INVENTORY_COLUMNS;
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 104;
	private static final int HOTBAR_Y = 162;
	private static final int HOTBAR_SLOT_COUNT = INVENTORY_COLUMNS;
	
	private static final int SHELF_INDEX_START = 0;
	private static final int INVENTORY_INDEX_START = SHELF_SLOT_COUNT;
	
	private final IWorldPosCallable canUse;
	private final Block block;
	
	public static TaskScrollShelfContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		return new TaskScrollShelfContainer(windowId, playerInv, BlockPos.ZERO, new DummyTaskScrollShelfItemHandler(SHELF_SLOT_COUNT));
	}
	
	public static IContainerProvider getServerContainerProvider(TaskScrollShelfTileEntity te, BlockPos activationPos) {
		return (windowId, playerInv, data) -> new TaskScrollShelfContainer(windowId, playerInv, activationPos, te.getItemHandler());
	}
	
	protected TaskScrollShelfContainer(int windowId, PlayerInventory playerInv, BlockPos activationPos, IItemHandler handler) {
		super(ContainerInit.TASK_SCROLL_SHELF.get(), windowId);
		this.canUse = IWorldPosCallable.create(playerInv.player.level, activationPos);
		this.block = playerInv.player.level.getBlockState(activationPos).getBlock();
		
		for (int i = 0; i < SHELF_ROWS; i++) {
			for (int j = 0; j < SHELF_COLUMNS; j++) {
				int index = i * SHELF_COLUMNS + j;
				int x = SHELF_SLOTS_START_X + j * SLOT_SPACING;
				int y = SHELF_SLOTS_START_Y + i * SLOT_SPACING;
				this.addSlot(new SlotItemHandler(handler, index, x, y) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						return stack.getItem() instanceof TaskScrollItem;
					}
				});
			}
		}
		
		for (int i = 0; i < INVENTORY_ROWS; i++) {
			for (int j = 0; j < INVENTORY_COLUMNS; j++) {
				int index = i * INVENTORY_COLUMNS + j + HOTBAR_SLOT_COUNT;
				int x = INVENTORY_START_X + j * SLOT_SPACING;
				int y = INVENTORY_START_Y + i * SLOT_SPACING;
				this.addSlot(new Slot(playerInv, index, x, y));
			}
		}
		
		for (int i = 0; i < HOTBAR_SLOT_COUNT; i++) {
			int x = INVENTORY_START_X + i * SLOT_SPACING;
			this.addSlot(new Slot(playerInv, i, x, HOTBAR_Y));
		}
	}
	
	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
		ItemStack slotCopy = ItemStack.EMPTY;
		Slot slot = this.getSlot(index);
		
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotCopy = slotStack.copy();
			
			if (index < INVENTORY_INDEX_START) { // Move to player inventory
				if (!this.moveItemStackTo(slotStack, INVENTORY_INDEX_START, INVENTORY_INDEX_START + INVENTORY_SLOT_COUNT, true)) {
					return ItemStack.EMPTY;
				}
			} else { // Move to tile entity inventory
				if (!this.moveItemStackTo(slotStack, SHELF_INDEX_START, INVENTORY_INDEX_START, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			}
			
			slot.onTake(player, slotStack);
		}
		
		return slotCopy;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return stillValid(this.canUse, player, this.block);
	}

}
