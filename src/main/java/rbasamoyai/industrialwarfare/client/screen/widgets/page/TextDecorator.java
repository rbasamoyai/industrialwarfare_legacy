package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class TextDecorator extends ScreenPageDecorator {

	private final float x;
	private final float y;
	private final boolean centered;
	private final boolean shadow;
	private final int textColor;
	
	private Component text;
	
	public TextDecorator(IScreenPage page, Component text, float x, float y, boolean centered, boolean shadow, int textColor) {
		super(page);
		
		this.text = text;
		this.x = x;
		this.y = y;
		this.centered = centered;
		this.shadow = shadow;
		this.textColor = textColor;
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		float x = this.centered ? this.x - (float) font.width(this.text) * 0.5f : this.x;
		if (this.shadow) {
			font.drawShadow(stack, this.text, x, this.y, this.textColor);
		} else {
			font.draw(stack, this.text, x, this.y, this.textColor);
		}
	}
	
	public void setText(Component text) { this.text = text; }
	
}
