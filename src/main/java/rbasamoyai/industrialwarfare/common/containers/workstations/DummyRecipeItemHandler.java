package rbasamoyai.industrialwarfare.common.containers.workstations;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class DummyRecipeItemHandler extends ItemStackHandler {

	public boolean canInsert = true;
	
	public DummyRecipeItemHandler(int slots) {
		super(slots);
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!this.canInsert) return stack;
		return super.insertItem(slot, stack, simulate);
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return this.canInsert;
	}
	
}
