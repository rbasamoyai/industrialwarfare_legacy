package rbasamoyai.industrialwarfare.client.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

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
	   
	public MultiStateImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, OnPress pressable) {
		this(x, y, width, height, texX, texY, resourceLocation, 256, 256, pressable);
	}

	public MultiStateImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, int texWidth, int texHeight, OnPress pressable) {
		this(x, y, width, height, texX, texY, resourceLocation, texWidth, texHeight, pressable, TextComponent.EMPTY);
	}
	
	public MultiStateImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, int texWidth, int texHeight, OnPress pressable, Component message) {
		this(x, y, width, height, texX, texY, resourceLocation, texWidth, texHeight, pressable, Button.NO_TOOLTIP, message);
	}
	
	public MultiStateImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, int texWidth, int texHeight, OnPress pressable, OnTooltip tooltip, Component message) {
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
	public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShaderTexture(0, this.resourceLocation);
		
		int y = this.yTexStart + this.height * this.getHeightMultiplier();
		
		RenderSystem.enableDepthTest();
		blit(stack, this.x, this.y, (float) this.xTexStart, (float) y, this.width, this.height, this.textureWidth, this.textureHeight);
		
		if (this.isHovered) {
			this.onTooltip.onTooltip(this, stack, mouseX, mouseY);
		}
	}
	
	public abstract int getHeightMultiplier();
	
}
