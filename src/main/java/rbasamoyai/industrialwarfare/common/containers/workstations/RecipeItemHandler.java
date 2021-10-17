package rbasamoyai.industrialwarfare.common.containers.workstations;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.tileentities.WorkstationTileEntity;

public class RecipeItemHandler extends ItemStackHandler {

	public final WorkstationTileEntity te;
	
	public RecipeItemHandler(WorkstationTileEntity te, int slots) {
		super(slots);
		this.te = te;
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return super.insertItem(slot, stack, simulate);
	}
	
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack before = this.getStackInSlot(slot);
		ItemStack result = super.extractItem(slot, amount, simulate);
		ItemStack after = this.getStackInSlot(slot);
		if (!ItemStack.matches(before, after) && !before.isEmpty()) {
			this.te.haltCrafting();
		}
		return result;
	}
		
	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		this.te.setChangedAndForceUpdate();
	}
}
