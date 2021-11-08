package rbasamoyai.industrialwarfare.utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class TooltipUtils {

	private static final float PERFECT_EPSILION = 0.0001f;
	
	public static final IFormattableTextComponent NOT_AVAILABLE = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".not_available");
	public static final IFormattableTextComponent SHORTENED_TITLE_TERMINATOR = new StringTextComponent("...").withStyle(Style.EMPTY);
	
	// Item tooltip styles
	public static final Style FIELD_STYLE = Style.EMPTY.applyFormats(TextFormatting.GOLD);
	public static final Style VALUE_STYLE = Style.EMPTY.applyFormats(TextFormatting.WHITE);
	
	public static IFormattableTextComponent makeItemFieldTooltip(IFormattableTextComponent field, IFormattableTextComponent value) {
		IFormattableTextComponent valuetc = value.copy();
		if (valuetc.getStyle() == Style.EMPTY) valuetc = valuetc.withStyle(VALUE_STYLE);
		return formatAsStyle(field.copy(), FIELD_STYLE)
				.append(new StringTextComponent(": ").withStyle(FIELD_STYLE))
				.append(valuetc);
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
		result.setStyle(formatStyle);
		result.getSiblings().forEach(s -> formatAsStyle(s, formatStyle));
		return result;
	}
	
	public static int charLength(ITextComponent tc) {
		int len = tc.getContents().length();
		for (ITextComponent sibling : tc.getSiblings()) {
			len += charLength(sibling);
		}
		return len;
	}
	
	public static String formatFloat(float f) {
		return String.format("%.2f", f);
	}
	
	public static IFormattableTextComponent getShortenedTitle(IFormattableTextComponent title, FontRenderer font, int width) {
		ArrayList<IFormattableTextComponent> componentList = new ArrayList<>();
		componentList.add(title);
		// Relies on the fact that all of the base text components implement IFormattableTextComponent, possibly unsafe
		componentList.addAll(title.getSiblings().stream().map(t -> (IFormattableTextComponent) t).collect(Collectors.toList()));
		
		LanguageMap languageMap = LanguageMap.getInstance();
		
		MutableInt w1 = new MutableInt(0);
		IFormattableTextComponent tcFinal = new StringTextComponent("");
		
		for (IFormattableTextComponent tc : componentList) {
			String str = tc instanceof TranslationTextComponent
					? languageMap.getOrDefault(((TranslationTextComponent) tc).getKey())
					: tc.getContents(); // Most should just return "", whereas StringTextComponent returns its plain text.
			
			int w2 = w1.getValue() + font.width(str);
			if (w2 <= width - 1) {
				tcFinal.append(tc);
				w1.setValue(w2);
			} else {
				StringBuilder sBuilder = new StringBuilder();
				str.chars().forEach(c -> {
					StringBuilder newBuilder = new StringBuilder(sBuilder.toString()).appendCodePoint(c);
					if (w1.getValue() + font.width(newBuilder.toString()) <= width - font.width(SHORTENED_TITLE_TERMINATOR))
						sBuilder.appendCodePoint(c);
				});
				tcFinal
						.append(new StringTextComponent(sBuilder.toString()).withStyle(tc.getStyle()))
						.append(SHORTENED_TITLE_TERMINATOR);
				break;
			}
		}
		
		return tcFinal;
	}
	
}
