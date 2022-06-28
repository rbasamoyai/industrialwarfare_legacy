package rbasamoyai.industrialwarfare.common.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import rbasamoyai.industrialwarfare.core.init.RecipeInit;

public class NormalWorkstationRecipeGetter extends SimplePreparableReloadListener<Void> {

	public static final NormalWorkstationRecipeGetter INSTANCE = new NormalWorkstationRecipeGetter();
	
	private int currentGen = 0;
	private int lastKnownGen = -1;
	
	private List<ManufactureRecipe> cachedRecipes = new ArrayList<>();
	
	public List<ManufactureRecipe> getRecipes(RecipeManager manager, Block block) {
		if (this.currentGen != this.lastKnownGen) {
			this.cachedRecipes = manager.getAllRecipesFor(RecipeInit.NORMAL_WORKSTATION_RECIPE_TYPE)
					.stream()
					.filter(recipe -> recipe.getWorkstation() == block)
					.collect(Collectors.toList());
			this.lastKnownGen = this.currentGen;
		}
		return this.cachedRecipes;
	}

	@Override
	protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
		return null;
	}

	@Override
	protected void apply(Void v, ResourceManager manager, ProfilerFiller profiler) {
		this.currentGen++;
	}
	
}
