package rbasamoyai.industrialwarfare.client.screen.attachmentitems;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.attachmentitems.AttachmentsRifleMenu;

public class AttachmentsRifleScreen extends AbstractContainerScreen<AttachmentsRifleMenu> {

	private static final ResourceLocation ATTACHMENTS_SLOTS_TEXTURE = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/attachment_slots.png");
	
	private static final int ITEM_RENDER_X = 4;
	private static final int ITEM_RENDER_Y = -4;
	private static final int INVENTORY_HEIGHT = 100;
	private static final int SLOT_WIDTH = 18;
	private static final float ITEM_SCALE = 10.0f;
	
	public AttachmentsRifleScreen(AttachmentsRifleMenu container, Inventory playerInv, Component title) {
		super(container, playerInv, title);
		
		this.imageWidth = 176;
		this.imageHeight = 200;
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
		RenderSystem.setShaderTexture(0, ATTACHMENTS_SLOTS_TEXTURE);
		
		this.blit(stack, this.leftPos, this.topPos + this.imageHeight - INVENTORY_HEIGHT, 0, 0, this.imageWidth, INVENTORY_HEIGHT);
		
		this.fillGradient(stack, this.leftPos, this.topPos + this.titleLabelY + 10, this.leftPos + this.imageWidth, this.topPos + this.imageHeight - INVENTORY_HEIGHT, 0x3fffffff, 0x3fffffff);
		
		ItemStack selected = this.menu.getTargetStack();
		
		stack.pushPose();
		stack.translate(this.leftPos + ITEM_RENDER_X, this.topPos + ITEM_RENDER_Y, -100.0f);
		stack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
		this.itemRenderer.renderGuiItem(selected, 0, 0);		
		stack.popPose();
		
		RenderSystem.setShaderTexture(0, ATTACHMENTS_SLOTS_TEXTURE);
	}
	
	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
		this.font.drawShadow(stack, this.title, this.titleLabelX, this.titleLabelY, 0xffffffff);
		this.font.draw(stack, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752);
	}
	
}
