package rbasamoyai.industrialwarfare.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.taskscrollshelf.TaskScrollShelfMenu;

public class TaskScrollShelfScreen extends AbstractContainerScreen<TaskScrollShelfMenu> {
	
	private static final ResourceLocation TASK_SCROLL_SHELF_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/task_scroll_shelf.png");

	public TaskScrollShelfScreen(TaskScrollShelfMenu container, Inventory playerInv, Component title) {
		super(container, playerInv, title);
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 94;
	}
	
	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, TASK_SCROLL_SHELF_GUI);
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

}
