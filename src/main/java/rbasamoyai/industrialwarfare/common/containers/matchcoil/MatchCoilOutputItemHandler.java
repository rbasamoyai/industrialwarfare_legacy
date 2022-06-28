package rbasamoyai.industrialwarfare.common.containers.matchcoil;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class MatchCoilOutputItemHandler extends ItemStackHandler {

	private final MatchCoilMenu callback;
	
	public MatchCoilOutputItemHandler(MatchCoilMenu callback) {
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
