package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.IQualityItemDataHandler;

public interface IRecipeItemDataHandler extends IQualityItemDataHandler {
	
	public void setItemId(Item item);
	public void setItemId(ResourceLocation id);
	public void setItemId(String id);
	public ResourceLocation getItemId();
	
}
