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
		if (nbt == null) this.defaultNBT(nbt);
		provider.deserializeNBT(nbt == null ? this.defaultNBT(new CompoundNBT()) : nbt);
		return provider;
	}
	
	public CompoundNBT defaultNBT(CompoundNBT nbt) {
		nbt.putFloat(QualityItemDataCapability.TAG_QUALITY, 1.0f);
		return nbt;
	}
	
	public static LazyOptional<? extends IQualityItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(QualityItemDataCapability.QUALITY_ITEM_DATA_CAPABILITY);
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
}
