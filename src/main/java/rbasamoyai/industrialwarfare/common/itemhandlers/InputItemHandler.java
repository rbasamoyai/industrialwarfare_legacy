package rbasamoyai.industrialwarfare.common.itemhandlers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;

public class InputItemHandler extends ItemStackHandler {

	public final WorkstationTileEntity te;
	
	public InputItemHandler(WorkstationTileEntity te, int slots) {
		super(slots);
		this.te = te;
	}
	
	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		this.te.setChangedAndForceUpdate();
	}
	
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack result = super.extractItem(slot, amount, simulate);
		return result;
	}
	
}
