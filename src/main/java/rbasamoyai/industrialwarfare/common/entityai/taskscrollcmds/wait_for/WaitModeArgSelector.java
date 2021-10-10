package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.wait_for;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class WaitModeArgSelector extends ArgSelector<Byte> {

	private static final String TITLE_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".task_scroll.selector.wait_mode";
	private static final String TOOLTIP_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".task_scroll.tooltip.selector.wait_mode";
	
	private static final IFormattableTextComponent TITLE_DAY_TIME = new TranslationTextComponent(TITLE_KEY_ROOT + ".day_time");
	private static final IFormattableTextComponent TITLE_RELATIVE_TIME = new TranslationTextComponent(TITLE_KEY_ROOT + ".relative_time");
	private static final IFormattableTextComponent TITLE_BELL = new TranslationTextComponent(TITLE_KEY_ROOT + ".bell");
	
	private static final IFormattableTextComponent TOOLTIP_HEADER = new TranslationTextComponent(TOOLTIP_KEY_ROOT).withStyle(HEADER_STYLE);
	private static final IFormattableTextComponent TOOLTIP_DAY_TIME = new TranslationTextComponent(TOOLTIP_KEY_ROOT + ".day_time");
	private static final IFormattableTextComponent TOOLTIP_RELATIVE_TIME = new TranslationTextComponent(TOOLTIP_KEY_ROOT + ".relative_time");
	private static final IFormattableTextComponent TOOLTIP_BELL = new TranslationTextComponent(TOOLTIP_KEY_ROOT + ".bell");
	
	private static final int LAST_MODE = 2;
	
	public WaitModeArgSelector(int mode) {
		super(null, mode);
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(0);
	}
	
	@Override
	public ArgWrapper getPossibleArg(int argIndex) {
		return new ArgWrapper(this.selectedArg);
	}
	
	@Override
	public void setSelectedArg(int mode) {
		this.selectedArg = mode;
	}
	
	@Override
	public void scrollSelectedArg(double scrollDist) {
		this.selectedArg = MathHelper.floor(MathHelper.clamp((double) this.selectedArg - scrollDist, 0.0d, (double) LAST_MODE));
	}
	
	@Override
	public List<ITextComponent> getComponentTooltip() {
		List<ITextComponent> tooltip = new ArrayList<>();
		tooltip.add(TOOLTIP_HEADER.copy().withStyle(ArgSelector.HEADER_STYLE));
		tooltip.add(TOOLTIP_DAY_TIME.copy());
		tooltip.add(TOOLTIP_RELATIVE_TIME.copy());
		tooltip.add(TOOLTIP_BELL.copy());
		
		for (int i = 1; i < tooltip.size(); i++) {
			if (i == this.selectedArg + 1) {
				tooltip.set(i, ArgSelector.formatAsSelected((IFormattableTextComponent) tooltip.get(i)));
			} else {
				TooltipUtils.formatAsStyle(tooltip.get(i), ArgSelector.NOT_SELECTED_STYLE);
			}
		}
		
		return tooltip;
	}

	@Override
	public ITextComponent getTitle() {
		switch (this.selectedArg) {
		case 0: return TITLE_DAY_TIME;
		case 1: return TITLE_RELATIVE_TIME;
		case 2: return TITLE_BELL;
		default: return TooltipUtils.NOT_AVAILABLE;
		}
	}

}
