package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;

public class ItemCountArgSelector extends ArgSelector<Byte> {
	
	private static final List<ITextComponent> TOOLTIP = Arrays.asList(new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".task_scroll.tooltip.selector.item_count").withStyle(ArgSelector.HEADER_STYLE));
	private static final int MAX_ITEM_COUNT = 64;
	
	public ItemCountArgSelector(int count) {
		super(null, count);
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(0);
	}
	
	@Override
	public ArgWrapper getPossibleArg(int count) {
		return new ArgWrapper(this.selectedArg);
	}
	
	@Override
	public void setSelectedArg(int count) {
		this.selectedArg = count;
	}
	
	@Override
	public void scrollSelectedArg(double scrollDist) {
		this.selectedArg = MathHelper.floor(MathHelper.clamp((double) this.selectedArg + scrollDist * (Screen.hasShiftDown() ? 10.0d : 1.0d), 0.0d, (double) MAX_ITEM_COUNT));
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		return TOOLTIP;
	}

	@Override
	public ITextComponent getTitle() {
		return new StringTextComponent(this.selectedArg == 0 ? "*" : String.valueOf(this.selectedArg));
	}

}
