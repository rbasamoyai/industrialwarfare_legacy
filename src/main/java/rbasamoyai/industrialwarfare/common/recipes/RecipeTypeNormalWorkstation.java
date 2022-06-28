package rbasamoyai.industrialwarfare.common.recipes;

import net.minecraft.world.item.crafting.RecipeType;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class RecipeTypeNormalWorkstation implements RecipeType<ManufactureRecipe> {

	@Override
	public String toString() {
		return IndustrialWarfare.MOD_ID + ":normal_workstation_recipe";
	}
	
}
