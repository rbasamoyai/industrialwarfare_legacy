package rbasamoyai.industrialwarfare.common.items;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.IQualityItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.qualityitem.QualityItemDataProvider;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class QualityItem extends Item {
	
	private static final IFormattableTextComponent TOOLTIP_QUALITY = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".quality");
	
	public QualityItem(Item.Properties properties) {
		super(properties);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		QualityItemDataProvider provider = new QualityItemDataProvider();
		CompoundNBT tag = nbt;
		if (nbt == null) tag = defaultNBT(nbt);
		else if (nbt.contains("Parent")) tag = nbt.getCompound("Parent");
		provider.deserializeNBT(tag);
		return provider;
	}
	
	public static CompoundNBT defaultNBT(CompoundNBT nbt) {
		nbt.putFloat(QualityItemDataCapability.TAG_QUALITY, 1.0f);
		return nbt;
	}
	
	public static LazyOptional<? extends IQualityItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(QualityItemDataCapability.QUALITY_ITEM_DATA_CAPABILITY);
	}
	
	@Override
	public CompoundNBT getShareTag(ItemStack stack) {
		CompoundNBT tag = stack.getOrCreateTag();
		getDataHandler(stack).ifPresent(h -> {
			tag.put("item_cap", QualityItemDataCapability.QUALITY_ITEM_DATA_CAPABILITY.writeNBT(h, null));
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
			QualityItemDataCapability.QUALITY_ITEM_DATA_CAPABILITY.readNBT(h, null, nbt.getCompound("item_cap"));
		});
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_QUALITY,
				getDataHandler(stack)
						.map(h -> Screen.hasShiftDown()
								? new StringTextComponent(TooltipUtils.formatFloat(h.getQuality() * 100.0f).concat("%"))
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
	
	protected static CompoundNBT getCreativeData(ItemStack stack) {
		CompoundNBT tag = new CompoundNBT();
		getDataHandler(stack).ifPresent(h -> {
			tag.putFloat(QualityItemDataCapability.TAG_QUALITY, h.getQuality());
		});
		return tag;
	}
	
	protected static void readCreativeData(ItemStack stack, CompoundNBT nbt) {
		getDataHandler(stack).ifPresent(h -> {
			h.setQuality(nbt.getFloat(QualityItemDataCapability.TAG_QUALITY));
		});
	}
	
}
