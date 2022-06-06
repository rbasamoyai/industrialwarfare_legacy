package rbasamoyai.industrialwarfare.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfContainer;

public class TaskScrollShelfScreen extends ContainerScreen<TaskScrollShelfContainer> {
	
	private static final ResourceLocation TASK_SCROLL_SHELF_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/task_scroll_shelf.png");

	public TaskScrollShelfScreen(TaskScrollShelfContainer container, PlayerInventory playerInv, ITextComponent title) {
		super(container, playerInv, title);
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 94;
	}
	
	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		TextureManager texManager = this.minecraft.getTextureManager();
		texManager.bind(TASK_SCROLL_SHELF_GUI);
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

}
