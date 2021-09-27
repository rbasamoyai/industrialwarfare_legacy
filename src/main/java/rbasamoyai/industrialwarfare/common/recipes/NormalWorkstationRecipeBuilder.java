package rbasamoyai.industrialwarfare.common.recipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class NormalWorkstationRecipeBuilder {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Map<Ingredient, BuilderValueData> ingredients;
	private final Block workstation;
	private final ItemStack output;
	private final int addedWeight;
	
	private NormalWorkstationRecipeBuilder(final ItemStack output, final int count, final int addedWeight, final Block workstation) {
		this.output = output.copy();
		this.workstation = workstation;
		this.ingredients = new HashMap<>();
		this.output.setCount(count);
		this.addedWeight = addedWeight;
	}
	
	public static NormalWorkstationRecipeBuilder recipe(ItemStack output, int count, int addedWeight, Block workstation) {
		return new NormalWorkstationRecipeBuilder(output, count, addedWeight, workstation);
	}
	
	public NormalWorkstationRecipeBuilder addIngredient(Ingredient ingredient, int count) {
		this.checkAddIngredientArgs(ingredient, count);
		this.ingredients.put(ingredient, new BuilderValueData(count, false)); // This method does not know whether the ingredient was created using ITag<Item>
		return this;
	}
	
	public NormalWorkstationRecipeBuilder addIngredient(IItemProvider item, int count) {
		this.checkAddIngredientArgs(item, count);
		Ingredient ingredient = Ingredient.of(item);
		this.ingredients.put(ingredient, new BuilderValueData(count, false));
		return this;
	}
	
	public NormalWorkstationRecipeBuilder addIngredient(ITag<Item> tag, int count) {
		this.checkAddIngredientArgs(tag, count);
		Ingredient ingredient = Ingredient.of(tag);
		this.ingredients.put(ingredient, new BuilderValueData(count, true));
		return this;
	}
	
	// TODO: Add ingredient checks (e.g. count, if this makes an invalid recipe (not enough stacks in workstation), etc.)
	private void checkAddIngredientArgs(Object o, int count) {
		if (count < 1) throw new IllegalArgumentException("Ingredient count must be at least 1");
		if (o instanceof IItemProvider) {
			
		} else if (o instanceof Ingredient) {
			
		} else if (o instanceof ITag<?>) {
			// Maybe TODO: somehow check for ITag<Item> instead of ITag<?> ? This function is private anyway, so unless if access tranformers are
			// used, I don't see anyone really futzing around with this function by passing incorrect ITag<?> arguments.
			
		} else {
			throw new IllegalArgumentException("The object to check is not a valid object (must be a subclass of/implement IItemProvider, Ingredient, or ITag<Item>");
		}
	}
	
	public void save(Consumer<IFinishedRecipe> consumer, ResourceLocation id) {
		LOGGER.debug("Generating recipe with id " + id.toString());
		consumer.accept(new NormalWorkstationRecipeBuilder.Result(id, this.output, this.addedWeight, this.ingredients, this.workstation));
	}
	
	public class Result implements IFinishedRecipe {
		
		public static final String RECIPE_TYPE = IndustrialWarfare.MOD_ID + ":normal_workstation_recipe";
		
		private final ResourceLocation id;
		private final ItemStack output;
		private final int addedWeight;
		private final Map<Ingredient, BuilderValueData> ingredients;
		private final Block workstation;
		
		public Result(final ResourceLocation id, final ItemStack output, final int addedWeight, final Map<Ingredient, BuilderValueData> ingredients, final Block workstation) {
			this.id = id;
			this.output = output.copy();
			this.addedWeight = addedWeight;
			this.ingredients = ingredients;
			this.workstation = workstation;
		}

		@Override
		public void serializeRecipeData(JsonObject jsonObj) {
			jsonObj.addProperty(NormalWorkstationRecipe.TAG_TYPE, RECIPE_TYPE);
			
			JsonArray inputJsonArray = new JsonArray();
			
			for (Entry<Ingredient, BuilderValueData> entry : this.ingredients.entrySet()) {
				Ingredient input = entry.getKey();
				BuilderValueData data = entry.getValue();
				
				JsonObject obj = new JsonObject();
				if (data.isTag)
					obj = input.toJson().getAsJsonObject();
				else if (input.getItems().length == 1)
					obj = input.toJson().getAsJsonObject();
				else
					obj.add(NormalWorkstationRecipe.TAG_INPUTS, input.toJson());
				if (data.ingredientCount > 1) obj.addProperty(NormalWorkstationRecipe.TAG_COUNT, data.ingredientCount);
				inputJsonArray.add(obj);
			}
			
			jsonObj.add(NormalWorkstationRecipe.TAG_INPUT, inputJsonArray);
			
			JsonObject outputObj = new JsonObject();
			outputObj.addProperty(NormalWorkstationRecipe.TAG_ITEM, this.output.getItem().getRegistryName().toString());
			if (this.output.getCount() > 1) outputObj.addProperty(NormalWorkstationRecipe.TAG_COUNT, this.output.getCount());
			if (this.addedWeight > 0) outputObj.addProperty(NormalWorkstationRecipe.TAG_ADDED_WEIGHT, this.addedWeight);
			
			jsonObj.add(NormalWorkstationRecipe.TAG_OUTPUT, outputObj);
			
			jsonObj.addProperty(NormalWorkstationRecipe.TAG_BLOCK, this.workstation.getRegistryName().toString());
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public IRecipeSerializer<?> getType() {
			return NormalWorkstationRecipe.SERIALIZER;
		}

		@Deprecated
		@Override
		public JsonObject serializeAdvancement() { // No advancements, crafting uses Industrial Warfare's own tech tree system
			return null;
		}

		@Deprecated
		@Override
		public ResourceLocation getAdvancementId() { // No advancements, crafting uses Industrial Warfare's own tech tree system
			return null;
		}
		
	}
	
	/*
	 * A rather unelegant way (in my opinion) to include some stuff like the ingredient count, if it is a tag for file optimization,
	 * etc. If it works, it works. Intended for Map use only.
	 */
	
	private static class BuilderValueData {
		public final int ingredientCount;
		public final boolean isTag;
		
		public BuilderValueData(int ingredientCount, boolean isTag) {
			this.ingredientCount = ingredientCount;
			this.isTag = isTag;
		}
	}
}