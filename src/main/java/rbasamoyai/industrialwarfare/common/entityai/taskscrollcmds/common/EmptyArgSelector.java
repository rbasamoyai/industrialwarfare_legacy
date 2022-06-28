package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Arrays;
import java.util.List;

import net.minecraft.network.chat.Component;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class EmptyArgSelector extends ArgSelector<Void> {

	private static final List<Component> TOOLTIP = Arrays.asList(TooltipUtils.NOT_AVAILABLE);
	
	protected EmptyArgSelector() {
		super(null);
	}

	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(0);
	}

	@Override
	public ArgWrapper getPossibleArg(int i) {
		return ArgWrapper.EMPTY;
	}
	
	@Override
	public void setSelectedArg(int argIndex) {
	}
	
	@Override
	public void scrollSelectedArg(double scrollDist) {
	}

	@Override
	public List<Component> getComponentTooltip() {
		return TOOLTIP;
	}

	@Override
	public Component getTitle() {
		return TooltipUtils.NOT_AVAILABLE;
	}

	
	
}
