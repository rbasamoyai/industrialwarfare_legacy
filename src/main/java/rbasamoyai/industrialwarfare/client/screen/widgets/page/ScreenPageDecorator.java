package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;

public class ScreenPageDecorator implements IScreenPage {

	private final IScreenPage page;
	
	public ScreenPageDecorator(IScreenPage page) {
		this.page = page;
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.page.render(stack, mouseX, mouseY, partialTicks);
	}
	
	@Override public Screen getScreen() { return this.page.getScreen(); }

}
