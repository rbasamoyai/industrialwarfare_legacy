package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;

/**
 * Useful for applying IWidgetDecorator operations on multiple widgets. For a
 * single widget decorator, see {@link WidgetDecorator}. 
 * 
 * @author rbasamoyai
 */

public class WidgetCollectionDecorator extends AbstractWidgetDecorator {

	private final Collection<Widget> widgets;
	
	public WidgetCollectionDecorator(IScreenPage page, Collection<Widget> widgets) {
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
		this.widgets.forEach(func);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int action) {
		return this.test(w -> w.mouseClicked(mouseX, mouseY, action)) || super.mouseClicked(mouseX, mouseY, action);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int action) {
		return this.test(w -> w.mouseReleased(mouseX, mouseY, action)) || super.mouseReleased(mouseX, mouseY, action);
	}
	
	@Override
	public boolean mouseDragged(double mouseX1, double mouseY1, int action, double mouseX2, double mouseY2) {
		return this.test(w -> w.mouseDragged(mouseX1, mouseY1, action, mouseX2, mouseY2)) || super.mouseDragged(mouseX1, mouseY1, action, mouseX2, mouseY2);
	}
	
	private boolean test(Predicate<Widget> predicate) {
		for (Widget w : this.widgets) {
			if (predicate.test(w)) return true;
		}
		return false;
	}
	
}
