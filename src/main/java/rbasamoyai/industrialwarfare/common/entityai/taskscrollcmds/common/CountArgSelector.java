package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;

public class CountArgSelector extends ArgSelector<Integer> {

	private final List<ITextComponent> tooltip;
	private final int min;
	private final int max;
	
	public CountArgSelector(int i, int min, int max, List<ITextComponent> tooltip) {
		super(null, i);
		this.tooltip = tooltip;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return new ArgWrapper(this.selectedArg);
	}

	@Override
	public ArgWrapper getPossibleArg(int i) {
		return new ArgWrapper(i);
	}
	
	@Override
	public void scrollSelectedArg(double scrollDist) {
		this.selectedArg = MathHelper.floor(MathHelper.clamp((double) this.selectedArg + scrollDist * (Screen.hasShiftDown() ? 10.0d : 1.0d), (double) this.min, (double) this.max));
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		return tooltip;
	}

	@Override
	public ITextComponent getTitle() {
		return new StringTextComponent(Integer.toString(this.selectedArg));
	}

}
