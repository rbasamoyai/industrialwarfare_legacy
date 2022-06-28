package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public interface IRecipeItemData {
	
	void setItemId(Item item);
	void setItemId(ResourceLocation id);
	void setItemId(String id);
	ResourceLocation getItemId();
	
	CompoundTag writeTag(CompoundTag tag);
	void readTag(CompoundTag tag);
	
}
