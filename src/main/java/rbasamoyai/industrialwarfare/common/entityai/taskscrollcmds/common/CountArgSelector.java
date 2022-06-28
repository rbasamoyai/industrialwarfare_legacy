package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;

public class CountArgSelector extends ArgSelector<Integer> {

	private final List<Component> tooltip;
	private final int min;
	private final int max;
	private final double shiftModifier;
	
	public CountArgSelector(int i, int min, int max, int shiftModifier, List<Component> tooltip) {
		super(null, i);
		this.tooltip = tooltip;
		this.min = min;
		this.max = max;
		this.shiftModifier = (double) shiftModifier;
	}
	
	public CountArgSelector(int i, int min, int max, List<Component> tooltip) {
		this(i, min, max, 10, tooltip);
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
		this.selectedArg = Mth.floor(Mth.clamp((double) this.selectedArg + scrollDist * (Screen.hasShiftDown() ? this.shiftModifier : 1.0d), (double) this.min, (double) this.max));
	}

	@Override
	public List<Component> getComponentTooltip() {
		return tooltip;
	}

	@Override
	public Component getTitle() {
		return new TextComponent(Integer.toString(this.selectedArg));
	}

}
