package rbasamoyai.industrialwarfare.client.screen;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.workstations.DummyRecipeItemHandler;
import rbasamoyai.industrialwarfare.common.containers.workstations.ManufacturingBlockMenu;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.SWorkstationPlayerActionMessage;

public class NormalWorkstationScreen extends AbstractContainerScreen<ManufacturingBlockMenu> {

	private static final ResourceLocation NORMAL_WORKSTATION_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/workstations/normal_workstation.png");
	
	private static final TranslatableComponent CRAFT_TEXT = new TranslatableComponent("gui." + IndustrialWarfare.MOD_ID + ".workstation.craft");
	private static final String CANNOT_CRAFT_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".workstation.tooltip.cannot_craft";
	private static final List<Component> CANNOT_CRAFT_TEXT = Arrays.asList(
			new TranslatableComponent(CANNOT_CRAFT_KEY_ROOT + "1"),
			new TranslatableComponent(CANNOT_CRAFT_KEY_ROOT + "2")
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
	
	public NormalWorkstationScreen(ManufacturingBlockMenu container, Inventory playerInv, Component localTitle) {
		super(container, playerInv, localTitle);
		this.imageHeight = 186;
		this.inventoryLabelY = this.imageHeight - 94;
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		
		Button.OnPress craftButton$pressable = (button) -> IWNetwork.CHANNEL.sendToServer(new SWorkstationPlayerActionMessage(this.menu.blockPos(), 1));
		Button.OnTooltip craftButton$tooltip = (button, stack, mouseX, mouseY) -> {
			if (this.menu.hasWorker() && this.menu.isViewerDifferentFromWorker())
				this.renderComponentTooltip(stack, CANNOT_CRAFT_TEXT, mouseX, mouseY);
		};
		
		this.craftButton = this.addRenderableWidget(new Button(
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
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		
		RenderSystem.setShaderTexture(0, NORMAL_WORKSTATION_GUI);
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		int workProgress = (int)(PROGRESS_BAR_WIDTH * this.menu.workingTicksScaled());
		this.blit(stack, this.leftPos + PROGRESS_BAR_GUI_X, this.topPos + PROGRESS_BAR_GUI_Y, PROGRESS_BAR_TEX_X, PROGRESS_BAR_TEX_Y, (int) workProgress, PROGRESS_BAR_HEIGHT);
	}
	
	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
		
		if (this.menu.hasWorker() && this.menu.isViewerDifferentFromWorker()) {
			this.fillGradient(stack, RECIPE_SLOT_X, RECIPE_SLOT_Y, RECIPE_SLOT_X + 16, RECIPE_SLOT_Y + 16, SHADE_COLOR, SHADE_COLOR);
		}
	}
	
	@Override
	protected void renderTooltip(PoseStack stack, int mouseX, int mouseY) {
		super.renderTooltip(stack, mouseX, mouseY);
		if (this.craftButton.isHoveredOrFocused()) this.craftButton.renderToolTip(stack, mouseX, mouseY);
	}
	
	@Override
	public void onClose() {
		IWNetwork.CHANNEL.sendToServer(new SWorkstationPlayerActionMessage(menu.blockPos(), 2));
		super.onClose();
	}
	
	@Override
	public void containerTick() {
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
		
		super.containerTick();
	}
	
}
