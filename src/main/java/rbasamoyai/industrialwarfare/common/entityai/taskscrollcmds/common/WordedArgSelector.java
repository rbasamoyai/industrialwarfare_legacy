package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WordedArgSelector.ArgGroup;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class WordedArgSelector extends ArgSelector<ArgGroup> {

	private final Component tooltipHeader;
	
	public WordedArgSelector(List<ArgGroup> args, int arg, Component tooltipHeader) {
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
	public List<Component> getComponentTooltip() {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(this.tooltipHeader);
		for (int i = 0; i < this.possibleArgs.size(); i++) {
			MutableComponent tc = this.possibleArgs.get(i).getTooltip().copy();
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
	public Component getTitle() {
		return this.possibleArgs.get(this.selectedArg).getTitle();
	}

	public static class ArgGroup {
		private final int arg;
		private final Component tooltip;
		private final Component title;
		
		public ArgGroup(int arg, Component tooltip, Component title) {
			this.arg = arg;
			this.tooltip = tooltip;
			this.title = title;
		}
		
		public int getArg() { return this.arg; }
		public Component getTooltip() { return this.tooltip; }
		public Component getTitle() { return this.title; }
	}
	
}
