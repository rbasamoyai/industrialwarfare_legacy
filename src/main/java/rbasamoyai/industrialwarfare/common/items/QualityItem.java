package rbasamoyai.industrialwarfare.common.items;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.IQualityItemData;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataProvider;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class QualityItem extends Item {
	
	private static final MutableComponent TOOLTIP_QUALITY = new TranslatableComponent("tooltip." + IndustrialWarfare.MOD_ID + ".quality");
	
	public QualityItem(Item.Properties properties) {
		super(properties);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		QualityItemDataProvider provider = new QualityItemDataProvider();
		if (nbt == null) {
			provider.getCapability(QualityItemCapability.INSTANCE).ifPresent(h -> {
				h.setQuality(1.0f);
			});
		} else {
			provider.deserializeNBT(nbt.contains("Parent") ? nbt.getCompound("Parent") : nbt);
		}
		return provider;
	}
	
	public static LazyOptional<IQualityItemData> getDataHandler(ItemStack stack) {
		return stack.getCapability(QualityItemCapability.INSTANCE);
	}
	
	public static float getQuality(ItemStack stack) {
		return QualityItem.getDataHandler(stack).map(IQualityItemData::getQuality).orElse(1.0f);
	}
	
	@Override
	public CompoundTag getShareTag(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> tag.put("item_cap", h.writeTag(new CompoundTag())));
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
		
		getDataHandler(stack).ifPresent(h -> h.readTag(nbt.getCompound("item_cap")));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Level Level, List<Component> tooltip, TooltipFlag flag) {
		appendHoverTextStatic(stack, Level, tooltip, flag);
	}
	
	public static void appendHoverTextStatic(ItemStack stack, Level Level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_QUALITY,
				getDataHandler(stack)
						.map(h -> Screen.hasShiftDown()
								? new TextComponent(TooltipUtils.formatFloat(h.getQuality() * 100.0f).concat("%"))
								: TooltipUtils.getWordedQuality(h.getQuality()))
						.orElseGet(() -> TooltipUtils.NOT_AVAILABLE))
		);
	}

	public static ItemStack setQualityValues(ItemStack stack, float quality) {
		getDataHandler(stack).ifPresent(h -> h.setQuality(quality));
		return stack;
	}
	
	public static ItemStack stackOf(Item item, float quality) {
		return setQualityValues(new ItemStack(item), quality);
	}
	
	/* Creative mode methods to get around issue where capability data is lost */
	
	public static ItemStack creativeStack(Item item, float quality) {
		ItemStack stack = stackOf(item, quality);
		stack.getOrCreateTag().put("creativeData", getCreativeData(stack));
		return stack;
	}
	
	public static CompoundTag getCreativeData(ItemStack stack) {
		CompoundTag tag = new CompoundTag();
		getDataHandler(stack).ifPresent(h -> {
			tag.putFloat("quality", h.getQuality());
		});
		return tag;
	}
	
	public static void readCreativeData(ItemStack stack, CompoundTag nbt) {
		getDataHandler(stack).ifPresent(h -> {
			h.setQuality(nbt.getFloat("quality"));
		});
	}
	
}
