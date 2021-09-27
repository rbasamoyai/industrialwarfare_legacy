package rbasamoyai.industrialwarfare.common.containers.npcs;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;

/**
 * Equipment Item handler. Not meant to be serialized/deserialized, as it only is meant to provide
 * an interface between the player and the NPC's ArmorItems and HandItems.
 */

public class EquipmentItemHandler extends ItemStackHandler {
	
	private boolean armorSlotsEnabled;
	private final NPCEntity entity;
	
	public EquipmentItemHandler(NPCEntity entity, boolean armorSlotsEnabled) {
		super(6);
		this.entity = entity;
		this.armorSlotsEnabled = armorSlotsEnabled;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return isItemValid(slot, stack, this.armorSlotsEnabled);
	}
	
	@Override
	protected void onContentsChanged(int slot) {
		this.entity.setItemSlot(getSlotType(slot), this.getStackInSlot(slot));
	}
	
	public void syncEquipmentSlotsToHandler() {
		for (int i = 0; i < 6; i++)
			this.setStackInSlot(i, this.entity.getItemBySlot(getSlotType(i)).copy());
	}
	
	public void setArmorSlotsEnabled(boolean armorSlotsEnabled) {
		this.armorSlotsEnabled = armorSlotsEnabled;
	}
	
	public static EquipmentSlotType getSlotType(int slot) {
		switch (slot) {
		case 0:	return EquipmentSlotType.HEAD;
		case 1:	return EquipmentSlotType.CHEST;
		case 2:	return EquipmentSlotType.LEGS;
		case 3:	return EquipmentSlotType.FEET;
		case 4: return EquipmentSlotType.OFFHAND;
		default: return EquipmentSlotType.MAINHAND;
		}
	}
	
	public static int getTypeSlot(EquipmentSlotType type) {
		switch (type) {
		case HEAD: return 0;
		case CHEST: return 1;
		case LEGS: return 2;
		case FEET: return 3;
		case OFFHAND: return 4;
		default: return 5;
		}
	}
	
	public static boolean isItemValid(int slot, ItemStack stack, boolean enabled) {
		return getSlotType(slot).getType() != EquipmentSlotType.Group.ARMOR || (enabled && NPCEntity.getEquipmentSlotForItem(stack) == getSlotType(slot));
	}
	
}
