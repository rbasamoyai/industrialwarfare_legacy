package rbasamoyai.industrialwarfare.client.screen.attachmentitems;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.attachmentitems.AttachmentsRifleContainer;

public class AttachmentsRifleScreen extends ContainerScreen<AttachmentsRifleContainer> {

	private static final ResourceLocation ATTACHMENTS_SLOTS_TEXTURE = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/attachment_slots.png");
	
	private static final int ITEM_RENDER_X = 4;
	private static final int ITEM_RENDER_Y = -4;
	private static final int INVENTORY_HEIGHT = 100;
	private static final int SLOT_WIDTH = 18;
	private static final float ITEM_SCALE = 10.0f;
	
	public AttachmentsRifleScreen(AttachmentsRifleContainer container, PlayerInventory playerInv, ITextComponent title) {
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
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		TextureManager texManager = this.minecraft.textureManager;
		texManager.bind(ATTACHMENTS_SLOTS_TEXTURE);
		this.blit(stack, this.leftPos, this.topPos + this.imageHeight - INVENTORY_HEIGHT, 0, 0, this.imageWidth, INVENTORY_HEIGHT);
		
		this.fillGradient(stack, this.leftPos, this.topPos + this.titleLabelY + 10, this.leftPos + this.imageWidth, this.topPos + this.imageHeight - INVENTORY_HEIGHT, 0x3fffffff, 0x3fffffff);
		
		ItemStack selected = this.menu.getTargetStack();
		
		RenderSystem.pushMatrix();
		
		RenderSystem.translatef(this.leftPos + ITEM_RENDER_X, this.topPos + ITEM_RENDER_Y, -100.0f);
		RenderSystem.scalef(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
		
		this.itemRenderer.renderGuiItem(selected, 0, 0);
		
		RenderSystem.popMatrix();
		
		texManager.bind(ATTACHMENTS_SLOTS_TEXTURE);
	}
	
	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
		this.font.drawShadow(stack, this.title, this.titleLabelX, this.titleLabelY, 0xffffffff);
		this.font.draw(stack, this.inventory.getDisplayName(), this.inventoryLabelX, this.inventoryLabelY, 4210752);
	}
	
}
