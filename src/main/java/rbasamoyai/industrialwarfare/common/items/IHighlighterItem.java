package rbasamoyai.industrialwarfare.common.items;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public interface IHighlighterItem {

	default boolean shouldHighlightEntity(ItemStack stack, Entity entity) { return false; }
	
}
