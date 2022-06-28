package rbasamoyai.industrialwarfare.common.containers.taskscrollshelf;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import rbasamoyai.industrialwarfare.common.blockentities.TaskScrollShelfBlockEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.init.MenuInit;

public class TaskScrollShelfMenu extends AbstractContainerMenu {

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
	
	private final Optional<TaskScrollShelfBlockEntity> optional;
	private final ContainerLevelAccess canUse;
	private final Block block;
	
	public static TaskScrollShelfMenu getClientContainer(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
		return new TaskScrollShelfMenu(windowId, playerInv, BlockPos.ZERO, new DummyTaskScrollShelfItemHandler(SHELF_SLOT_COUNT), Optional.empty());
	}
	
	public static MenuConstructor getServerContainerProvider(TaskScrollShelfBlockEntity te, BlockPos activationPos) {
		return (windowId, playerInv, player) -> new TaskScrollShelfMenu(windowId, playerInv, activationPos, te.getItemHandler(), Optional.of(te));
	}
	
	protected TaskScrollShelfMenu(int windowId, Inventory playerInv, BlockPos activationPos, IItemHandler handler, Optional<TaskScrollShelfBlockEntity> optional) {
		super(MenuInit.TASK_SCROLL_SHELF.get(), windowId);
		this.optional = optional;
		this.canUse = ContainerLevelAccess.create(playerInv.player.level, activationPos);
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
	public ItemStack quickMoveStack(Player player, int index) {
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
			this.optional.ifPresent(TaskScrollShelfBlockEntity::setChanged);
			
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

}
