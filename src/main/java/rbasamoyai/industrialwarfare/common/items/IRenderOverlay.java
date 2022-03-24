package rbasamoyai.industrialwarfare.common.items;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IRenderOverlay {

	void renderOverlay(MatrixStack stack, float partialTicks);
	
}
