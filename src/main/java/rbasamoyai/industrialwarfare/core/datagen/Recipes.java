package rbasamoyai.industrialwarfare.core.datagen;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.recipes.NormalWorkstationRecipeBuilder;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.ItemInit;

public class Recipes extends RecipeProvider {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public Recipes(DataGenerator dataGen) {
		super(dataGen);
	}
	
	@Override
	protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
		LOGGER.debug("Building recipes for rbasamoyai's Inudstrial Warfare mod");
		
		NormalWorkstationRecipeBuilder.recipe(new ItemStack(ItemInit.PART_SCREW), 4, 0, BlockInit.ASSEMBLER_WORKSTATION)
			.addIngredient(ItemInit.PART_IRON_WIRE, 1)
			.addIngredient(Items.IRON_NUGGET, 1)
			.save(consumer, new ResourceLocation(IndustrialWarfare.MOD_ID, "part_screw"));
		
		NormalWorkstationRecipeBuilder.recipe(new ItemStack(ItemInit.PART_IRON_WIRE), 2, 0, BlockInit.ASSEMBLER_WORKSTATION)
			.addIngredient(Items.IRON_INGOT, 1)
			.save(consumer, new ResourceLocation(IndustrialWarfare.MOD_ID, "part_iron_wire"));
	}

}
