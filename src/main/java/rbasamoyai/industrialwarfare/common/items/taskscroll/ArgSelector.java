package rbasamoyai.industrialwarfare.common.items.taskscroll;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

/**
 * Selector abstract class for ArgSelectorWidget.
 * 
 * @author rbasamoyai
 */

public abstract class ArgSelector<T> {
	
	public static final Style HEADER_STYLE = Style.EMPTY.applyFormats(ChatFormatting.ITALIC);
	public static final Style NOT_SELECTED_STYLE = Style.EMPTY.applyFormats(ChatFormatting.DARK_GRAY);
	public static final Style CANNOT_SELECT_STYLE = Style.EMPTY.applyFormats(ChatFormatting.RED, ChatFormatting.ITALIC);
	public static final Style SELECTED_STYLE = Style.EMPTY.applyFormats(ChatFormatting.GOLD);
	
	private static final TextComponent SELECTION_ARROW = new TextComponent("\u2192 ");
	protected static final MutableComponent SPACER = new TextComponent(" ").withStyle(NOT_SELECTED_STYLE);
	
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
		this.selectedArg = Mth.floor(Mth.clamp((double) this.selectedArg - scrollDist, 0.0d, (double)(this.possibleArgs.size() - 1)));
	}
	
	public abstract ArgWrapper getSelectedArg();
	
	public abstract ArgWrapper getPossibleArg(int i);
	
	public abstract List<Component> getComponentTooltip();
	
	public abstract Component getTitle();
	
	protected void warnInvalidSelection() {
		IndustrialWarfare.LOGGER.warn("Invalid argument selection setting " + this.selectedArg + ", cannot properly display the tooltip! Setting to argIndex 0...");
		this.selectedArg = 0;
	}
	
	public static MutableComponent formatAsSelected(MutableComponent tc) {
		return TooltipUtils.formatAsStyle(SELECTION_ARROW.copy().append(tc.copy()), SELECTED_STYLE);
	}
	
}
