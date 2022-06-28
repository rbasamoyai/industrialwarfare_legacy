package rbasamoyai.industrialwarfare.client.screen.editlabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;

public class LabelNumArgSelector extends ArgSelector<Byte> {

	private static final String TRANSLATION_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".edit_label";
	private static final Component TOOLTIP_HEADER1 = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".tooltip.label_num1").withStyle(ArgSelector.HEADER_STYLE);
	private static final Component TOOLTIP_HEADER2 = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".tooltip.label_num2").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
	private static final Component NO_NUMBER = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".no_num");
	
	private static final List<Component> TOOLTIP = Arrays.asList(
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
		this.selectedArg = Mth.floor(Mth.clamp((double) this.selectedArg + scrollDist * (Screen.hasShiftDown() ? 10.0d : 1.0d), 0.0d, MAX_NUMBER));
	}

	@Override
	public List<Component> getComponentTooltip() {
		return TOOLTIP;
	}

	@Override
	public Component getTitle() {
		return this.selectedArg == 0 ? NO_NUMBER : new TextComponent(Integer.toString(this.selectedArg));
	}

}
