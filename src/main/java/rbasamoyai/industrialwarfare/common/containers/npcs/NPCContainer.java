package rbasamoyai.industrialwarfare.common.containers.npcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.schedule.Activity;
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
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.itemhandlers.ToggleableSlotItemHandler;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.init.ContainerInit;
import rbasamoyai.industrialwarfare.core.init.ItemInit;
import rbasamoyai.industrialwarfare.core.init.MemoryModuleTypeInit;

/*
 * Base NPC container class. Interfaces with the task slot and the inventory.
 */

public class NPCContainer extends Container {
	
	private static final Pair<ResourceLocation, ResourceLocation> TASK_ICON = Pair.of(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(IndustrialWarfare.MOD_ID, "item/task_icon"));
	private static final Pair<ResourceLocation, ResourceLocation> SCHEDULE_ICON = Pair.of(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(IndustrialWarfare.MOD_ID, "item/schedule_icon"));
	
	private static final int SLOT_SPACING = 18;
	
	private static final int PLAYER_INVENTORY_START_X = 8;
	private static final int PLAYER_INVENTORY_START_Y = 154;
	private static final int PLAYER_INVENTORY_ROWS = 3;
	private static final int PLAYER_INVENTORY_COLUMNS = 9;
	private static final int PLAYER_HOTBAR_SLOT_Y = 212;
	private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS;
	private static final int PLAYER_HOTBAR_COUNT = PLAYER_INVENTORY_COLUMNS;
	
	// public so NPCBaseScreen can access
	public static final int NPC_EQUIPMENT_SLOTS_CENTER_X = 134;
	public static final int NPC_EQUIPMENT_SLOTS_CENTER_START_Y = 32;
	public static final int NPC_EQUIPMENT_SLOTS_LEFT_X = NPC_EQUIPMENT_SLOTS_CENTER_X - SLOT_SPACING;
	public static final int NPC_EQUIPMENT_SLOTS_RIGHT_X = NPC_EQUIPMENT_SLOTS_CENTER_X + SLOT_SPACING;
	public static final int NPC_EQUIPMENT_SLOTS_SIDE_Y = NPC_EQUIPMENT_SLOTS_CENTER_START_Y + SLOT_SPACING;
	public static final int NPC_EQUIPMENT_ARMOR_SLOTS_COUNT = 5; // Counting offhand as equipment
	public static final int NPC_EQUIPMENT_HAND_SLOTS_COUNT = 1; 
	public static final int NPC_EQUIPMENT_WORKSTUFFS_SLOTS_COUNT = 2;
	public static final int NPC_EQUIPMENT_SLOT_COUNT = NPC_EQUIPMENT_ARMOR_SLOTS_COUNT + NPC_EQUIPMENT_HAND_SLOTS_COUNT + NPC_EQUIPMENT_WORKSTUFFS_SLOTS_COUNT;
	
	public static final int NPC_INVENTORY_START_X = 8;
	public static final int NPC_INVENTORY_START_Y = 32;
	public static final int NPC_INVENTORY_COLUMNS = 9;
	
	private static final int PLAYER_INVENTORY_SLOTS_START_INDEX = 0;
	private static final int NPC_EQUIPMENT_SLOTS_START_INDEX = PLAYER_INVENTORY_SLOTS_START_INDEX + PLAYER_HOTBAR_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
	private static final int NPC_EQUIPMENT_ARMOR_SLOTS_START_INDEX = NPC_EQUIPMENT_SLOTS_START_INDEX;
	private static final int NPC_EQUIPMENT_HAND_SLOTS_START_INDEX = NPC_EQUIPMENT_ARMOR_SLOTS_START_INDEX + NPC_EQUIPMENT_ARMOR_SLOTS_COUNT;
	private static final int NPC_EQUIPMENT_WORKSTUFFS_SLOTS_START_INDEX = NPC_EQUIPMENT_HAND_SLOTS_START_INDEX + NPC_EQUIPMENT_HAND_SLOTS_COUNT;
	private static final int NPC_INVENTORY_START_INDEX = NPC_EQUIPMENT_WORKSTUFFS_SLOTS_START_INDEX + NPC_EQUIPMENT_WORKSTUFFS_SLOTS_COUNT;
	
	private static final ResourceLocation[] EQUIPMENT_SLOT_ICONS = new ResourceLocation[] {
			PlayerContainer.EMPTY_ARMOR_SLOT_HELMET,
			PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE,
			PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS,
			PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS,
			PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD
			};
	
	protected final List<ToggleableSlotItemHandler> npcEquipmentSlots = new ArrayList<>(NPC_EQUIPMENT_SLOT_COUNT); 
	protected final List<ToggleableSlotItemHandler> npcInventorySlots = new ArrayList<>();
	
	protected final int npcInventoryEndIndex;
	
	protected final Optional<? extends NPCEntity> entityOptional;
	protected final IIntArray data;
	
	public static NPCContainer getClientContainer(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
		int npcSlots = buf.readVarInt();
		boolean armorSlotsEnabled = buf.readBoolean();
		
		IIntArray data = new IntArray(3);
		data.set(0, npcSlots);
		data.set(2, armorSlotsEnabled ? 1 : 0);
		
		return new NPCContainer(ContainerInit.NPC_BASE, windowId, playerInv, new DummyEquipmentItemHandler(data), new ItemStackHandler(npcSlots), data, Optional.empty());
	}
	
	public static IContainerProvider getServerContainerProvider(NPCEntity entity) {
		return (windowId, playerInv, data) -> new NPCContainer(ContainerInit.NPC_BASE, windowId, playerInv, entity.getEquipmentItemHandler(), entity.getInventoryItemHandler(), new NPCContainerDataSync(entity, playerInv.player), Optional.of(entity));
	}
	
	protected NPCContainer(ContainerType<?> type, int windowId, PlayerInventory playerInv, IItemHandler equipmentItemHandler, IItemHandler inventoryItemHandler, IIntArray data, Optional<? extends NPCEntity> entity) {
		super(type, windowId);
		
		this.data = data;
		this.entityOptional = entity;
		
		// Player slots
		for (int i = 0; i < PLAYER_INVENTORY_ROWS; i++) {
			for (int j = 0; j < PLAYER_INVENTORY_COLUMNS; j++) {
				int x = PLAYER_INVENTORY_START_X + j * SLOT_SPACING;
				int y = PLAYER_INVENTORY_START_Y + i * SLOT_SPACING;
				int index = i * PLAYER_INVENTORY_COLUMNS + j + PLAYER_HOTBAR_COUNT;
				this.addSlot(new Slot(playerInv, index, x, y));
			}
		}
		
		for (int i = 0; i < PLAYER_HOTBAR_COUNT; i++) {
			int x = PLAYER_INVENTORY_START_X + SLOT_SPACING * i;
			this.addSlot(new Slot(playerInv, i, x, PLAYER_HOTBAR_SLOT_Y));
		}
		
		// NPC slots
		for (int i = 0; i < NPC_EQUIPMENT_ARMOR_SLOTS_COUNT; i++) {
			boolean isOffhandSlot = i == EquipmentItemHandler.OFFHAND_INDEX;
			int x = isOffhandSlot ? NPC_EQUIPMENT_SLOTS_LEFT_X : NPC_EQUIPMENT_SLOTS_CENTER_X;
			int y = isOffhandSlot ? NPC_EQUIPMENT_SLOTS_SIDE_Y : NPC_EQUIPMENT_SLOTS_CENTER_START_Y + i * SLOT_SPACING;
			
			this.npcEquipmentSlots.add(
					(ToggleableSlotItemHandler) this.addSlot(new ToggleableSlotItemHandler(equipmentItemHandler, i, x, y, true) {
						@Override
						public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
							return Pair.of(PlayerContainer.BLOCK_ATLAS, EQUIPMENT_SLOT_ICONS[this.getSlotIndex()]);
						}
					}));
		}
		
		this.npcEquipmentSlots.add((ToggleableSlotItemHandler) this.addSlot(new ToggleableSlotItemHandler(equipmentItemHandler, EquipmentItemHandler.MAINHAND_INDEX, NPC_EQUIPMENT_SLOTS_RIGHT_X, NPC_EQUIPMENT_SLOTS_SIDE_Y, true)));
		
		this.npcEquipmentSlots.add((ToggleableSlotItemHandler) this.addSlot(new ToggleableSlotItemHandler(equipmentItemHandler, EquipmentItemHandler.TASK_ITEM_INDEX, NPC_EQUIPMENT_SLOTS_LEFT_X, NPC_EQUIPMENT_SLOTS_SIDE_Y + SLOT_SPACING, true) {
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return TASK_ICON;
			}
		}));
		
		this.npcEquipmentSlots.add((ToggleableSlotItemHandler) this.addSlot(new ToggleableSlotItemHandler(equipmentItemHandler, EquipmentItemHandler.SCHEDULE_ITEM_INDEX, NPC_EQUIPMENT_SLOTS_RIGHT_X, NPC_EQUIPMENT_SLOTS_SIDE_Y + SLOT_SPACING, true) {
			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return SCHEDULE_ICON;
			}
		}));
		
		for (int i = 0; i < inventoryItemHandler.getSlots(); i++) {
			int x = NPC_INVENTORY_START_X + i % NPC_INVENTORY_COLUMNS * SLOT_SPACING;
			int y = NPC_INVENTORY_START_Y + i / NPC_INVENTORY_COLUMNS * SLOT_SPACING;
			this.npcInventorySlots.add((ToggleableSlotItemHandler) this.addSlot(new ToggleableSlotItemHandler(inventoryItemHandler, i, x, y, false)));
		}
		
		this.npcInventoryEndIndex = this.slots.size();
		
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
			
			if (index < NPC_EQUIPMENT_ARMOR_SLOTS_START_INDEX) { // Move stack from player to NPC
				EquipmentSlotType type = NPCEntity.getEquipmentSlotForItem(slotStack);
				int equipmentSlot = NPC_EQUIPMENT_ARMOR_SLOTS_START_INDEX + EquipmentItemHandler.getTypeSlot(type);
				
				if (type != EquipmentSlotType.MAINHAND && this.getSlot(equipmentSlot).getItem().isEmpty()) {
					if (!this.moveItemStackTo(slotStack, equipmentSlot, equipmentSlot + 1, false)) {
						return ItemStack.EMPTY;
					}
				}
				
				if (slotCopy.getItem() instanceof TaskScrollItem) {
					if (this.moveItemStackTo(slotStack, NPC_EQUIPMENT_WORKSTUFFS_SLOTS_START_INDEX, NPC_EQUIPMENT_WORKSTUFFS_SLOTS_START_INDEX + 1, false)) {
						// TODO: Update entity if this happens
					} else {
						return ItemStack.EMPTY;
					}
				}
				
				if (slotCopy.getItem() == ItemInit.SCHEDULE) {
					if (this.moveItemStackTo(slotStack, NPC_EQUIPMENT_WORKSTUFFS_SLOTS_START_INDEX + 1, NPC_EQUIPMENT_WORKSTUFFS_SLOTS_START_INDEX + 2, false)) {
						// TODO: Update entity if this happens
					} else {
						return ItemStack.EMPTY;
					}
				}
				
				if (this.moveItemStackTo(slotStack, NPC_INVENTORY_START_INDEX, this.npcInventoryEndIndex, false)) {
					
				} else {
					return ItemStack.EMPTY;
				}
			} else { // Move stack from NPC to player
				if (slotCopy.getItem() instanceof TaskScrollItem) {
					this.entityOptional.ifPresent(npc -> {
						Brain<?> brain = npc.getBrain();
						brain.setMemory(MemoryModuleTypeInit.STOP_EXECUTION, true);
					});
				} else if (slotCopy.getItem() == ItemInit.SCHEDULE) {
					this.entityOptional.ifPresent(npc -> {
						Brain<?> brain = npc.getBrain();
						brain.setActiveActivityIfPossible(Activity.IDLE);
					});
				}
				
				if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_SLOTS_START_INDEX, NPC_EQUIPMENT_ARMOR_SLOTS_START_INDEX, true)) {
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
	
	public void setNPCEquipmentSlotsActive(boolean active) {
		this.npcEquipmentSlots.forEach(s -> s.setActive(active));
	}
	
	public void setNPCInventorySlotsActive(boolean active) {
		this.npcInventorySlots.forEach(s -> s.setActive(active));
	}

}
