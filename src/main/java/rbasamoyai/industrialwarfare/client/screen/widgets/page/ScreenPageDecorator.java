package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;

public class ScreenPageDecorator implements IScreenPage {

	private final IScreenPage page;
	
	public ScreenPageDecorator(IScreenPage page) {
		this.page = page;
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		this.page.render(stack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void tick() {
		this.page.tick();
	}
	
	@Override public Screen getScreen() {
		return this.page.getScreen();
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		this.page.mouseMoved(mouseX, mouseY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int action) {
		return this.page.mouseClicked(mouseX, mouseY, action);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int action) {
		return this.page.mouseReleased(mouseX, mouseY, action);
	}
	
	@Override
	public boolean mouseDragged(double mouseX1, double mouseY1, int action, double mouseX2, double mouseY2) {
		return this.page.mouseDragged(mouseX1, mouseY1, action, mouseX2, mouseY2);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDist) {
		return this.page.mouseScrolled(mouseX, mouseY, scrollDist);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifier) {
		return this.page.keyPressed(keyCode, scanCode, modifier);
	}
	
	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifier) {
		return this.page.keyReleased(keyCode, scanCode, modifier);
	}
	
	@Override
	public boolean charTyped(char charTyped, int keyCode) {
		return this.page.charTyped(charTyped, keyCode);
	}
	
	@Override
	public boolean changeFocus(boolean focus) {
		return this.page.changeFocus(focus);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.page.isMouseOver(mouseX, mouseY);
	}

}
