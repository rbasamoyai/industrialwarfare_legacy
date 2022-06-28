package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;

public class RowIndexWidget extends AbstractWidget {

	private final int rowIndex;
	
	public RowIndexWidget(int x, int y, int width, int height, int rowIndex) {
		super(x, y, width, height, TextComponent.EMPTY);
		this.rowIndex = rowIndex;
	}
	
	public int getRowIndex() { return this.rowIndex; }
	
	@Override public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {}
	@Override public void playDownSound(SoundManager manager) {}
	@Override public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
	
}
