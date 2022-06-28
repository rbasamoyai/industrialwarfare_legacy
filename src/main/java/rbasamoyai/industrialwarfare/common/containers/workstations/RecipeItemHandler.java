package rbasamoyai.industrialwarfare.common.containers.workstations;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.blockentities.ManufacturingBlockEntity;

public class RecipeItemHandler extends ItemStackHandler {

	public final ManufacturingBlockEntity manufactureBlock;
	
	public RecipeItemHandler(int size, ManufacturingBlockEntity te) {
		super(size);
		this.manufactureBlock = te;
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
			this.manufactureBlock.haltCrafting();
		}
		return result;
	}
		
	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		this.manufactureBlock.setChangedAndForceUpdate();
	}
}
