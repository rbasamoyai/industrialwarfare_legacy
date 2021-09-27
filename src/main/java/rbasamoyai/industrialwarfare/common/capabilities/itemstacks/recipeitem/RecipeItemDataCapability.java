package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataCapability;

public class RecipeItemDataCapability {

	public static final String TAG_RECIPE_ITEM = "recipeItem";
	
	@CapabilityInject(IRecipeItemDataHandler.class)
	public static Capability<IRecipeItemDataHandler> RECIPE_ITEM_DATA_CAPABILITY = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IRecipeItemDataHandler.class, new Storage(), RecipeItemDataHandler::new);
	}
	
	public static class Storage extends QualityItemDataCapability.Storage<IRecipeItemDataHandler> {
		
		@Override
		public INBT writeNBT(Capability<IRecipeItemDataHandler> capability, IRecipeItemDataHandler instance,
				Direction side) {
			CompoundNBT tag = (CompoundNBT) super.writeNBT(capability, instance, side);
			tag.putString(TAG_RECIPE_ITEM, instance.getItemId().toString());
			return tag;
		}
		
		@Override
		public void readNBT(Capability<IRecipeItemDataHandler> capability, IRecipeItemDataHandler instance,
				Direction side, INBT nbt) {
			super.readNBT(capability, instance, side, nbt);
			CompoundNBT tag = (CompoundNBT) nbt;
			instance.setItemId(tag.getString(TAG_RECIPE_ITEM));
		}
		
	}
}
