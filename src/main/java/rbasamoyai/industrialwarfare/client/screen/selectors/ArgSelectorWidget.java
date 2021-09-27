package rbasamoyai.industrialwarfare.client.screen.selectors;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.utils.WidgetUtils;

public class ArgSelectorWidget extends Widget {

	private static final ResourceLocation IW_WIDGETS_LOCATION = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/widgets.png");
	
	private static final IFormattableTextComponent SHORTENED_TITLE_TERMINATOR = new StringTextComponent("...").withStyle(Style.EMPTY);
	// public for TaskScrollScreen#renderTooltip
	public static final IFormattableTextComponent NOT_AVAILABLE = new TranslationTextComponent("tooltip." + IndustrialWarfare.MOD_ID + ".not_available");
	
	private static final int BORDER_SIZE = 4;
	private static final int TEXTURE_HEIGHT = 18;
	
	// The given values point to the top left corner of each corner
	private static final int TEX_TOP_LEFT_X = 0;
	private static final int TEX_TOP_LEFT_Y = 0;
	private static final int TEX_FIELD_X = TEX_TOP_LEFT_X + BORDER_SIZE;
	private static final int TEX_WIDTH = 200;
	private static final int TEX_FIELD_WIDTH = TEX_WIDTH - BORDER_SIZE * 2;
	private static final int TEX_RIGHT_BORDER_X = 196;
	
	private static final int TEXT_COLOR = 0x00FFFFFF;
	
	// Instance constants
	private final int rightBorderX;
	private final int fieldWidth;
	private final int fieldTopLeftX;
	
	protected FontRenderer font;
	
	protected IFormattableTextComponent shortenedTitle;
	
	protected Optional<ArgSelector<?>> selector;
	
	public ArgSelectorWidget(Minecraft minecraft, int x, int y, int width, Optional<ArgSelector<?>> initialSelector) {
		super(x, y, width, TEXTURE_HEIGHT, StringTextComponent.EMPTY); // TODO: Add message stuff
		
		this.font = minecraft.font;
		
		this.rightBorderX = this.x + this.width - BORDER_SIZE;
		this.fieldWidth = this.width - BORDER_SIZE * 2;
		this.fieldTopLeftX = this.x + BORDER_SIZE;
		
		this.selector = initialSelector;
		
		// Possibly unsafe if someone adds a text component interface that directly extends ITextComponent and not IFormattableTextComponent,
		// but all of the base text components implement IFormattableTextComponent.
		this.shortenedTitle = this.getShortenedTitle((IFormattableTextComponent) this.selector.map(s -> s.getTitle()).orElse(NOT_AVAILABLE.copy()));
		
		WidgetUtils.setActiveAndVisible(this, this.selector.isPresent());
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDist) {
		if (!this.isMouseOver(mouseX, mouseY)) return false;
		this.selector.ifPresent(as -> {
			as.scrollSelectedArg(scrollDist);
			this.shortenedTitle = this.getShortenedTitle((IFormattableTextComponent) as.getTitle());
		});
		return true;
	}
	
	@Override
	public void renderButton(MatrixStack stack, int x, int y, float partialTicks) {
		Minecraft.getInstance().getTextureManager().bind(IW_WIDGETS_LOCATION);
		
		this.blit(stack, this.x, this.y, TEX_TOP_LEFT_X, TEX_TOP_LEFT_Y, BORDER_SIZE, TEXTURE_HEIGHT);
		this.blit(stack, this.rightBorderX, this.y, TEX_RIGHT_BORDER_X, TEX_TOP_LEFT_Y, BORDER_SIZE, TEXTURE_HEIGHT);
		
		int tileCount = MathHelper.ceil((float) this.fieldWidth / (float) TEX_FIELD_WIDTH);
		for (int i = 0; i < tileCount; i++) { // This should only loop once - why the hell would you need a long field?
			int x1 = this.fieldTopLeftX + TEX_FIELD_WIDTH * i;
			int texWidth = MathHelper.clamp(this.fieldWidth - TEX_FIELD_WIDTH * i, 0, TEX_FIELD_WIDTH);
			this.blit(stack, x1, this.y, TEX_FIELD_X, TEX_TOP_LEFT_Y, texWidth, TEXTURE_HEIGHT);
		}
		
		drawString(stack, this.font, this.shortenedTitle, this.x + BORDER_SIZE + 1, this.y + BORDER_SIZE + 1, TEXT_COLOR);
	}
	
	protected IFormattableTextComponent getShortenedTitle(IFormattableTextComponent title) {
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
			
			int w2 = w1.getValue() + this.font.width(str);
			if (w2 <= this.fieldWidth - 1) {
				tcFinal.append(tc);
				w1.setValue(w2);
			} else {
				StringBuilder sBuilder = new StringBuilder();
				str.chars().forEach(c -> {
					StringBuilder newBuilder = new StringBuilder(sBuilder.toString()).appendCodePoint(c);
					if (w1.getValue() + this.font.width(newBuilder.toString()) <= this.fieldWidth - this.font.width(SHORTENED_TITLE_TERMINATOR))
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
	
	public Optional<ArgSelector<?>> getSelectorOptional() {
		return this.selector;
	}
	
	@Override
	public void playDownSound(SoundHandler handler) {
		
	}

}
