package rbasamoyai.industrialwarfare.client.screen.widgets;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class HoldSelectImageButton extends MultiStateImageButton {

	private boolean selected = false;
	
	public HoldSelectImageButton(int x, int y, int width, int height, int texX, int texY, ResourceLocation resourceLocation, int texWidth, int texHeight, IPressable pressable, ITooltip tooltip, ITextComponent message) {
		super(x, y, width, height, texX, texY, resourceLocation, texWidth, texHeight, pressable, tooltip, message);
	}
	
	public void setSelected(boolean selected) { this.selected = selected; }
	
	@Override
	public int getHeightMultiplier() {
		if (this.selected) return 2;
		if (this.isHovered) return 1;
		return 0;
	}

}
