package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import java.awt.Point;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.math.MathHelper;

/**
 * Add a pseudo-widget that can be dragged around the screen within given
 * bounds.
 * 
 * @implNote Only works for 256 x 256 textures.
 * 
 * @author rbasamoyai
 */
public class DraggableDecorator extends BlitDecorator {

	private final Point dragDimensions;
	private final Point start;
	
	private FloatPoint output;
	
	private boolean isDragging;
	private boolean isHovering;
	private boolean isActive;
	
	private int regularTexPosY;
	
	public DraggableDecorator(IScreenPage page, AbstractGui gui, Properties properties) {
		super(page, gui, properties);
		
		this.dragDimensions = properties.dragDimensions;
		this.start = properties.startingPoint;
		this.output = properties.startingOutput;
		
		this.regularTexPosY = this.texPos.y;
		
		this.recalculatePos();
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.isHovering = mouseX >= this.pos.x && mouseX < this.pos.x + this.dimensions.x && mouseY >= this.pos.y && mouseY < this.pos.y + this.dimensions.y;
		this.texPos.y = this.isActive ? this.regularTexPosY : this.regularTexPosY + this.dimensions.y;
		super.render(stack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.isDragging || !this.isHovering || !this.isActive || button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !this.insideDragArea(mouseX, mouseY)) {
			return super.mouseClicked(mouseX, mouseY, button);		
		}
		this.isDragging = true;
		return true;
	}
	
	@Override
	public boolean mouseDragged(double mouseX1, double mouseY1, int button, double mouseX2, double mouseY2) {
		if (!this.isDragging || !this.isHovering || !this.isActive || !this.insideDragArea(mouseX1, mouseY1)) {
			return super.mouseDragged(mouseX1, mouseY1, button, mouseX2, mouseY2);
		}
		
		this.output.x = (float)(mouseX1 - (double) this.start.x) / (float) this.dragDimensions.x;
		this.output.x = MathHelper.clamp(this.output.x, 0.0f, 1.0f);
		
		this.output.y = (float)(mouseY1 - (double) this.start.y) / (float) this.dragDimensions.y;
		this.output.y = MathHelper.clamp(this.output.y, 0.0f, 1.0f);
		
		this.recalculatePos();
		
		return true;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			this.isDragging = false;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	private void recalculatePos() {
		this.pos.x = this.start.x - MathHelper.floor((float) this.dimensions.x * 0.5f) + MathHelper.floor((float) this.dragDimensions.x * this.output.x);
		this.pos.y = this.start.y - MathHelper.floor((float) this.dimensions.y * 0.5f) + MathHelper.floor((float) this.dragDimensions.y * this.output.y);
	}
	
	public void setActive(boolean isActive) { this.isActive = isActive; }
	public float getOutputX() { return this.output.x; }
	public float getOutputY() { return this.output.y; }
	
	private boolean insideDragArea(double mouseX, double mouseY) {
		double startX = (double) this.start.x;
		double startY = (double) this.start.y;
		double halfWidth = (double) this.dimensions.x * 0.5d;
		double halfHeight = (double) this.dimensions.y * 0.5d;
		
		return mouseX >= startX - halfWidth
				&& mouseX < startX + (double) this.dragDimensions.x + halfWidth
				&& mouseY >= startY - halfHeight
				&& mouseY < startY + (double) this.dragDimensions.y + halfWidth;
	}
	
	public static class Properties extends BlitDecorator.Properties {
		public FloatPoint startingOutput = new FloatPoint(0.0f, 0.0f);
		public Point dragDimensions = new Point(0, 0);
		public Point startingPoint = new Point(0, 0);
		
		public Properties startingOutput(FloatPoint startingOutput) {
			this.startingOutput = startingOutput;
			return this;
		}
		
		public Properties dragDimensions(Point dragDimensions) {
			this.dragDimensions = dragDimensions;
			return this;
		}
		
		public Properties startingPoint(Point startingPoint) {
			this.startingPoint = startingPoint;
			return this;
		}
	}
	
	public static class FloatPoint {
		public float x;
		public float y;
		
		public FloatPoint(float x, float y) {
			this.x = x;
			this.y = y;
		}
		
		public FloatPoint(FloatPoint point) {
			this(point.x, point.y);
		}
	}
	
}
