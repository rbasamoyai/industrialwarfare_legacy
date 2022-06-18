package rbasamoyai.industrialwarfare.client.screen.resourcestation;

import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class ItemInputWidget extends Widget {

	private static final int SLOT_WIDTH = 16;
	private static final int HOVER_COLOR = 0x80FFFFFF;
	private final Consumer<ItemStack> callback;
	
	public ItemInputWidget(int x, int y, Consumer<ItemStack> callback) {
		super(x, y, SLOT_WIDTH, SLOT_WIDTH, StringTextComponent.EMPTY);
		this.callback = callback;
	}
	
	@Override
	public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		if (this.isHovered) {
			RenderSystem.disableDepthTest();
			this.fillGradient(stack, this.x, this.y, this.x + SLOT_WIDTH, this.y + SLOT_WIDTH, HOVER_COLOR, HOVER_COLOR);
			RenderSystem.enableDepthTest();
		}
	}
	
	public void setItem(ItemStack stack) {
		this.callback.accept(stack);
	}
	
	@Override public void playDownSound(SoundHandler handler) {}
	
}
