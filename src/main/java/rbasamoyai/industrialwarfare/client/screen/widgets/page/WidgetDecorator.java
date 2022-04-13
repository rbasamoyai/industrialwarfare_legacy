package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;

public class WidgetDecorator extends AbstractWidgetDecorator {

	private final Widget widget;
	
	public WidgetDecorator(IScreenPage page, Widget widget) {
		super(page);
		this.widget = widget;
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		this.widget.render(stack, mouseX, mouseY, partialTicks);
	}
	
	@Override public void setActive(boolean active) { this.widget.active = active; }
	@Override public void setVisible(boolean visible) { this.widget.visible = visible; }
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int action) {
		return this.widget.mouseClicked(mouseX, mouseY, action) || super.mouseClicked(mouseX, mouseY, action);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int action) {
		return this.widget.mouseReleased(mouseX, mouseY, action) || super.mouseReleased(mouseX, mouseY, action);
	}
	
	@Override
	public boolean mouseDragged(double mouseX1, double mouseY1, int action, double mouseX2, double mouseY2) {
		return this.widget.mouseDragged(mouseX1, mouseY1, action, mouseX2, mouseY2) || super.mouseDragged(mouseX1, mouseY1, action, mouseX2, mouseY2);
	}

}
