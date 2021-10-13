package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WordedArgSelector.ArgGroup;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class WordedArgSelector extends ArgSelector<ArgGroup> {

	private final ITextComponent tooltipHeader;
	
	public WordedArgSelector(List<ArgGroup> args, int arg, ITextComponent tooltipHeader) {
		super(args, 0);
		this.tooltipHeader = tooltipHeader;
		for (int i = 0; i < this.possibleArgs.size(); i++) {
			if (arg == this.possibleArgs.get(i).getArg()) this.selectedArg = i;
		}
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(this.selectedArg);
	}

	@Override
	public ArgWrapper getPossibleArg(int i) {
		return new ArgWrapper(this.possibleArgs.get(i).getArg());
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		List<ITextComponent> tooltip = new ArrayList<>();
		tooltip.add(this.tooltipHeader);
		for (int i = 0; i < this.possibleArgs.size(); i++) {
			IFormattableTextComponent tc = this.possibleArgs.get(i).getTooltip().copy();
			if (i == this.selectedArg) {
				tc = ArgSelector.formatAsSelected(tc);
			} else {
				tc = TooltipUtils.formatAsStyle(tc, ArgSelector.NOT_SELECTED_STYLE);
			}
			tooltip.add(tc);
		}
		return tooltip;
	}

	@Override
	public ITextComponent getTitle() {
		return this.possibleArgs.get(this.selectedArg).getTitle();
	}

	public static class ArgGroup {
		private final int arg;
		private final ITextComponent tooltip;
		private final ITextComponent title;
		
		public ArgGroup(int arg, ITextComponent tooltip, ITextComponent title) {
			this.arg = arg;
			this.tooltip = tooltip;
			this.title = title;
		}
		
		public int getArg() { return this.arg; }
		public ITextComponent getTooltip() { return this.tooltip; }
		public ITextComponent getTitle() { return this.title; }
	}
	
}
