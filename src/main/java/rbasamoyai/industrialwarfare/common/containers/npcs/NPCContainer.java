package rbasamoyai.industrialwarfare.common.containers.npcs;

import java.util.Optional;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;

/*
 * Base NPC container class. Interfaces with the task slot and the inventory.
 */

public class NPCContainer extends Container {
	
	private static final Pair<ResourceLocation, ResourceLocation> TASK_ICON = Pair.of(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(IndustrialWarfare.MOD_ID, "item/task_icon"));
	
	private static final int SLOT_SPACING = 18;
	private static final int TASK_SLOT_X = 143;
	private static final int TASK_SLOT_Y = 31;
	private static final int NPC_INVENTORY_START_X = 80;
	private static final int NPC_INVENTORY_START_Y = 72;
	private static final int NPC_INVENTORY_COLUMNS = 5;
	private static final int NPC_EQUIPMENT_START_X = 80;
	private static final int NPC_EQUIPMENT_START_Y = 22;
	private static final int NPC_EQUIPMENT_ROWS = 2;
	private static final int NPC_EQUIPMENT_COLUMNS = 3;
	private static final int NPC_EQUIPMENT_SLOT_COUNT = NPC_EQUIPMENT_ROWS * NPC_EQUIPMENT_COLUMNS;
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 140;
	private static final int INVENTORY_ROWS = 3;
	private static final int INVENTORY_COLUMNS = 9;
	private static final int INVENTORY_SLOT_COUNT = INVENTORY_ROWS * INVENTORY_COLUMNS;
	private static final int HOTBAR_SLOT_Y = 198;
	private static final int HOTBAR_COUNT = INVENTORY_COLUMNS;
	
	protected static final int NPC_TASK_SLOT_INDEX = 0;
	protected static final int NPC_EQUIPMENT_SLOTS_START = 1;
	protected static final int NPC_INVENTORY_SLOTS_START = NPC_EQUIPMENT_SLOTS_START + NPC_EQUIPMENT_SLOT_COUNT;
	
	private static final ResourceLocation[] EQUIPMENT_SLOT_ICONS = new ResourceLocation[] {
			PlayerContainer.EMPTY_ARMOR_SLOT_HELMET,
			PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE,
			PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS,
			PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS,
			PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD
			};
	
	protected final int playerInvStartIndex;
	protected final int playerInvEndIndex;
	
	protected final Optional<? extends NPCEntity> entityOptional;
	protected final IIntArray data;
	
	public static NPCContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		return new NPCContainer(ContainerInit.NPC_BASE, windowId, playerInv, new ItemStackHandler(1), new ItemStackHandler(buf.readInt()), new DummyEquipmentItemHandler(buf.readBoolean()), new IntArray(3), Optional.empty());
	}
	
	public static IContainerProvider getServerContainerProvider(NPCEntity entity) {
		return (windowId, playerInv, data) -> new NPCContainer(ContainerInit.NPC_BASE, windowId, playerInv, entity.getTaskItemHandler(), entity.getInventoryItemHandler(), entity.getEquipmentItemHandler(), new NPCContainerDataSync(entity, playerInv.player), Optional.of(entity));
	}
	
	protected NPCContainer(ContainerType<?> type, int windowId, PlayerInventory playerInv, IItemHandler taskSlot, IItemHandler inventorySlots, IItemHandler equipmentSlots, IIntArray data, Optional<? extends NPCEntity> entity) {
		super(type, windowId);
		
		this.data = data;
		this.entityOptional = entity;
		
		// NPC slots
		this.addSlot(new SlotItemHandler(taskSlot, 0, TASK_SLOT_X, TASK_SLOT_Y) {
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return TASK_ICON;
			}
		});
		
		for (int i = 0; i < NPC_EQUIPMENT_ROWS; i++) {
			for (int j = 0; j < NPC_EQUIPMENT_COLUMNS; j++) {
				int x = NPC_EQUIPMENT_START_X + j * SLOT_SPACING;
				int y = NPC_EQUIPMENT_START_Y + i * SLOT_SPACING;
				int index = i * NPC_EQUIPMENT_COLUMNS + j;
				
				this.addSlot(new SlotItemHandler(equipmentSlots, index, x, y) {
					@Override
					public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
						if (this.getSlotIndex() == 5) return super.getNoItemIcon();
						return Pair.of(PlayerContainer.BLOCK_ATLAS, EQUIPMENT_SLOT_ICONS[this.getSlotIndex()]);
					}
				});
			}
		}
		
		for (int i = 0; i < inventorySlots.getSlots(); i++) {
			int x = NPC_INVENTORY_START_X + i % NPC_INVENTORY_COLUMNS * SLOT_SPACING;
			int y = NPC_INVENTORY_START_Y + i / NPC_INVENTORY_COLUMNS * SLOT_SPACING;
			this.addSlot(new SlotItemHandler(inventorySlots, i, x, y));
		}
		
		this.playerInvStartIndex = NPC_INVENTORY_SLOTS_START + inventorySlots.getSlots();
		this.playerInvEndIndex = this.playerInvStartIndex + HOTBAR_COUNT + INVENTORY_SLOT_COUNT;
		
		// Player slots
		for (int i = 0; i < INVENTORY_ROWS; i++) {
			for (int j = 0; j < INVENTORY_COLUMNS; j++) {
				int x = INVENTORY_START_X + j * SLOT_SPACING;
				int y = INVENTORY_START_Y + i * SLOT_SPACING;
				int index = i * INVENTORY_COLUMNS + j + HOTBAR_COUNT;
				this.addSlot(new Slot(playerInv, index, x, y));
			}
		}
		
		for (int i = 0; i < HOTBAR_COUNT; i++) {
			int x = INVENTORY_START_X + SLOT_SPACING * i;
			this.addSlot(new Slot(playerInv, i, x, HOTBAR_SLOT_Y));
		}
		
		this.addDataSlots(data);
	}
	
	@Override
	public boolean stillValid(PlayerEntity player) {
		return this.data.get(1) > 0;
	}
	
	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
		ItemStack slotCopy = ItemStack.EMPTY;
		Slot slot = this.getSlot(index);
		
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotCopy = slotStack.copy();
			
			if (index < this.playerInvStartIndex) {
				if (slotCopy.getItem() instanceof TaskScrollItem) {
					// TODO: Update entity tasks if this happens
				}
				
				if (!this.moveItemStackTo(slotStack, this.playerInvStartIndex, this.playerInvEndIndex, true)) {
					return ItemStack.EMPTY;
				}
			} else {
				EquipmentSlotType type = NPCEntity.getEquipmentSlotForItem(slotStack);
				int equipmentSlot = NPC_EQUIPMENT_SLOTS_START + EquipmentItemHandler.getTypeSlot(type);
				
				if ((type.getType() == EquipmentSlotType.Group.ARMOR || type == EquipmentSlotType.OFFHAND) && this.getSlot(equipmentSlot).getItem().isEmpty()) {
					if (!this.moveItemStackTo(slotStack, equipmentSlot, equipmentSlot + 1, false)) {
						return ItemStack.EMPTY;
					}
				}
				
				if (slotCopy.getItem() instanceof TaskScrollItem) {
					if (this.moveItemStackTo(slotStack, NPC_TASK_SLOT_INDEX, NPC_TASK_SLOT_INDEX + 1, false)) {
						// TODO: Update entity if this happens
					} else {
						return ItemStack.EMPTY;
					}
				}
				
				if (this.moveItemStackTo(slotStack, NPC_INVENTORY_SLOTS_START, this.playerInvStartIndex, false)) {
					
				} else {
					return ItemStack.EMPTY;
				}
			}
			
			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			
			if (slotStack.getCount() == slotCopy.getCount()) {
				return ItemStack.EMPTY;
			}
			
			slot.onTake(player, slotStack);
		}
		
		return slotCopy;
	}
	
	public int getInvSlotCount() {
		return this.data.get(0);
	}
	
	public boolean areArmorSlotsEnabled() {
		return this.data.get(2) > 0;
	}
	
	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
	}

}
