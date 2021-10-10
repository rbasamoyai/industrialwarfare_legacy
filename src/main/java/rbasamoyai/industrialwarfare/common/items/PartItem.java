package rbasamoyai.industrialwarfare.common.items;

import java.util.List;

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
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.IPartItemDataHandler;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemDataCapability;
import rbasamoyai.industrialwarfare.common.capabilities.itemstacks.partitem.PartItemDataProvider;
import rbasamoyai.industrialwarfare.core.itemgroup.IWItemGroups;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class PartItem extends QualityItem {
	
	private static final IFormattableTextComponent TOOLTIP_PART_COUNT = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".part.part_count");
	private static final IFormattableTextComponent TOOLTIP_WEIGHT = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".part.weight");
	
	public PartItem(String id) {
		super(new Item.Properties().tab(IWItemGroups.TAB_PARTS), "part_" + id);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		PartItemDataProvider provider = new PartItemDataProvider();
		provider.deserializeNBT(nbt == null ? this.defaultNBT(new CompoundNBT()) : nbt);
		return provider;
	}
	
	@Override
	public CompoundNBT defaultNBT(CompoundNBT nbt) {
		super.defaultNBT(nbt);
		nbt.putFloat(PartItemDataCapability.TAG_PART_COUNT, 1);
		nbt.putFloat(PartItemDataCapability.TAG_WEIGHT, 1);
		return nbt;
	}
	
	public static LazyOptional<IPartItemDataHandler> getDataHandler(ItemStack stack) {
		return stack.getCapability(PartItemDataCapability.PART_ITEM_DATA_CAPABILITY);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		super.appendHoverText(stack, world, tooltip, flag);
		LazyOptional<IPartItemDataHandler> optional = getDataHandler(stack);
		
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_PART_COUNT,
				optional
						.map(h -> (IFormattableTextComponent) new StringTextComponent(Integer.toString(h.getPartCount())))
						.orElse(TooltipUtils.NOT_AVAILABLE))
		);
		tooltip.add(TooltipUtils.makeItemFieldTooltip(TOOLTIP_WEIGHT, 
				optional
						.map(h -> (IFormattableTextComponent) new StringTextComponent(TooltipUtils.formatFloat(h.getWeight())))
						.orElse(TooltipUtils.NOT_AVAILABLE))
		);
	}

	public static ItemStack setQualityValues(ItemStack stack, float quality, int partCount, float weight) {
		getDataHandler(stack).ifPresent(h -> {
			QualityItem.setQualityValues(stack, quality);
			h.setPartCount(partCount);
			h.setWeight(weight);
		});
		return stack;
	}
}
