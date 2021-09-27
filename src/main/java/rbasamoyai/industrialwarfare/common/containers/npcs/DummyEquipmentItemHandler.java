package rbasamoyai.industrialwarfare.common.containers.npcs;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class DummyEquipmentItemHandler extends ItemStackHandler {

	public boolean armorSlotsEnabled;
	
	public DummyEquipmentItemHandler(boolean armorSlotsEnabled) {
		super(6);
		this.armorSlotsEnabled = armorSlotsEnabled;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return EquipmentItemHandler.isItemValid(slot, stack, this.armorSlotsEnabled);
	}
	
}
