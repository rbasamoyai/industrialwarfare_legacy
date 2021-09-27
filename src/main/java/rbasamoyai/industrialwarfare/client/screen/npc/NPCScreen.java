package rbasamoyai.industrialwarfare.client.screen.npc;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.npcs.NPCContainer;

public class NPCScreen extends ContainerScreen<NPCContainer> {

	private static final ResourceLocation NPC_SCREEN_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/npcs/npc_base.png");
	
	private static final TranslationTextComponent NPC_INVENTORY_TEXT = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".container.npc.inventory");
	
	private static final int SHADE_COLOR = 0x40000000;
	private static final int TEXT_COLOR = 4210752;
	
	private static final int SLOT_SPACING = 18;
	private static final int INVENTORY_SLOT_GUI_START_X = 79;
	private static final int INVENTORY_SLOT_GUI_START_Y = 71;
	private static final int INVENTORY_SLOT_GUI_COLUMNS = 5;
	private static final int EQUIPMENT_SLOT_START_X = 80;
	private static final int EQUIPMENT_SLOT_START_Y = 22;
	private static final int SLOT_TEX_X = 176;
	private static final int SLOT_TEX_Y = 0;
	
	public NPCScreen(NPCContainer container, PlayerInventory playerInv, ITextComponent localTitle) {
		super(container, playerInv, localTitle);
		
		this.imageWidth = 176;
		this.imageHeight = 222;
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
	protected void renderBg(MatrixStack stack, float partialTicks, int x, int y) {
		TextureManager texManager = this.getMinecraft().getTextureManager();
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		texManager.bind(NPC_SCREEN_GUI);
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		for (int i = 0; i < this.menu.getInvSlotCount(); i++) {
			int offsetX = this.leftPos + INVENTORY_SLOT_GUI_START_X + i % INVENTORY_SLOT_GUI_COLUMNS * SLOT_SPACING;
			int offsetY = this.topPos + INVENTORY_SLOT_GUI_START_Y + i / INVENTORY_SLOT_GUI_COLUMNS * SLOT_SPACING;
			this.blit(stack, offsetX, offsetY, SLOT_TEX_X, SLOT_TEX_Y, SLOT_SPACING, SLOT_SPACING);
		}
	}
	
	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
		this.font.draw(stack, NPC_INVENTORY_TEXT, INVENTORY_SLOT_GUI_START_X, INVENTORY_SLOT_GUI_START_Y - 11, TEXT_COLOR);
		
		if (!this.menu.areArmorSlotsEnabled()) {
			int x = EQUIPMENT_SLOT_START_X;
			int y = EQUIPMENT_SLOT_START_Y;
			
			fill(stack, x, y, x + 16, y + 16, SHADE_COLOR);
			x += SLOT_SPACING;
			
			fill(stack, x, y, x + 16, y + 16, SHADE_COLOR);
			x += SLOT_SPACING;
			
			fill(stack, x, y, x + 16, y + 16, SHADE_COLOR);
			x = EQUIPMENT_SLOT_START_X;
			y += SLOT_SPACING;
			
			fill(stack, x, y, x + 16, y + 16, SHADE_COLOR);
		}
	}
	
	@Override
	public void onClose() {
		super.onClose();
	}

}
