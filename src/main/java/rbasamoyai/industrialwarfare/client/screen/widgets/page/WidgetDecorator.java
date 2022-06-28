package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetDecorator extends AbstractWidgetDecorator {

	private final AbstractWidget widget;
	
	public WidgetDecorator(IScreenPage page, AbstractWidget widget) {
		super(page);
		this.widget = widget;
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
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
