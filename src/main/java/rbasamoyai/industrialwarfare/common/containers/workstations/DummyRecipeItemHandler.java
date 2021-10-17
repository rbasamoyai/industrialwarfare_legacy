package rbasamoyai.industrialwarfare.common.containers.workstations;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class DummyRecipeItemHandler extends ItemStackHandler {

	public boolean canInteract = true;
	
	public DummyRecipeItemHandler(int slots) {
		super(slots);
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!this.canInteract) return stack;
		return super.insertItem(slot, stack, simulate);
	}
	
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (!this.canInteract) return ItemStack.EMPTY;
		return super.extractItem(slot, amount, simulate);
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return this.canInteract;
	}
	
}
