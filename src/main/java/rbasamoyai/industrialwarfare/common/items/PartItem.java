package rbasamoyai.industrialwarfare.common.items;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.IPartItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.IQualityItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataHandler;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class PartItem extends Item {
	
	private static final MutableComponent TOOLTIP_PART_COUNT = new TranslatableComponent("tooltip." + IndustrialWarfare.MOD_ID + ".part.part_count");
	private static final MutableComponent TOOLTIP_WEIGHT = new TranslatableComponent("tooltip." + IndustrialWarfare.MOD_ID + ".part.weight");
	
	public PartItem() {
		super(new Item.Properties().tab(IWItemGroups.TAB_PARTS));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		BundledProvider provider = new BundledProvider();
		if (nbt == null) {
			provider.getCapability(PartItemCapability.INSTANCE).ifPresent(h -> {
				h.setPartCount(1);
				h.setWeight(1.0f);
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
		private final IPartItemData partDataInterface = new PartItemDataHandler();
		private final IQualityItemData qualityDataInterface = new QualityItemDataHandler();
		private final LazyOptional<IPartItemData> partDataOptional = LazyOptional.of(() -> this.partDataInterface);
		private final LazyOptional<IQualityItemData> qualityDataOptional = LazyOptional.of(() -> this.qualityDataInterface);
		
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			if (cap == QualityItemCapability.INSTANCE) {
				return this.qualityDataOptional.cast();
			}
			return cap == PartItemCapability.INSTANCE ? this.partDataOptional.cast() : LazyOptional.empty();
		}
		
		@Override
		public CompoundTag serializeNBT() {
			CompoundTag tag = new CompoundTag();
			this.qualityDataInterface.writeTag(tag);
			return this.partDataInterface.writeTag(tag);
		}
		
		@Override
		public void deserializeNBT(CompoundTag nbt) {
			this.qualityDataInterface.readTag(nbt);
			this.partDataInterface.readTag(nbt);
		}
	}
	
	public static LazyOptional<IPartItemData> getDataHandler(ItemStack stack) {
		return stack.getCapability(PartItemCapability.INSTANCE);
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
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, world, tooltip, flag);
		appendHoverTextStatic(stack, world, tooltip, flag);
	}
	
	public static void appendHoverTextStatic(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
		LazyOptional<IPartItemData> optional = getDataHandler(stack);
		
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_PART_COUNT,
				optional
						.map(h -> (MutableComponent) new TextComponent(Integer.toString(h.getPartCount())))
						.orElse(TooltipUtils.NOT_AVAILABLE))
		);
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_WEIGHT, 
				optional
						.map(h -> (MutableComponent) new TextComponent(TooltipUtils.formatFloat(h.getWeight())))
						.orElse(TooltipUtils.NOT_AVAILABLE))
		);

	}

	public static ItemStack setQualityValues(ItemStack stack, float quality, int partCount, float weight) {
		QualityItem.setQualityValues(stack, quality);
		stack.getCapability(PartItemCapability.INSTANCE).ifPresent(h -> {
			h.setPartCount(partCount);
			h.setWeight(weight);
		});
		return stack;
	}
	
	public static ItemStack stackOf(Item item, float quality, int partCount, float weight) {
		return setQualityValues(new ItemStack(item), quality, partCount, weight);
	}
	
	/* Creative mode methods to get around issue where capability data is lost */
	
	public static ItemStack creativeStack(Item item, float quality, int partCount, float weight) {
		ItemStack stack = stackOf(item, quality, partCount, weight);
		stack.getOrCreateTag().put("creativeData", getCreativeData(stack));
		return stack;
	}
	
	public static CompoundTag getCreativeData(ItemStack stack) {
		CompoundTag tag = QualityItem.getCreativeData(stack);
		getDataHandler(stack).ifPresent(h -> {
			tag.putInt("partCount", h.getPartCount());
			tag.putFloat("weight", h.getWeight());
		});
		return tag;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundTag nbt) {
		QualityItem.readCreativeData(stack, nbt);
		getDataHandler(stack).ifPresent(h -> {
			h.setPartCount(nbt.getInt("partCount"));
			h.setWeight(nbt.getFloat("weight"));
		});
	}
	
}
