package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

public class RowIndexWidget extends Widget {

	private final int rowIndex;
	
	public RowIndexWidget(int x, int y, int width, int height, int rowIndex) {
		super(x, y, width, height, StringTextComponent.EMPTY);
		
		this.rowIndex = rowIndex;
	}
	
	@Override
	public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	public int getRowIndex() {
		return this.rowIndex;
	}
	
	@Override
	public void playDownSound(SoundHandler handler) {
		
	}
	
}
