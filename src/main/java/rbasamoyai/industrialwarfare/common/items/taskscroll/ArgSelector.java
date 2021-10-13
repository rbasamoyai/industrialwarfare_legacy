package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.List;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

/**
 * Selector abstract class for ArgSelectorWidget.
 * 
 * @author rbasamoyai
 */

public abstract class ArgSelector<T> {
	
	public static final Style HEADER_STYLE = Style.EMPTY.applyFormats(TextFormatting.ITALIC);
	public static final Style NOT_SELECTED_STYLE = Style.EMPTY.applyFormats(TextFormatting.DARK_GRAY);
	public static final Style CANNOT_SELECT_STYLE = Style.EMPTY.applyFormats(TextFormatting.RED, TextFormatting.ITALIC);
	public static final Style SELECTED_STYLE = Style.EMPTY.applyFormats(TextFormatting.GOLD);
	
	private static final StringTextComponent SELECTION_ARROW = new StringTextComponent("\u2192 ");
	protected static final IFormattableTextComponent SPACER = new StringTextComponent(" ").withStyle(NOT_SELECTED_STYLE);
	
	protected final List<T> possibleArgs;
	protected int selectedArg;

	protected ArgSelector(List<T> possibleArgs, int selectedArg) {
		this.possibleArgs = possibleArgs;
		this.selectedArg = selectedArg;
	}
	
	protected ArgSelector(List<T> possibleArgs) {
		this(possibleArgs, 0);
	}
	
	public void setSelectedArg(int argIndex) {
		this.selectedArg = argIndex;
	}
	
	public void scrollSelectedArg(double scrollDist) {
		this.selectedArg = MathHelper.floor(MathHelper.clamp((double) this.selectedArg - scrollDist, 0.0d, (double)(this.possibleArgs.size() - 1)));
	}
	
	public abstract ArgWrapper getSelectedArg();
	
	public abstract ArgWrapper getPossibleArg(int i);
	
	public abstract List<ITextComponent> getComponentTooltip();
	
	public abstract ITextComponent getTitle();
	
	protected void warnInvalidSelection() {
		IndustrialWarfare.LOGGER.warn("Invalid argument selection setting " + this.selectedArg + ", cannot properly display the tooltip! Setting to argIndex 0...");
		this.selectedArg = 0;
	}
	
	public static IFormattableTextComponent formatAsSelected(IFormattableTextComponent tc) {
		return TooltipUtils.formatAsStyle(SELECTION_ARROW.copy().append(tc.copy()), SELECTED_STYLE);
	}
	
}
