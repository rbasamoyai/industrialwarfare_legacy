package rbasamoyai.industrialwarfare.common.itemhandlers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;

public class OutputItemHandler extends ItemStackHandler {

	public final WorkstationTileEntity te;
	public boolean canInsert = false;
	
	public OutputItemHandler(WorkstationTileEntity te, int slots) {
		super(slots);
		this.te = te;
	}
	
	public ItemStack insertResult(int slot, ItemStack stack, boolean simulate) {
		this.canInsert = true;
		ItemStack result = this.insertItem(slot, stack, simulate);
		this.canInsert = false;
		return result;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return this.canInsert;
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!this.canInsert) return stack;
		else return super.insertItem(slot, stack, simulate);
	}
	
	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		this.te.setChanged();
	}
	
}
