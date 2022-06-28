package rbasamoyai.industrialwarfare.common.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.entities.npc.INPCData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.IPartItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.IRecipeItemData;
import rbasamoyai.industrialwarfare.common.entities.NPCEntity;
import rbasamoyai.industrialwarfare.common.items.PartItem;
import rbasamoyai.industrialwarfare.common.items.QualityItem;
import rbasamoyai.industrialwarfare.common.items.RecipeItem;
import rbasamoyai.industrialwarfare.core.config.IWConfig;
import rbasamoyai.industrialwarfare.core.init.RecipeInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;

public class ManufactureRecipe implements Recipe<NormalWorkstationRecipeWrapper> {
	
	public static final String TAG_TYPE = "type";
	public static final String TAG_INPUT = "input";
	public static final String TAG_OUTPUT = "output";
	public static final String TAG_BLOCK = "block";
	public static final String TAG_INPUTS = "inputs";
	public static final String TAG_ITEM = "item";
	public static final String TAG_COUNT = "count";
	public static final String TAG_TAG = "tag"; // Would've called it TAG_ITEM_TAG but nah
	public static final String TAG_ADDED_WEIGHT = "added_weight";
	
	public static final Serializer SERIALIZER = new Serializer();
	private static final Random RNG = new Random();
	
	private final ResourceLocation id;
	private final Map<Ingredient, Integer> ingredients;
	private final ItemStack output;
	private final int addedWeight;
	private final Block workstation;
	
	private static final ResourceLocation INVALID_ID = new ResourceLocation(IndustrialWarfare.MOD_ID, "invalid_id");
	
	public ManufactureRecipe(ResourceLocation id, Map<Ingredient, Integer> ingredients, ItemStack output, int addedWeight, Block workstation) {
		this.id = id;
		this.ingredients = ingredients;
		this.output = output.copy();
		this.addedWeight = addedWeight;
		this.workstation = workstation;
	}
	
	@Override
	public boolean matches(NormalWorkstationRecipeWrapper wrapper, Level level) {
		if (wrapper.getWorkstation() != this.workstation) return false;
		
		// Testing if recipe item or workstation doesn't match, if so we can skip the entire computation
		// Enjoy the if statement gauntlet that is to follow
		ItemStack recipeItem = wrapper.getRecipeItem();
		LazyOptional<IRecipeItemData> optional = RecipeItem.getDataHandler(recipeItem);
		if (recipeItem.getItem() != ItemInit.RECIPE_MANUAL.get()) return false;
		if (!optional.isPresent()) return false;
		
		// Please don't use industrialwarfare:unused_id :pleading:
		// If someone can tell me a better way to safeguard this statement against invalid ids, tell me
		ResourceLocation recipeItemId = optional.map(IRecipeItemData::getItemId).orElse(INVALID_ID);
		if (!ForgeRegistries.ITEMS.containsKey(recipeItemId)) return false;
		if (recipeItemId.equals(INVALID_ID)) return false;
		if (ForgeRegistries.ITEMS.getValue(recipeItemId) != this.output.getItem()) return false; // That's all, folks!
						
		Map<Ingredient, Integer> invIngredientCount = this.copyIngredients();
		invIngredientCount.forEach((ingredient, count) -> { // init all to 0
			invIngredientCount.put(ingredient, 0);
		});
		
		for (int i = 0; i < wrapper.getContainerSize(); i++) {
			for (Ingredient ingredient : invIngredientCount.keySet()) {
				ItemStack stack = wrapper.getItem(i);
				if (ingredient.test(stack)) {
					invIngredientCount.put(ingredient, invIngredientCount.get(ingredient) + stack.getCount());
				}
			}
		}
		
		for (Ingredient ingredient : this.ingredients.keySet()) {
			if (invIngredientCount.get(ingredient) < this.ingredients.get(ingredient)) return false;
		}
		return true;
	}

	@Override
	public ItemStack assemble(NormalWorkstationRecipeWrapper wrapper) {
		float weightSum = IWConfig.part_weight.get() + IWConfig.skill_weight.get() + IWConfig.recipe_weight.get();
		ItemStack newOutput = this.output.copy();

		if (PartItem.getDataHandler(newOutput).isPresent()) {
			Map<Ingredient, Integer> ingredientsCopy = this.copyIngredients();
			
			int partCount = this.ingredients.values().stream().reduce(0, Integer::sum);
			
			float weight = 0.0f;
			for (int i = 0; i < wrapper.getContainerSize(); i++) {
				ItemStack currentItem = wrapper.getItem(i);
				for (Ingredient ingredient : ingredientsCopy.keySet()) {
					if (!ingredient.test(currentItem)) continue;
					if (ingredientsCopy.get(ingredient) <= 0) continue;
					
					int minCount = Math.min(currentItem.getCount(), ingredientsCopy.get(ingredient)); 
					weight += PartItem.getDataHandler(currentItem).map(IPartItemData::getWeight).orElse(1.0f) * minCount;
					ingredientsCopy.put(ingredient, ingredientsCopy.get(ingredient) - minCount);
				}
			}
			if (weight == 0.0f) {
				weight = 1.0f;
			}
			
			List<ItemStack> usedIngredients = this.getUsedIngredientsAndUse(wrapper);
			
			float sum = 0.0f;
			for (ItemStack stack : usedIngredients) {
				float partQuality = PartItem.getDataHandler(stack).map(IPartItemData::getWeight).orElse(1.0f);
				partQuality *= QualityItem.getQuality(stack);
				partQuality *= (float) stack.getCount();
				sum += partQuality;
			}
			float quality = sum == 0.0f ? 1.0f : sum / weight;
			
			// Adding together the weights
			float skill = wrapper.getCrafterOptional().map(crafter -> crafter instanceof NPCEntity
					? ((NPCEntity) crafter).getDataHandler().map(INPCData::getSkill).orElse(0.0f)
					: RNG.nextFloat()).orElse(0.0f);
			skill *= IWConfig.skill_weight.get();
			
			float recipeQuality = QualityItem.getQuality(wrapper.getRecipeItem());
			recipeQuality *= IWConfig.recipe_weight.get();
			
			quality *= IWConfig.part_weight.get();
			quality += skill;
			quality += recipeQuality;
			quality /= weightSum;		
			
			PartItem.setQualityValues(newOutput, quality, partCount, weight + this.addedWeight);
		}
		return newOutput;
	}
	
	private List<ItemStack> getUsedIngredientsAndUse(NormalWorkstationRecipeWrapper inventory) {
		List<ItemStack> result = new ArrayList<>();
		Map<Ingredient, Integer> count = this.copyIngredients();
		
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack currentItem = inventory.getItem(i);
			if (currentItem.isEmpty()) continue;
			
			for (Ingredient ingredient : count.keySet()) {
				int ingredientCount = count.get(ingredient);
				if (ingredientCount <= 0) continue;
				if (!ingredient.test(currentItem)) continue;
				
				int minCount = Math.min(currentItem.getCount(), ingredientCount);
				ItemStack addItem = currentItem.copy();
				addItem.setCount(minCount);
				result.add(addItem);
				currentItem.shrink(minCount);
				count.put(ingredient, ingredientCount - minCount);
				break;
			}
		}
		return result;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return this.output;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.NORMAL_WORKSTATION_RECIPE_TYPE;
	}
	
	public Block getWorkstation() {
		return this.workstation;
	}
	
	private Map<Ingredient, Integer> copyIngredients() {
		return this.ingredients.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
	
	private static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ManufactureRecipe> {

		Serializer() {
			this.setRegistryName(new ResourceLocation(IndustrialWarfare.MOD_ID, "normal_workstation_recipe"));
		}
		
		@Override
		public ManufactureRecipe fromJson(ResourceLocation recipeId, JsonObject jsonObj) {
			JsonArray inputElements = jsonObj.getAsJsonArray(TAG_INPUT);
			
			Map<Ingredient, Integer> ingredients = new HashMap<>();
			for (JsonElement element : inputElements) {
				JsonObject obj = element.getAsJsonObject();
				if (obj.has(TAG_INPUTS))
					ingredients.put(Ingredient.fromJson(obj.getAsJsonArray(TAG_INPUTS)), obj.has(TAG_COUNT) ? obj.get(TAG_COUNT).getAsInt() : 1);
				else
					ingredients.put(Ingredient.fromJson(obj), obj.has(TAG_COUNT) ? obj.get(TAG_COUNT).getAsInt() : 1);
			}
			
			ItemStack output = ShapedRecipe.itemStackFromJson(jsonObj.getAsJsonObject(TAG_OUTPUT));
			int weight = jsonObj.has(TAG_ADDED_WEIGHT) ? jsonObj.get(TAG_ADDED_WEIGHT).getAsInt() : 1;
			Block workstation = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonObj.get(TAG_BLOCK).getAsString()));
			return new ManufactureRecipe(recipeId, ingredients, output, weight, workstation);
		}

		// Weird order to take advantage of that sweet sweet method chaining in toNetwork
		@Override
		public ManufactureRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
			int size = buf.readInt();
			Map<Ingredient, Integer> ingredients = new HashMap<>();
			
			for (int i = 0; i < size; i++) {
				Ingredient keyIngredient = Ingredient.fromNetwork(buf);
				int count = buf.readInt();
				ingredients.put(keyIngredient, count);
			}
			
			ItemStack output = buf.readItem();
			
			ResourceLocation blockId = buf.readResourceLocation();
			Block workstation = ForgeRegistries.BLOCKS.getValue(blockId);
			if (workstation == null)
				throw new IllegalStateException("Block with id " + blockId.toString() + " does not exist, error found in recipe " + recipeId.toString());
			
			int addedWeight = buf.readInt();
			if (addedWeight < 0) {
				addedWeight = 0;
				IndustrialWarfare.LOGGER.warn("NormalWorkstationRecipe of id " + recipeId.toString() + " has an added weight that is less than 0, defaulting to 0...");
			}
			
			return new ManufactureRecipe(recipeId, ingredients, output, addedWeight, workstation);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, ManufactureRecipe recipe) {
			buf.writeInt(recipe.ingredients.size());
			recipe.ingredients.forEach((ingredient, count) -> {
				ingredient.toNetwork(buf);
				buf.writeInt(count);
			});
			buf
					.writeItemStack(recipe.output, true)
					.writeResourceLocation(recipe.workstation.getRegistryName())
					.writeInt(recipe.addedWeight);
		}
		
	}

}
