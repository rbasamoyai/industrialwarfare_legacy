package rbasamoyai.industrialwarfare.common.containers.npcs;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraftforge.items.ItemStackHandler;

public class DummyEquipmentItemHandler extends ItemStackHandler {
	
	private final IIntArray data;
	
	public DummyEquipmentItemHandler(IIntArray data) {
		super(8);
		this.data = data;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return EquipmentItemHandler.isItemValid(slot, stack, this.data.get(2) > 0);
	}
	
}
