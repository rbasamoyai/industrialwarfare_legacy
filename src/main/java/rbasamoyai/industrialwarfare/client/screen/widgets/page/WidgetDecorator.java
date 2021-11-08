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

}
