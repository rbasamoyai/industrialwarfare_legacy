package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import rbasamoyai.industrialwarfare.client.screen.widgets.MultiStateImageButton;

public class DeactivatableImageButton extends MultiStateImageButton {

	public DeactivatableImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, int texWidth, int texHeight, IPressable pressable, ITooltip tooltip, ITextComponent message) {
		super(x, y, width, height, texX, texY, resourceLocation, texWidth, texHeight, pressable, tooltip, message);
	}
	
	@Override
	public int getHeightMultiplier() {
		if (!this.active) return 0;
		else if (this.isHovered) return 2;
		else return 1;
	}
	
}
