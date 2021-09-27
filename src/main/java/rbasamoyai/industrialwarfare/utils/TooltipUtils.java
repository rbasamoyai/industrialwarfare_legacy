package rbasamoyai.industrialwarfare.utils;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextProcessing;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class TooltipUtils {

	private static final float PERFECT_EPSILION = 0.0001f;
	
	public static final IFormattableTextComponent TOOLTIP_NOT_AVAILABLE = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".not_available");
	
	// Item tooltip styles
	public static final Style FIELD_STYLE = Style.EMPTY.applyFormats(TextFormatting.GOLD);
	public static final Style VALUE_STYLE = Style.EMPTY.applyFormats(TextFormatting.WHITE);
	
	public static IFormattableTextComponent makeItemFieldTooltip(IFormattableTextComponent field, IFormattableTextComponent value) {
		return formatAsStyle(field.copy(), FIELD_STYLE)
				.append(new StringTextComponent(": ").withStyle(FIELD_STYLE))
				.append(value.copy().withStyle(VALUE_STYLE));
	}
	
	public static IFormattableTextComponent getWordedQuality(float f) {
		String rootKey = "tooltip." + IndustrialWarfare.MOD_ID + ".quality.";
		if (f >= 0.0f && f <= 0.2f) return new TranslationTextComponent(rootKey + "very_bad").withStyle(TextFormatting.DARK_RED);
		else if (f >= 0.2f && f < 0.4f) return new TranslationTextComponent(rootKey + "bad").withStyle(TextFormatting.RED);
		else if (f >= 0.4f && f < 0.6f) return new TranslationTextComponent(rootKey + "mediocre").withStyle(TextFormatting.YELLOW);
		else if (f >= 0.6f && f < 0.8f) return new TranslationTextComponent(rootKey + "good").withStyle(TextFormatting.GREEN);
		else if (f >= 0.8f && f <= 1.0f) {
			if (MathHelper.abs(1.0f - f) <= PERFECT_EPSILION) return new TranslationTextComponent(rootKey + "perfect").withStyle(TextFormatting.DARK_GREEN, TextFormatting.ITALIC);
			return new TranslationTextComponent(rootKey + "very_good").withStyle(TextFormatting.DARK_GREEN);
		} else return new TranslationTextComponent(rootKey + "invalid").withStyle(VALUE_STYLE);
	}
	
	public static IFormattableTextComponent formatAsStyle(ITextComponent tc, Style formatStyle) {
		IFormattableTextComponent result = (IFormattableTextComponent) tc;
		result.withStyle(formatStyle);
		result.getSiblings().forEach(s -> formatAsStyle(s, formatStyle));
		return result;
	}
	
	public static int charLength(ITextComponent tc) {
		MutableInt length = new MutableInt(0);
		TextProcessing.iterateFormatted(tc, Style.EMPTY, (index, style, codepoint) -> {
			length.increment();
			return true;
		});
		return length.intValue();
	}
	
	public static String formatFloat(float f) {
		return String.format("%.2f", f * 100.0f);
	}
	
}
