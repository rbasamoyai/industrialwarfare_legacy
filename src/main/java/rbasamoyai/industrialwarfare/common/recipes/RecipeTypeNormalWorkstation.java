package rbasamoyai.industrialwarfare.common.recipes;

import net.minecraft.item.crafting.IRecipeType;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class RecipeTypeNormalWorkstation implements IRecipeType<NormalWorkstationRecipe> {

	@Override
	public String toString() {
		return IndustrialWarfare.MOD_ID + ":normal_workstation_recipe";
	}
	
}
