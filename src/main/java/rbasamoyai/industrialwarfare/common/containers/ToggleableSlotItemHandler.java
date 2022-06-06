package rbasamoyai.industrialwarfare.common.containers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ToggleableSlotItemHandler extends SlotItemHandler {

	private boolean isActive;
	
	public ToggleableSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean isActive) {
		super(itemHandler, index, xPosition, yPosition);
		this.isActive = isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	@Override
	public boolean isActive() {
		return this.isActive;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		return this.isActive ? super.mayPlace(stack) : false;
	}

}
