package rbasamoyai.industrialwarfare.client.screen.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Effectively a copy of {@link
 * net.minecraft.client.gui.widget.button.ImageButton}, but with
 * {@link net.minecraft.client.gui.widget.button.ImageButton#renderButton}
 * changed so that it can render another texture depending on certain
 * conditions.
 * 
 * The textures must be vertically arranged and must all be of the same size.
 * 
 * Due to {@code ImageButton} having all its fields private, this may as well be a
 * reimplementation.
 * 
 * @author rbasamoyai
 */
public abstract class MultiStateImageButton extends Button {

	private final ResourceLocation resourceLocation; 
	private final int xTexStart;
	private final int yTexStart;
	private final int textureWidth;
	private final int textureHeight;
	   
	public MultiStateImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, IPressable pressable) {
		this(x, y, width, height, texX, texY, resourceLocation, 256, 256, pressable);
	}

	public MultiStateImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, int texWidth, int texHeight, IPressable pressable) {
		this(x, y, width, height, texX, texY, resourceLocation, texWidth, texHeight, pressable, StringTextComponent.EMPTY);
	}
	
	public MultiStateImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, int texWidth, int texHeight, IPressable pressable, ITextComponent message) {
		this(x, y, width, height, texX, texY, resourceLocation, texWidth, texHeight, pressable, Button.NO_TOOLTIP, message);
	}
	
	public MultiStateImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, int texWidth, int texHeight, IPressable pressable, ITooltip tooltip, ITextComponent message) {
		super(x, y, width, height, message, pressable, tooltip);
		
		this.resourceLocation = resourceLocation;
		this.xTexStart = texX;
		this.yTexStart = texY;
		this.textureWidth = texWidth;
		this.textureHeight = texHeight;
	}
	
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		Minecraft.getInstance().getTextureManager().bind(this.resourceLocation);
		
		int y = this.yTexStart + this.height * this.getHeightMultiplier();
		
		RenderSystem.enableDepthTest();
		blit(stack, this.x, this.y, (float) this.xTexStart, (float) y, this.width, this.height, this.textureWidth, this.textureHeight);
		
		if (this.isHovered()) {
			this.onTooltip.onTooltip(this, stack, mouseX, mouseY);
		}
	}
	
	public abstract int getHeightMultiplier();
	
}
