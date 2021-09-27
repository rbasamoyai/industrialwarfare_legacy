package rbasamoyai.industrialwarfare.common.itemhandlers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class UninsertableItemHandler extends ItemStackHandler {

	public UninsertableItemHandler(int slots) {
		super(slots);
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return false;
	}
	
}
