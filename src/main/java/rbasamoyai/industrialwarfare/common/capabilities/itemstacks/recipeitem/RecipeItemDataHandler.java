package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import rbasamoyai.industrialwarfare.core.init.items.PartItemInit;

public class RecipeItemDataHandler implements IRecipeItemData {
	
	// Defaulting to the iron wire id to be safe
	private ResourceLocation itemId = PartItemInit.PART_IRON_WIRE.getId();

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
	
	@Override
	public CompoundTag writeTag(CompoundTag tag) {
		tag.putString("recipeItem", this.itemId.toString());
		return tag;
	}
	
	@Override
	public void readTag(CompoundTag tag) {
		this.setItemId(tag.getString("recipeItem"));
	}

}
