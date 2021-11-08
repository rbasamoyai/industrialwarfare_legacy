package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;

/**
 * Useful for applying IWidgetDecorator operations on multiple widgets. For a
 * single widget decorator, see {@link WidgetDecorator}. 
 * 
 * @author rbasamoyai
 */

public class WidgetCollectionDecorator extends AbstractWidgetDecorator {

	private final Widget[] widgets;
	
	public WidgetCollectionDecorator(IScreenPage page, Widget[] widgets) {
		super(page);
		this.widgets = widgets;
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		this.apply(w -> w.render(stack, mouseX, mouseY, partialTicks));
	}
	
	@Override
	public void setActive(boolean active) {
		this.apply(w -> w.active = active);
	}
	
	@Override
	public void setVisible(boolean visible) {
		this.apply(w -> w.visible = visible);
	}
	
	private void apply(Consumer<Widget> func) {
		for (Widget w : this.widgets) func.accept(w);
	}
	
}
