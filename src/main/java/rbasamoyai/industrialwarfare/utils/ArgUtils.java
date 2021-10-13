package rbasamoyai.industrialwarfare.utils;

import net.minecraft.item.ItemStack;

public class ArgUtils {

	public static boolean filterMatches(ItemStack filter, ItemStack stack) {
		if (filter.isEmpty() && !stack.isEmpty()) {
			return true;
		} else {
			// TODO: more complex stuff, such as filter items
			return filter.getItem().equals(stack.getItem());
		}
	}
	
}
