package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.wait_for;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;

public class WaitTimeArgSelector extends ArgSelector<Byte> {

	private static final String TRANSLATION_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".task_scroll";
	private static final List<ITextComponent> TOOLTIP = Arrays.asList(new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".tooltip.selector.wait_time").withStyle(ArgSelector.HEADER_STYLE));
	private static final String TITLE_KEY = TRANSLATION_KEY_ROOT + ".selector.wait_time.time_format";
	private static final int MAX_TIME = 60; // In seconds
	
	public WaitTimeArgSelector(int time) {
		super(null, time);
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
	public void setSelectedArg(int time) {
		this.selectedArg = time;
	}
	
	@Override
	public void scrollSelectedArg(double scrollDist) {
		this.selectedArg = MathHelper.floor(MathHelper.clamp((double) this.selectedArg - scrollDist, 0.0d, (double) MAX_TIME));
	}
	
	@Override
	public List<ITextComponent> getComponentTooltip() {
		return TOOLTIP;
	}

	@Override
	public ITextComponent getTitle() {
		return new TranslationTextComponent(TITLE_KEY, this.selectedArg);
	}

}
