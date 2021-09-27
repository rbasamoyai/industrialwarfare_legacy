package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class RecipeItemDataProvider implements ICapabilitySerializable<CompoundNBT> {

	private final RecipeItemDataHandler dataHandler = new RecipeItemDataHandler();
	private final LazyOptional<IRecipeItemDataHandler> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return this.dataOptional.cast();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return RecipeItemDataCapability.RECIPE_ITEM_DATA_CAPABILITY == null
				? new CompoundNBT()
				: (CompoundNBT) RecipeItemDataCapability.RECIPE_ITEM_DATA_CAPABILITY.writeNBT(this.dataHandler, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (RecipeItemDataCapability.RECIPE_ITEM_DATA_CAPABILITY != null)
			RecipeItemDataCapability.RECIPE_ITEM_DATA_CAPABILITY.readNBT(this.dataHandler, null, nbt);
	}

}
