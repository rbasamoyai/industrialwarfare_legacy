package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import rbasamoyai.industrialwarfare.client.screen.widgets.MultiStateImageButton;

public class DeactivatableImageButton extends MultiStateImageButton {

	public DeactivatableImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, int texWidth, int texHeight, OnPress pressable, OnTooltip tooltip, Component message) {
		super(x, y, width, height, texX, texY, resourceLocation, texWidth, texHeight, pressable, tooltip, message);
	}
	
	@Override
	public int getHeightMultiplier() {
		if (!this.active) return 0;
		else if (this.isHovered) return 2;
		else return 1;
	}
	
}
