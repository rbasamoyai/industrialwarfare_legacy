package rbasamoyai.industrialwarfare.common.entities;

import java.util.function.Predicate;

import net.minecraft.item.ItemStack;

public interface IItemPredicateSearch {
	
	ItemStack getMatching(Predicate<ItemStack> predicate);
	boolean has(Predicate<ItemStack> predicate);
	
}
