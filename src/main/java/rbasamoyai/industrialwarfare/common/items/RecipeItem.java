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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.IRecipeItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.RecipeItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.RecipeItemDataProvider;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class RecipeItem extends QualityItem {

	private static final IFormattableTextComponent TOOLTIP_QUALITY = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".recipe.item");
	
	public RecipeItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_RECIPES));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		RecipeItemDataProvider provider = new RecipeItemDataProvider();
		CompoundNBT tag = nbt;
		if (nbt == null) tag = defaultNBT(new CompoundNBT());
		else if (nbt.contains("Parent")) tag = nbt.getCompound("Parent");
		provider.deserializeNBT(tag);
		return provider;
	}
	
	public static CompoundNBT defaultNBT(CompoundNBT nbt) {
		QualityItem.defaultNBT(nbt);
		nbt.putString(RecipeItemDataCapability.TAG_RECIPE_ITEM, "");
		return nbt;
	}
	
	public static LazyOptional<IRecipeItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(RecipeItemDataCapability.RECIPE_ITEM_DATA_CAPABILITY);
	}
	
	@Override
	public CompoundNBT getShareTag(ItemStack stack) {
		CompoundNBT tag = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> {
			tag.put("item_cap", RecipeItemDataCapability.RECIPE_ITEM_DATA_CAPABILITY.writeNBT(h, null));
		});
		return tag;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundNBT nbt) {
		stack.setTag(nbt);
		
		if (nbt == null) return;
		
		if (nbt.contains("creativeData", Constants.NBT.TAG_COMPOUND)) {
			readCreativeData(stack, nbt.getCompound("creativeData"));
			nbt.remove("creativeData");
			return;
		}
		
		getDataHandler(stack).ifPresent(h -> {
			RecipeItemDataCapability.RECIPE_ITEM_DATA_CAPABILITY.readNBT(h, null, nbt.getCompound("item_cap"));
		});
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
	
	public static ItemStack setQualityValues(ItemStack stack, Item recipeItem, float quality) {
		QualityItem.setQualityValues(stack, quality);
		getDataHandler(stack).ifPresent(h -> {
			h.setItemId(recipeItem);
		});
		return stack;
	}
	
	public static ItemStack stackOf(Item recipeItem, float quality) {
		return setQualityValues(new ItemStack(ItemInit.RECIPE_MANUAL.get()), recipeItem, quality);
	}
	
	/* Creative mode methods to get around issue where capability data is lost */
	
	public static ItemStack creativeStack(Item item, float quality) {
		ItemStack stack = stackOf(item, quality);
		stack.getOrCreateTag().put("creativeData", getCreativeData(stack));
		return stack;
	}
	
	public static CompoundNBT getCreativeData(ItemStack stack) {
		CompoundNBT tag = QualityItem.getCreativeData(stack);
		getDataHandler(stack).ifPresent(h -> {
			tag.putString(RecipeItemDataCapability.TAG_RECIPE_ITEM, h.getItemId().toString());
		});
		return tag;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundNBT nbt) {
		QualityItem.readCreativeData(stack, nbt);
		getDataHandler(stack).ifPresent(h -> {
			h.setItemId(nbt.getString(RecipeItemDataCapability.TAG_RECIPE_ITEM));
		});
	}
	
}
