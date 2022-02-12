package rbasamoyai.industrialwarfare.common.items;

import java.util.function.Predicate;

import net.minecraft.item.ItemStack;

public interface ISpeedloadable {

	boolean canSpeedload(ItemStack stack);
	
	Predicate<ItemStack> getSpeedloaderPredicate();
	
}
