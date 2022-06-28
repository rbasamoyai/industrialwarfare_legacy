package rbasamoyai.industrialwarfare.utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

public class TooltipUtils {

	private static final float PERFECT_EPSILION = 0.0001f;
	
	public static final MutableComponent NOT_AVAILABLE = new TranslatableComponent("tooltip." + IndustrialWarfare.MOD_ID + ".not_available");
	public static final MutableComponent SHORTENED_TITLE_TERMINATOR = new TextComponent("...").withStyle(Style.EMPTY);
	
	// Item tooltip styles
	public static final Style FIELD_STYLE = Style.EMPTY.applyFormats(ChatFormatting.GOLD);
	public static final Style VALUE_STYLE = Style.EMPTY.applyFormats(ChatFormatting.WHITE);
	
	public static MutableComponent makeItemFieldTooltip(MutableComponent field, MutableComponent value) {
		MutableComponent valuetc = value.copy();
		if (valuetc.getStyle() == Style.EMPTY) valuetc = valuetc.withStyle(VALUE_STYLE);
		return formatAsStyle(field.copy(), FIELD_STYLE)
				.append(new TextComponent(": ").withStyle(FIELD_STYLE))
				.append(valuetc);
	}
	
	public static MutableComponent getWordedQuality(float f) {
		String rootKey = "tooltip." + IndustrialWarfare.MOD_ID + ".quality.";
		if (f >= 0.0f && f <= 0.2f) return new TranslatableComponent(rootKey + "very_bad").withStyle(ChatFormatting.DARK_RED);
		else if (f >= 0.2f && f < 0.4f) return new TranslatableComponent(rootKey + "bad").withStyle(ChatFormatting.RED);
		else if (f >= 0.4f && f < 0.6f) return new TranslatableComponent(rootKey + "mediocre").withStyle(ChatFormatting.YELLOW);
		else if (f >= 0.6f && f < 0.8f) return new TranslatableComponent(rootKey + "good").withStyle(ChatFormatting.GREEN);
		else if (f >= 0.8f && f <= 1.0f) {
			if (Math.abs(1.0f - f) <= PERFECT_EPSILION) return new TranslatableComponent(rootKey + "perfect").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC);
			return new TranslatableComponent(rootKey + "very_good").withStyle(ChatFormatting.DARK_GREEN);
		} else return new TranslatableComponent(rootKey + "invalid").withStyle(VALUE_STYLE);
	}
	
	public static MutableComponent formatAsStyle(Component tc, Style formatStyle) {
		MutableComponent result = (MutableComponent) tc;
		result.setStyle(formatStyle);
		result.getSiblings().forEach(s -> formatAsStyle(s, formatStyle));
		return result;
	}
	
	public static int charLength(Component tc) {
		int len = tc.getContents().length();
		for (Component sibling : tc.getSiblings()) {
			len += charLength(sibling);
		}
		return len;
	}
	
	public static String formatFloat(float f) {
		return String.format("%.2f", f);
	}
	
	public static MutableComponent getShortenedTitle(MutableComponent title, Font font, int width) {
		ArrayList<MutableComponent> componentList = new ArrayList<>();
		componentList.add(title);
		// Relies on the fact that all of the base text components implement MutableComponent, possibly unsafe
		componentList.addAll(title.getSiblings().stream().map(t -> (MutableComponent) t).collect(Collectors.toList()));
		
		Language languageMap = Language.getInstance();
		
		MutableInt w1 = new MutableInt(0);
		MutableComponent tcFinal = new TextComponent("");
		
		for (MutableComponent tc : componentList) {
			String str = tc instanceof TranslatableComponent
					? languageMap.getOrDefault(((TranslatableComponent) tc).getKey())
					: tc.getContents(); // Most should just return "", whereas TextComponent returns its plain text.
			
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
						.append(new TextComponent(sBuilder.toString()).withStyle(tc.getStyle()))
						.append(SHORTENED_TITLE_TERMINATOR);
				break;
			}
		}
		
		return tcFinal;
	}
	
}
