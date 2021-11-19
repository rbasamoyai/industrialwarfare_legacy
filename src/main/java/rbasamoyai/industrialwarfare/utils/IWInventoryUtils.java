package rbasamoyai.industrialwarfare.utils;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class IWInventoryUtils {

	public static void dropHandlerItems(IItemHandler handler, double x, double y, double z, World worldIn) {
		for (int i = 0; i < handler.getSlots(); i++) {
			InventoryHelper.dropItemStack(worldIn, x, y, z, handler.getStackInSlot(i));
		}
	}
	
	public static <T> T iterateAndApplyIf(
			IItemHandler handler,
			BiFunction<IItemHandler, Integer, T> function,
			Predicate<ItemStack> stackPredicate,
			Predicate<T> resultPredicate,
			Supplier<T> orElse) {
		for (int i = 0; i < handler.getSlots(); ++i) {
			if (stackPredicate.test(handler.getStackInSlot(i))) {
				T result = function.apply(handler, i);
				// Allows for continuing the search if going through a deeper container yields nothing
				if (stackPredicate == resultPredicate || resultPredicate.test(result)) return result;
			}
		}
		return orElse.get();
	}
	
}
