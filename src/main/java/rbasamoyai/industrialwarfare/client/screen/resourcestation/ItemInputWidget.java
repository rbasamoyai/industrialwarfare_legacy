package rbasamoyai.industrialwarfare.client.screen.resourcestation;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

public class ItemInputWidget extends AbstractWidget {

	private static final int SLOT_WIDTH = 16;
	private static final int HOVER_COLOR = 0x80FFFFFF;
	private final Consumer<ItemStack> callback;
	
	public ItemInputWidget(int x, int y, Consumer<ItemStack> callback) {
		super(x, y, SLOT_WIDTH, SLOT_WIDTH, TextComponent.EMPTY);
		this.callback = callback;
	}
	
	@Override
	public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		if (this.isHovered) {
			RenderSystem.disableDepthTest();
			this.fillGradient(stack, this.x, this.y, this.x + SLOT_WIDTH, this.y + SLOT_WIDTH, HOVER_COLOR, HOVER_COLOR);
			RenderSystem.enableDepthTest();
		}
	}
	
	public void setItem(ItemStack stack) {
		this.callback.accept(stack);
	}
	
	@Override public void playDownSound(SoundManager manager) {}
	
	@Override public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
	
}
