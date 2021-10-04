package rbasamoyai.industrialwarfare.common.containers.npcs;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCDataHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.init.ItemInit;

/**
 * Equipment Item handler. Not meant to be serialized/deserialized, as it only is meant to provide
 * an interface between the player and the NPC's ArmorItems and HandItems.
 */

public class EquipmentItemHandler extends ItemStackHandler {
	
	private static final String TAG_TASK_ITEM = "taskItem";
	private static final String TAG_SCHEDULE_ITEM = "scheduleItem";
	
	public static final int HEAD_INDEX = 0;
	public static final int CHEST_INDEX = 1;
	public static final int LEGS_INDEX = 2;
	public static final int FEET_INDEX = 3;
	public static final int OFFHAND_INDEX = 4;
	public static final int MAINHAND_INDEX = 5;
	public static final int TASK_ITEM_INDEX = 6;
	public static final int SCHEDULE_ITEM_INDEX = 7;
	
	private static final Map<EquipmentSlotType, Integer> EQUIPMENT_SLOT_TYPE_MAP = getSlotTypeMap();
	private static Map<EquipmentSlotType, Integer> getSlotTypeMap() {
		Map<EquipmentSlotType, Integer> map = new HashMap<>();
		
		map.put(EquipmentSlotType.HEAD, HEAD_INDEX);
		map.put(EquipmentSlotType.CHEST, CHEST_INDEX);
		map.put(EquipmentSlotType.LEGS, LEGS_INDEX);
		map.put(EquipmentSlotType.FEET, FEET_INDEX);
		map.put(EquipmentSlotType.OFFHAND, OFFHAND_INDEX);
		map.put(EquipmentSlotType.MAINHAND, MAINHAND_INDEX);
		
		return map;
	}
	
	private static final Map<Integer, EquipmentSlotType> EQUIPMENT_TYPE_SLOT_MAP = getTypeSlotMap();
	private static Map<Integer, EquipmentSlotType> getTypeSlotMap() {
		Map<Integer, EquipmentSlotType> map = new HashMap<>();
		
		map.put(HEAD_INDEX, EquipmentSlotType.HEAD);
		map.put(CHEST_INDEX, EquipmentSlotType.CHEST);
		map.put(LEGS_INDEX, EquipmentSlotType.LEGS);
		map.put(FEET_INDEX, EquipmentSlotType.FEET);
		map.put(OFFHAND_INDEX, EquipmentSlotType.OFFHAND);
		map.put(MAINHAND_INDEX, EquipmentSlotType.MAINHAND);
		
		return map;
	}
	
	private NPCEntity entity;
	
	public EquipmentItemHandler(NPCEntity entity) {
		super(8);
		this.entity = entity;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return isItemValid(slot, stack, this.entity.getDataHandler().map(INPCDataHandler::canWearEquipment).orElse(false));
	}
	
	@Override
	protected void onContentsChanged(int slot) {
		if (slot != TASK_ITEM_INDEX && slot != SCHEDULE_ITEM_INDEX)
			this.entity.setItemSlot(getSlotType(slot), this.getStackInSlot(slot));
	}
	
	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		
		tag.put(TAG_TASK_ITEM, this.getStackInSlot(TASK_ITEM_INDEX).serializeNBT());
		tag.put(TAG_SCHEDULE_ITEM, this.getStackInSlot(SCHEDULE_ITEM_INDEX).serializeNBT());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		CompoundNBT workstuffsTag = nbt.contains(NPCEntity.TAG_WORKSTUFFS) ? nbt.getCompound(NPCEntity.TAG_WORKSTUFFS) : new CompoundNBT();
		
		ItemStack taskItem = ItemStack.of(workstuffsTag.getCompound(TAG_TASK_ITEM));
		ItemStack scheduleItem = ItemStack.of(workstuffsTag.getCompound(TAG_SCHEDULE_ITEM));
		
		this.setStackInSlot(TASK_ITEM_INDEX, taskItem);
		this.setStackInSlot(SCHEDULE_ITEM_INDEX, scheduleItem);
		
		ListNBT armorItems = nbt.getList("ArmorItems", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < armorItems.size(); i++) {
			this.setStackInSlot(3 - i, ItemStack.of(armorItems.getCompound(i)));
		}
		
		ListNBT handItems = nbt.getList("HandItems", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < handItems.size(); i++) {
			this.setStackInSlot(4 + i, ItemStack.of(handItems.getCompound(i)));
		}
	}
	
	public static EquipmentSlotType getSlotType(int slot) {
		EquipmentSlotType result = EQUIPMENT_TYPE_SLOT_MAP.get(slot);
		return result == null ? EquipmentSlotType.MAINHAND : result;
	}
	
	public static int getTypeSlot(EquipmentSlotType type) {
		Integer result = EQUIPMENT_SLOT_TYPE_MAP.get(type);
		return result == null ? MAINHAND_INDEX : result.intValue();
	}
	
	public static boolean isItemValid(int slot, ItemStack stack, boolean armorSlotsEnabled) {
		switch (slot) {
		case TASK_ITEM_INDEX: return stack.getItem() instanceof TaskScrollItem;
		case SCHEDULE_ITEM_INDEX: return stack.getItem() == ItemInit.SCHEDULE;
		default: return getSlotType(slot).getType() != EquipmentSlotType.Group.ARMOR || armorSlotsEnabled && NPCEntity.getEquipmentSlotForItem(stack) == getSlotType(slot);
		}
	}
	
}
