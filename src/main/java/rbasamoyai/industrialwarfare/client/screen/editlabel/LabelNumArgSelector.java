package rbasamoyai.industrialwarfare.client.screen.editlabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;

public class LabelNumArgSelector extends ArgSelector<Byte> {

	private static final String TRANSLATION_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".edit_label";
	private static final IFormattableTextComponent TOOLTIP_HEADER1 = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".tooltip.label_num1").withStyle(ArgSelector.HEADER_STYLE);
	private static final IFormattableTextComponent TOOLTIP_HEADER2 = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".tooltip.label_num2").withStyle(TextFormatting.DARK_GRAY, TextFormatting.ITALIC);
	private static final IFormattableTextComponent NO_NUMBER = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".no_num");
	
	private static final List<ITextComponent> TOOLTIP = Arrays.asList(
			TOOLTIP_HEADER1,
			TOOLTIP_HEADER2
			);
	
	private static final double MAX_NUMBER = (double) Byte.MAX_VALUE;
	
	public LabelNumArgSelector(int selectedArg) {
		super(new ArrayList<>(), selectedArg);
	}
	
	@Override
	public ArgWrapper getSelectedArg() {
		return this.getPossibleArg(0);
	}
	
	@Override
	public ArgWrapper getPossibleArg(int i) {
		return new ArgWrapper(this.selectedArg);
	}
	
	@Override
	public void scrollSelectedArg(double scrollDist) {
		this.selectedArg = MathHelper.floor(MathHelper.clamp((double) this.selectedArg + scrollDist * (Screen.hasShiftDown() ? 10.0d : 1.0d), 0.0d, MAX_NUMBER));
	}

	@Override
	public List<ITextComponent> getComponentTooltip() {
		return TOOLTIP;
	}

	@Override
	public ITextComponent getTitle() {
		return this.selectedArg == 0 ? NO_NUMBER : new StringTextComponent(Integer.toString(this.selectedArg));
	}

}
