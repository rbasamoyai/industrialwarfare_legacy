package rbasamoyai.industrialwarfare.client.screen;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.workstations.DummyRecipeItemHandler;
import rbasamoyai.industrialwarfare.common.containers.workstations.NormalWorkstationContainer;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.SWorkstationPlayerActionMessage;

public class NormalWorkstationScreen extends ContainerScreen<NormalWorkstationContainer> {

	private static final ResourceLocation NORMAL_WORKSTATION_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/workstations/normal_workstation.png");
	
	private static final TranslationTextComponent CRAFT_TEXT = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".workstation.craft");
	private static final String CANNOT_CRAFT_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".workstation.tooltip.cannot_craft";
	private static final List<ITextComponent> CANNOT_CRAFT_TEXT = Arrays.asList(
			new TranslationTextComponent(CANNOT_CRAFT_KEY_ROOT + "1"),
			new TranslationTextComponent(CANNOT_CRAFT_KEY_ROOT + "2")
			);
	
	private static final int PROGRESS_BAR_TEX_X = 176;
	private static final int PROGRESS_BAR_TEX_Y = 0;
	private static final int PROGRESS_BAR_GUI_X = 99;
	private static final int PROGRESS_BAR_GUI_Y = 44;
	private static final int PROGRESS_BAR_WIDTH = 28;
	private static final int PROGRESS_BAR_HEIGHT = 17;
	
	private static final int CRAFT_BUTTON_CENTER_X = 113;
	private static final int CRAFT_BUTTON_Y = 68;
	private static final int CRAFT_BUTTON_HEIGHT = 20;
	
	private static final int RECIPE_SLOT_X = 105;
	private static final int RECIPE_SLOT_Y = 20;
	
	private static final int SHADE_COLOR = 1090453504;
	
	private Button craftButton;
	
	public NormalWorkstationScreen(NormalWorkstationContainer container, PlayerInventory playerInv, ITextComponent localTitle) {
		super(container, playerInv, localTitle);
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 94;
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		
		Button.IPressable craftButton$pressable = (button) -> IWNetwork.CHANNEL.sendToServer(new SWorkstationPlayerActionMessage(this.menu.blockPos(), 1));
		Button.ITooltip craftButton$tooltip = (button, stack, mouseX, mouseY) -> {
			if (this.menu.hasWorker() && this.menu.isViewerDifferentFromWorker())
				this.renderComponentTooltip(stack, CANNOT_CRAFT_TEXT, mouseX, mouseY);
		};
		
		this.craftButton = this.addButton(new Button(
				this.leftPos + CRAFT_BUTTON_CENTER_X - this.font.width(CRAFT_TEXT) / 2 - 4,
				this.topPos + CRAFT_BUTTON_Y,
				this.font.width(CRAFT_TEXT) + 8,
				CRAFT_BUTTON_HEIGHT,
				CRAFT_TEXT,
				craftButton$pressable,
				craftButton$tooltip
				));
		this.craftButton.active = !this.menu.hasWorker();
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
		
		this.getMinecraft().getTextureManager().bind(NORMAL_WORKSTATION_GUI);
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		int workProgress = (int)(PROGRESS_BAR_WIDTH * this.menu.workingTicksScaled());
		this.blit(stack, this.leftPos + PROGRESS_BAR_GUI_X, this.topPos + PROGRESS_BAR_GUI_Y, PROGRESS_BAR_TEX_X, PROGRESS_BAR_TEX_Y, (int) workProgress, PROGRESS_BAR_HEIGHT);
	}
	
	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
		
		if (this.menu.hasWorker() && this.menu.isViewerDifferentFromWorker()) {
			this.fillGradient(stack, RECIPE_SLOT_X, RECIPE_SLOT_Y, RECIPE_SLOT_X + 16, RECIPE_SLOT_Y + 16, SHADE_COLOR, SHADE_COLOR);
		}
	}
	
	@Override
	protected void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
		super.renderTooltip(stack, mouseX, mouseY);
		if (this.craftButton.isHovered()) this.craftButton.renderToolTip(stack, mouseX, mouseY);
	}
	
	@Override
	public void onClose() {
		IWNetwork.CHANNEL.sendToServer(new SWorkstationPlayerActionMessage(menu.blockPos(), 2));
		super.onClose();
	}
	
	@Override
	public void tick() {
		this.craftButton.x = (this.width - this.imageWidth) / 2 + CRAFT_BUTTON_CENTER_X - craftButton.getWidth() / 2;
		this.craftButton.active = !this.menu.hasWorker();
		
		// Set insert status of client recipe slot
		Slot clientRecipeSlot = this.menu.slots.get(5);
		if (clientRecipeSlot instanceof SlotItemHandler) {
			IItemHandler handler = ((SlotItemHandler) clientRecipeSlot).getItemHandler();
			if (handler instanceof DummyRecipeItemHandler) {
				((DummyRecipeItemHandler) handler).canInteract = !this.menu.hasWorker() || !this.menu.isViewerDifferentFromWorker();
			}
		}
	}
	
}
