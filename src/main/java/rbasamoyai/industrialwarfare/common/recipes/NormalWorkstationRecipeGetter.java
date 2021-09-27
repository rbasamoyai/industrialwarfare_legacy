package rbasamoyai.industrialwarfare.common.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import rbasamoyai.industrialwarfare.core.init.RecipeInit;

public class NormalWorkstationRecipeGetter extends ReloadListener<Void> {

	public static final NormalWorkstationRecipeGetter INSTANCE = new NormalWorkstationRecipeGetter();
	
	private int currentGen = 0;
	private int lastKnownGen = -1;
	
	private List<NormalWorkstationRecipe> cachedRecipes = new ArrayList<>();
	
	public List<NormalWorkstationRecipe> getRecipes(RecipeManager manager, Block block) {
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
	protected Void prepare(IResourceManager manager, IProfiler profiler) {
		return null;
	}

	@Override
	protected void apply(Void v, IResourceManager manager, IProfiler profiler) {
		this.currentGen++;
	}
	
}
