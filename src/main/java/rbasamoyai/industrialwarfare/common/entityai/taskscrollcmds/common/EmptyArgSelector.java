package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.text.ITextComponent;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class EmptyArgSelector extends ArgSelector<Void> {

	private static final List<ITextComponent> TOOLTIP = Arrays.asList(TooltipUtils.NOT_AVAILABLE);
	
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
	public List<ITextComponent> getComponentTooltip() {
		return TOOLTIP;
	}

	@Override
	public ITextComponent getTitle() {
		return TooltipUtils.NOT_AVAILABLE;
	}

	
	
}
