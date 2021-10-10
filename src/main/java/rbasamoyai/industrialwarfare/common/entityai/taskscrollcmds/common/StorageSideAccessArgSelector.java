package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.Direction;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class StorageSideAccessArgSelector extends ArgSelector<Direction> {

	private static final String TOOLTIP_TRANSLATION_KEY = "gui." + IndustrialWarfare.MOD_ID + ".task_scroll.tooltip.selector.storage_side";
	private static final IFormattableTextComponent TOOLTIP_HEADER = new TranslationTextComponent(TOOLTIP_TRANSLATION_KEY).withStyle(ArgSelector.HEADER_STYLE);
	
	public StorageSideAccessArgSelector(int index) {
		super(Arrays.asList(Direction.values()), index);
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(this.selectedArg);
	}
	
	@Override
	public ArgWrapper getPossibleArg(int argIndex) {
		return new ArgWrapper(this.selectedArg);
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		tooltip.add(TOOLTIP_HEADER.copy());
		
		if (this.selectedArg < 0 || this.selectedArg >= this.possibleArgs.size()) this.warnInvalidSelection();
		
		this.possibleArgs.forEach(dir -> {
			IFormattableTextComponent tc = getTooltip(dir);
			if (dir == Direction.from3DDataValue(this.selectedArg)) tc = ArgSelector.formatAsSelected(tc);
			else tc = TooltipUtils.formatAsStyle(tc, ArgSelector.NOT_SELECTED_STYLE);
			tooltip.add(tc);
		});
		
		return tooltip;
	}

	@Override
	public ITextComponent getTitle() {
		return getTooltip(Direction.from3DDataValue(this.selectedArg));
	}
	
	private static IFormattableTextComponent getTooltip(Direction dir) {
		return new TranslationTextComponent(TOOLTIP_TRANSLATION_KEY + "." + dir.getName());
	}

}
