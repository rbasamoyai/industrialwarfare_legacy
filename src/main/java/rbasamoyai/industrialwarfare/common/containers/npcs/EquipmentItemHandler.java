package rbasamoyai.industrialwarfare.common.containers.npcs;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCData;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

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
	
	private static final Map<EquipmentSlot, Integer> EQUIPMENT_SLOT_TYPE_MAP = getSlotTypeMap();
	private static Map<EquipmentSlot, Integer> getSlotTypeMap() {
		Map<EquipmentSlot, Integer> map = new HashMap<>();
		
		map.put(EquipmentSlot.HEAD, HEAD_INDEX);
		map.put(EquipmentSlot.CHEST, CHEST_INDEX);
		map.put(EquipmentSlot.LEGS, LEGS_INDEX);
		map.put(EquipmentSlot.FEET, FEET_INDEX);
		map.put(EquipmentSlot.OFFHAND, OFFHAND_INDEX);
		map.put(EquipmentSlot.MAINHAND, MAINHAND_INDEX);
		
		return map;
	}
	
	private static final Map<Integer, EquipmentSlot> EQUIPMENT_TYPE_SLOT_MAP = getTypeSlotMap();
	private static Map<Integer, EquipmentSlot> getTypeSlotMap() {
		Map<Integer, EquipmentSlot> map = new HashMap<>();
		
		map.put(HEAD_INDEX, EquipmentSlot.HEAD);
		map.put(CHEST_INDEX, EquipmentSlot.CHEST);
		map.put(LEGS_INDEX, EquipmentSlot.LEGS);
		map.put(FEET_INDEX, EquipmentSlot.FEET);
		map.put(OFFHAND_INDEX, EquipmentSlot.OFFHAND);
		map.put(MAINHAND_INDEX, EquipmentSlot.MAINHAND);
		
		return map;
	}
	
	private NPCEntity entity;
	
	public EquipmentItemHandler(NPCEntity entity) {
		super(8);
		this.entity = entity;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return isItemValid(slot, stack, this.entity.getDataHandler().map(INPCData::canWearEquipment).orElse(false));
	}
	
	@Override
	protected void onContentsChanged(int slot) {
		if (slot != TASK_ITEM_INDEX && slot != SCHEDULE_ITEM_INDEX)
			this.entity.setItemSlot(getSlotType(slot), this.getStackInSlot(slot));
	}
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		
		tag.put(TAG_TASK_ITEM, this.getStackInSlot(TASK_ITEM_INDEX).serializeNBT());
		tag.put(TAG_SCHEDULE_ITEM, this.getStackInSlot(SCHEDULE_ITEM_INDEX).serializeNBT());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		CompoundTag workstuffsTag = nbt.contains(NPCEntity.TAG_WORKSTUFFS) ? nbt.getCompound(NPCEntity.TAG_WORKSTUFFS) : new CompoundTag();
		
		ItemStack taskItem = ItemStack.of(workstuffsTag.getCompound(TAG_TASK_ITEM));
		ItemStack scheduleItem = ItemStack.of(workstuffsTag.getCompound(TAG_SCHEDULE_ITEM));
		
		this.setStackInSlot(TASK_ITEM_INDEX, taskItem);
		this.setStackInSlot(SCHEDULE_ITEM_INDEX, scheduleItem);
		
		ListTag armorItems = nbt.getList("ArmorItems", Tag.TAG_COMPOUND);
		for (int i = 0; i < armorItems.size(); i++) {
			this.setStackInSlot(3 - i, ItemStack.of(armorItems.getCompound(i)));
		}
		
		ListTag handItems = nbt.getList("HandItems", Tag.TAG_COMPOUND);
		for (int i = 0; i < handItems.size(); i++) {
			this.setStackInSlot(5 - i, ItemStack.of(handItems.getCompound(i)));
		}
	}
	
	public void update() {
		EQUIPMENT_SLOT_TYPE_MAP.forEach((k, v) -> {
			this.setStackInSlot(v, this.entity.getItemBySlot(k));
		});
	}
	
	public static EquipmentSlot getSlotType(int slot) {
		EquipmentSlot result = EQUIPMENT_TYPE_SLOT_MAP.get(slot);
		return result == null ? EquipmentSlot.MAINHAND : result;
	}
	
	public static int getTypeSlot(EquipmentSlot type) {
		Integer result = EQUIPMENT_SLOT_TYPE_MAP.get(type);
		return result == null ? MAINHAND_INDEX : result.intValue();
	}
	
	public static boolean isItemValid(int slot, ItemStack stack, boolean armorSlotsEnabled) {
		switch (slot) {
		case TASK_ITEM_INDEX: return stack.getItem() instanceof TaskScrollItem;
		case SCHEDULE_ITEM_INDEX: return stack.getItem() == ItemInit.SCHEDULE.get();
		default: return getSlotType(slot).getType() != EquipmentSlot.Type.ARMOR || armorSlotsEnabled && NPCEntity.getEquipmentSlotForItem(stack) == getSlotType(slot);
		}
	}
	
}
