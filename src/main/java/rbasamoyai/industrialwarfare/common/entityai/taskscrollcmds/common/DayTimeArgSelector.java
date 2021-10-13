package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;

public class DayTimeArgSelector extends ArgSelector<Integer> {
	
	private static final String TRANSLATION_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".task_scroll";
	private static final List<ITextComponent> TOOLTIP = Arrays.asList(new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".tooltip.selector.day_time").withStyle(ArgSelector.HEADER_STYLE));
	private static final String TITLE_KEY = TRANSLATION_KEY_ROOT + ".selector.day_time.time_format";
	
	private static final double MAX_SECONDS = 1200.0d;
	
	public DayTimeArgSelector(int time) {
		super(null, time);
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(this.selectedArg);
	}

	@Override
	public ArgWrapper getPossibleArg(int i) {
		return new ArgWrapper(i);
	}
	
	@Override
	public void scrollSelectedArg(double scrollDist) {
		this.selectedArg = MathHelper.floor(MathHelper.clamp((double) this.selectedArg + scrollDist * (Screen.hasShiftDown() ? 60.0d : 1.0d) , 0.0d, MAX_SECONDS));
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		return TOOLTIP;
	}

	@Override
	public ITextComponent getTitle() {
		String minute = String.format("%02d", this.selectedArg / 60);
		String second = String.format("%02d", this.selectedArg % 60);
		return new TranslationTextComponent(TITLE_KEY, minute, second);
	}

}
