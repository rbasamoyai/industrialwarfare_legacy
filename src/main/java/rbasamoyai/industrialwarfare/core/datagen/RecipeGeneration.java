package rbasamoyai.industrialwarfare.core.datagen;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.recipes.NormalWorkstationRecipeBuilder;
import rbasamoyai.industrialwarfare.core.init.BlockInit;
import rbasamoyai.industrialwarfare.core.init.items.PartItemInit;

public class RecipeGeneration extends RecipeProvider {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public RecipeGeneration(DataGenerator dataGen) {
		super(dataGen);
	}
	
	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
		LOGGER.debug("Building recipes for rbasamoyai's Inudstrial Warfare mod");
		
		NormalWorkstationRecipeBuilder.recipe(new ItemStack(PartItemInit.PART_IRON_WIRE.get()), 2, 0, BlockInit.ASSEMBLER_WORKSTATION.get())
			.addIngredient(Items.IRON_INGOT, 1)
			.save(consumer, new ResourceLocation(IndustrialWarfare.MOD_ID, "part_iron_wire"));
		
		NormalWorkstationRecipeBuilder.recipe(new ItemStack(PartItemInit.PART_SCREW.get()), 4, 0, BlockInit.ASSEMBLER_WORKSTATION.get())
			.addIngredient(PartItemInit.PART_IRON_WIRE.get(), 1)
			.addIngredient(Items.IRON_NUGGET, 1)
			.save(consumer, new ResourceLocation(IndustrialWarfare.MOD_ID, "part_screw"));
	}

}
