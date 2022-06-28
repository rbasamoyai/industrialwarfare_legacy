package rbasamoyai.industrialwarfare.client.screen.widgets.page;

import java.awt.Point;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import rbasamoyai.industrialwarfare.IndustrialWarfare;

/**
 * @implNote Only works for 256 x 256 textures.
 * 
 * @author rbasamoyai
 */
public class BlitDecorator extends ScreenPageDecorator {

	private final ResourceLocation textureLocation;
	protected final Point texPos;
	protected final Point dimensions;
	
	protected Point pos;
	
	public BlitDecorator(IScreenPage page, GuiComponent gui, Properties properties) {
		super(page);
		
		this.textureLocation = properties.textureLocation;
		
		this.pos = properties.pos;
		this.texPos = properties.texPos;
		this.dimensions = properties.dimensions;
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		RenderSystem.setShaderTexture(0, this.textureLocation);
		
		Screen screen = this.getScreen();
		screen.blit(stack, this.pos.x, this.pos.y, this.texPos.x, this.texPos.y, this.dimensions.x, this.dimensions.y);
	}
	
	public static class Properties {
		public ResourceLocation textureLocation = new ResourceLocation(IndustrialWarfare.MOD_ID, "empty");
		public Point pos = new Point(0, 0);
		public Point texPos = new Point(0, 0);
		public Point dimensions = new Point(0, 0);
		
		public Properties textureLocation(ResourceLocation textureLocation) {
			this.textureLocation = textureLocation;
			return this;
		}
		
		public Properties pos(Point pos) {
			this.pos = pos;
			return this;
		}
		
		public Properties texturePos(Point texPos) {
			this.texPos = texPos;
			return this;
		}
		
		public Properties textureDimensions(Point dimensions) {
			this.dimensions = dimensions;
			return this;
		}
	}
	
}
