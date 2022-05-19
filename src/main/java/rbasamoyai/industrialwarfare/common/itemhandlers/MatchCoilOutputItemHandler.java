package rbasamoyai.industrialwarfare.common.itemhandlers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.containers.MatchCoilContainer;

public class MatchCoilOutputItemHandler extends ItemStackHandler {

	private final MatchCoilContainer callback;
	
	public MatchCoilOutputItemHandler(MatchCoilContainer callback) {
		super(1);
		this.callback = callback;
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		this.setStackInSlot(slot, stack);
		return ItemStack.EMPTY;
	}
	
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack result = super.extractItem(slot, amount, simulate);
		if (!simulate) {
			this.callback.updateCoil(result);
		}
		return result;
	}
	
	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		this.callback.broadcastChanges();
	}
	
}
