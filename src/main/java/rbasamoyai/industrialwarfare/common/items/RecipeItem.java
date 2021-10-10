package rbasamoyai.industrialwarfare.common.items;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.IRecipeItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.RecipeItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.RecipeItemDataProvider;
import rbasamoyai.industrialwarfare.core.init.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class RecipeItem extends QualityItem {

	private static final IFormattableTextComponent TOOLTIP_QUALITY = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".recipe.item");
	
	public RecipeItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_RECIPES), "recipe_manual");
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		RecipeItemDataProvider provider = new RecipeItemDataProvider();
		provider.deserializeNBT(nbt == null ? this.defaultNBT(new CompoundNBT()) : nbt);
		return provider;
	}
	
	@Override
	public CompoundNBT defaultNBT(CompoundNBT nbt) {
		super.defaultNBT(nbt);
		nbt.putString(RecipeItemDataCapability.TAG_RECIPE_ITEM, "");
		return nbt;
	}
	
	public static LazyOptional<IRecipeItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(RecipeItemDataCapability.RECIPE_ITEM_DATA_CAPABILITY);
	}

	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		super.appendHoverText(stack, world, tooltip, flag);
		
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_QUALITY,
				getDataHandler(stack)
						.map(h -> {
							ResourceLocation id = h.getItemId();
							if (ForgeRegistries.ITEMS.containsKey(id)) {
								Item item = ForgeRegistries.ITEMS.getValue(id);
								String firstKeyPart = item instanceof BlockItem ? "block." : "item.";
								return new TranslationTextComponent(firstKeyPart + id.getNamespace() + "." + id.getPath());
							} else return TooltipUtils.NOT_AVAILABLE;
						})
						.orElse(TooltipUtils.NOT_AVAILABLE))
		);
	}
	
	public static ItemStack getRecipeManualOf(Item recipeItem) {
		ItemStack stack = new ItemStack(ItemInit.RECIPE_MANUAL);
		getDataHandler(stack).ifPresent(h -> h.setItemId(recipeItem));
		return stack;
	}
	
}
