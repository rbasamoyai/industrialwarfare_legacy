package rbasamoyai.industrialwarfare.common.containers.taskscrollshelf;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import rbasamoyai.industrialwarfare.common.blockentities.TaskScrollShelfBlockEntity;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollItem;

public class TaskScrollShelfItemHandler extends ItemStackHandler {

	public final TaskScrollShelfBlockEntity te;
	
	public TaskScrollShelfItemHandler(TaskScrollShelfBlockEntity te, int count) {
		super(count);
		
		this.te = te;
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack result = stack;
		if (stack.getItem() instanceof TaskScrollItem) result = super.insertItem(slot, stack, simulate);
		return result;
	}
	
	@Override
	protected void onContentsChanged(int slot) {
		this.te.setChanged();
	}
	
	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}
	
}
