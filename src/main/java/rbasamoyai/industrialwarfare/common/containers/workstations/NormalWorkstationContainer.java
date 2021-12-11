package rbasamoyai.industrialwarfare.common.containers.workstations;

import java.util.Optional;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.itemhandlers.UninsertableItemHandler;
import rbasamoyai.industrialwarfare.common.tileentities.NormalWorkstationTileEntity;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

/*
 * Container for "normal" workstations (defined in NormalWorkstationContainer.java).
 */

public class NormalWorkstationContainer extends WorkstationContainer {

	private static final Pair<ResourceLocation, ResourceLocation> RECIPE_MANUAL_ICON = Pair.of(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(IndustrialWarfare.MOD_ID, "item/recipe_manual_icon"));
	
	private static final int SLOT_SPACING = 18;
	private static final int INPUT_SLOT_START_X = 8;
	private static final int INPUT_SLOT_COUNT = 5;
	private static final int INPUT_SLOT_Y = 44;
	private static final int OUTPUT_SLOT_X = 134;
	private static final int RECIPE_SLOT_X = 105;
	private static final int RECIPE_SLOT_Y = 20;
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 104;
	private static final int INVENTORY_ROWS = 3;
	private static final int INVENTORY_COLUMNS = 9;
	private static final int INVENTORY_SLOT_COUNT = INVENTORY_ROWS * INVENTORY_COLUMNS;
	private static final int HOTBAR_SLOT_Y = 162;
	private static final int HOTBAR_COUNT = INVENTORY_COLUMNS;
	
	private static final int INPUT_SLOT_INDEX_START = 0;
	private static final int RECIPE_SLOT_INDEX = INPUT_SLOT_COUNT;
	private static final int OUTPUT_SLOT_INDEX = RECIPE_SLOT_INDEX;
	private static final int PLAYERINV_INDEX_START = OUTPUT_SLOT_INDEX + 1;
	private static final int PLAYERINV_INDEX_END = PLAYERINV_INDEX_START + HOTBAR_COUNT + INVENTORY_SLOT_COUNT + 1;
	
	public static NormalWorkstationContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		return new NormalWorkstationContainer(windowId, playerInv, BlockPos.ZERO, new ItemStackHandler(5), new UninsertableItemHandler(1), new DummyRecipeItemHandler(1), new IntArray(7), Optional.empty());
	}
	
	public static IContainerProvider getServerContainerProvider(NormalWorkstationTileEntity te, BlockPos activationPos) {
		return (windowId, playerInv, player) -> new NormalWorkstationContainer(windowId, playerInv, activationPos, te.getInputItemHandler(), te.getOutputItemHandler(), te.getRecipeItemHandler(), new WorkstationDataSync(te, player), Optional.of(te));
	}
	
	public NormalWorkstationContainer(int windowId, PlayerInventory playerInv, BlockPos activationPos, IItemHandler input, IItemHandler output, IItemHandler recipe, IIntArray data, Optional<NormalWorkstationTileEntity> te) {
		super(ContainerInit.NORMAL_WORKSTATION.get(), windowId, playerInv.player, activationPos, data, te);
		
		// Adding block slots
		for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
			int x = INPUT_SLOT_START_X + SLOT_SPACING * i;
			this.addSlot(new SlotItemHandler(input, i, x, INPUT_SLOT_Y));
		}
		this.addSlot(new SlotItemHandler(recipe, 0, RECIPE_SLOT_X, RECIPE_SLOT_Y) {
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return RECIPE_MANUAL_ICON;
			}
		});
		this.addSlot(new SlotItemHandler(output, 0, OUTPUT_SLOT_X, INPUT_SLOT_Y));
		
		// Player inventory slots
		for (int i = 0; i < INVENTORY_ROWS; i++) {
			for (int j = 0; j < INVENTORY_COLUMNS; j++) {
				int x = INVENTORY_START_X + SLOT_SPACING * j;
				int y = INVENTORY_START_Y + SLOT_SPACING * i;
				int index = i * INVENTORY_COLUMNS + j + HOTBAR_COUNT;
				this.addSlot(new Slot(playerInv, index, x, y));
			}
		}
		
		for (int i = 0; i < HOTBAR_COUNT; i++) {
			int x = INVENTORY_START_X + SLOT_SPACING * i;
			this.addSlot(new Slot(playerInv, i, x, HOTBAR_SLOT_Y));
		}
	}
		
	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
		ItemStack slotCopy = ItemStack.EMPTY;
		Slot slot = this.getSlot(index);
		
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotCopy = slotStack.copy();
			
			if (index < PLAYERINV_INDEX_START) {
				if (!this.moveItemStackTo(slotStack, PLAYERINV_INDEX_START, PLAYERINV_INDEX_END, true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (slotStack.getItem() == ItemInit.RECIPE_MANUAL.get()) {
					if (!this.moveItemStackTo(slotStack, RECIPE_SLOT_INDEX, RECIPE_SLOT_INDEX + 1, false)) {
						return ItemStack.EMPTY;
					}
				}
				
				if (!this.moveItemStackTo(slotStack, INPUT_SLOT_INDEX_START, RECIPE_SLOT_INDEX, false)) {
					return ItemStack.EMPTY;
				}
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
	
}
