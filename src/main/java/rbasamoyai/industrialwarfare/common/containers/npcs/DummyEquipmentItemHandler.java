package rbasamoyai.industrialwarfare.common.containers.npcs;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerData;
import net.minecraftforge.items.ItemStackHandler;

public class DummyEquipmentItemHandler extends ItemStackHandler {
	
	private final ContainerData data;
	
	public DummyEquipmentItemHandler(ContainerData data) {
		super(8);
		this.data = data;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return EquipmentItemHandler.isItemValid(slot, stack, this.data.get(2) > 0);
	}
	
}
