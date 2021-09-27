package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.selectors.ArgSelector;
import rbasamoyai.industrialwarfare.utils.ArgUtils;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class StorageSideAccessArgSelector extends ArgSelector<Byte> {

	private static final String TOOLTIP_TRANSLATION_KEY = "gui." + IndustrialWarfare.MOD_ID + ".task_scroll.tooltip.selector.storage_side";
	private static final IFormattableTextComponent TOOLTIP_HEADER = new TranslationTextComponent(TOOLTIP_TRANSLATION_KEY).withStyle(ArgSelector.HEADER_STYLE);
	
	public StorageSideAccessArgSelector(int index) {
		super(Arrays.asList((byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5), index);
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		tooltip.add(TOOLTIP_HEADER.copy());
		
		if (this.selectedArg < 0 || this.selectedArg >= this.possibleArgs.size()) this.warnInvalidSelection();
		
		for (byte b : this.possibleArgs) {
			IFormattableTextComponent tc = (IFormattableTextComponent) getTextComponentFromValue(b);
			if (b == this.selectedArg) tc = ArgSelector.formatAsSelected(tc);
			else tc = TooltipUtils.formatAsStyle(tc, ArgSelector.NOT_SELECTED_STYLE);
			tooltip.add(tc);
		}
		
		return tooltip;
	}

	@Override
	public ITextComponent getTitle() {
		return getTextComponentFromValue(this.selectedArg);
	}
	
	private static ITextComponent getTextComponentFromValue(int i) {
		return new TranslationTextComponent(TOOLTIP_TRANSLATION_KEY + "." + ArgUtils.getDirection(i).getName());
	}

}
