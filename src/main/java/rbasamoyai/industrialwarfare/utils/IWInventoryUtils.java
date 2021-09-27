package rbasamoyai.industrialwarfare.utils;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class IWInventoryUtils {

	public static void dropHandlerItems(IItemHandler handler, double x, double y, double z, World worldIn) {
		for (int i = 0; i < handler.getSlots(); i++) {
			InventoryHelper.dropItemStack(worldIn, x, y, z, handler.getStackInSlot(i));
		}
	}
	
}
