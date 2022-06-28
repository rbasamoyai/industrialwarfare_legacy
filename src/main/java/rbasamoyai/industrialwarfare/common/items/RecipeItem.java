package rbasamoyai.industrialwarfare.common.items;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.IQualityItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.IRecipeItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.RecipeItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.recipeitem.RecipeItemDataHandler;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class RecipeItem extends Item {

	private static final MutableComponent TOOLTIP_QUALITY = new TranslatableComponent("tooltip." + IndustrialWarfare.MOD_ID + ".recipe.item");
	
	public RecipeItem() {
		super(new Item.Properties().stacksTo(1).tab(IWItemGroups.TAB_RECIPES));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		BundledProvider provider = new BundledProvider();
		if (nbt == null) {
			provider.getCapability(RecipeItemCapability.INSTANCE).ifPresent(h -> {
				h.setItemId("");
			});
			provider.getCapability(QualityItemCapability.INSTANCE).ifPresent(h -> {
				h.setQuality(1.0f);
			});
		} else {
			provider.deserializeNBT(nbt.contains("Parent") ? nbt.getCompound("Parent") : nbt);
		}
		return provider;
	}
	
	private static class BundledProvider implements ICapabilitySerializable<CompoundTag> {
		private final IRecipeItemData recipeDataInterface = new RecipeItemDataHandler();
		private final IQualityItemData qualityDataInterface = new QualityItemDataHandler();
		private final LazyOptional<IRecipeItemData> recipeDataOptional = LazyOptional.of(() -> this.recipeDataInterface);
		private final LazyOptional<IQualityItemData> qualityDataOptional = LazyOptional.of(() -> this.qualityDataInterface);
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if (cap == QualityItemCapability.INSTANCE) {
				return this.qualityDataOptional.cast();
			}
			return cap == RecipeItemCapability.INSTANCE ? this.recipeDataOptional.cast() : LazyOptional.empty();
		}
		
		@Override
		public CompoundTag serializeNBT() {
			CompoundTag tag = new CompoundTag();
			this.qualityDataInterface.writeTag(tag);
			return this.recipeDataInterface.writeTag(tag);
		}
		
		@Override
		public void deserializeNBT(CompoundTag nbt) {
			this.qualityDataInterface.readTag(nbt);
			this.recipeDataInterface.readTag(nbt);
		}
	}
	
	public static LazyOptional<IRecipeItemData> getDataHandler(ItemStack stack) {
		return stack.getCapability(RecipeItemCapability.INSTANCE);
	}
	
	@Override
	public CompoundTag getShareTag(ItemStack stack) {
		CompoundTag itemCap = new CompoundTag();
		getDataHandler(stack).ifPresent(h -> h.writeTag(itemCap));
		QualityItem.getDataHandler(stack).ifPresent(h -> h.writeTag(itemCap));
		CompoundTag tag = stack.getOrCreateTag();
		tag.put("item_cap", itemCap);
		return tag;
	}
	
	@Override
	public void readShareTag(ItemStack stack, CompoundTag nbt) {
		stack.setTag(nbt);
		
		if (nbt == null) return;
		
		if (nbt.contains("creativeData", Tag.TAG_COMPOUND)) {
			readCreativeData(stack, nbt.getCompound("creativeData"));
			nbt.remove("creativeData");
			return;
		}
		
		CompoundTag itemCap = nbt.getCompound("item_cap");
		getDataHandler(stack).ifPresent(h -> h.readTag(itemCap));
		QualityItem.getDataHandler(stack).ifPresent(h -> h.readTag(itemCap));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_QUALITY,
				getDataHandler(stack)
						.map(h -> {
							ResourceLocation id = h.getItemId();
							if (ForgeRegistries.ITEMS.containsKey(id)) {
								Item item = ForgeRegistries.ITEMS.getValue(id);
								String firstKeyPart = item instanceof BlockItem ? "block." : "item.";
								return new TranslatableComponent(firstKeyPart + id.getNamespace() + "." + id.getPath());
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
	
	public static CompoundTag getCreativeData(ItemStack stack) {
		CompoundTag tag = QualityItem.getCreativeData(stack);
		getDataHandler(stack).ifPresent(h -> {
			tag.putString("recipeItem", h.getItemId().toString());
		});
		return tag;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundTag nbt) {
		QualityItem.readCreativeData(stack, nbt);
		getDataHandler(stack).ifPresent(h -> {
			h.setItemId(nbt.getString("recipeItem"));
		});
	}
	
}
