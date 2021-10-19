package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataHandler;
import rbasamoyai.industrialwarfare.core.init.ItemInit;

public class RecipeItemDataHandler extends QualityItemDataHandler implements IRecipeItemDataHandler {
	
	// Defaulting to the iron wire id to be safe
	private ResourceLocation itemId = ItemInit.PART_IRON_WIRE.getId();

	@Override
	public void setItemId(Item item) {
		this.itemId = item.getRegistryName();
	}
	
	@Override
	public void setItemId(ResourceLocation id) {
		this.itemId = id;
	}
	
	@Override
	public void setItemId(String id) {
		this.itemId = new ResourceLocation(id);
	}

	@Override
	public ResourceLocation getItemId() {
		return this.itemId;
	}

}
