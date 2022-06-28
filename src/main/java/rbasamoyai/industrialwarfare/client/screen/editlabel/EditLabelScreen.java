package rbasamoyai.industrialwarfare.client.screen.editlabel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelectorWidget;
import rbasamoyai.industrialwarfare.common.containers.EditLabelMenu;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.SEditLabelSyncMessage;
import rbasamoyai.industrialwarfare.utils.TextureUtils;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;

public class EditLabelScreen extends AbstractContainerScreen<EditLabelMenu> {

	private static final ResourceLocation EDIT_LABEL_SCREEN_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/edit_label.png");
	
	private static final String EDIT_LABEL_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".edit_label";
	private static final MutableComponent NAME_FIELD = new TranslatableComponent(EDIT_LABEL_KEY_ROOT + ".name").append(": ");
	private static final MutableComponent UUID_FIELD = new TranslatableComponent(EDIT_LABEL_KEY_ROOT + ".uuid").append(": ");
	private static final MutableComponent RESET_BUTTON_TEXT = new TranslatableComponent(EDIT_LABEL_KEY_ROOT + ".reset");
	
	private static final int NUM_SELECTOR_WIDGET_X = 10;
	private static final int NUM_SELECTOR_WIDGET_Y = 16;
	private static final int NUM_SELECTOR_WIDGET_WIDTH = 48;
	
	private static final int RESET_BUTTON_X = NUM_SELECTOR_WIDGET_X + NUM_SELECTOR_WIDGET_WIDTH + 2;
	private static final int RESET_BUTTON_Y = NUM_SELECTOR_WIDGET_Y + 20;
	private static final int RESET_BUTTON_HEIGHT = 20;
	private static final int NAME_UUID_FIELD_X = RESET_BUTTON_X;
	private static final int NAME_UUID_FIELD_Y = NUM_SELECTOR_WIDGET_Y;
	
	private static final int NAME_UUID_FIELD_TEXT_X = NAME_UUID_FIELD_X + 4;
	private static final int NAME_UUID_FIELD_TEXT_Y = NAME_UUID_FIELD_Y + 5;
	
	private static final int NAME_UUID_FIELD_HEIGHT = 18;
	
	private static final int TITLE_Y = 4;
	private static final int TEXT_COLOR = 8350000;
	private static final int FIELD_SHADOW_COLOR = 536870912;
	
	private static final int BORDER_START_COLOR_X = 0;
	private static final int BORDER_END_COLOR_X = 1;
	private static final int FIELD_COLOR_X = 2;
	private static final int COLORS_Y = 64;
	private static final List<Pair<Integer, Integer>> COLOR_COORDS = Arrays.asList(
			Pair.of(BORDER_START_COLOR_X, COLORS_Y),
			Pair.of(BORDER_END_COLOR_X, COLORS_Y),
			Pair.of(FIELD_COLOR_X, COLORS_Y)
			);
	
	private ArgSelectorWidget numSelectWidget;
	private Button resetButton;
	
	private final int labelBorderStartColor;
	private final int labelBorderEndColor;
	private final int labelFieldColor;
	
	public EditLabelScreen(EditLabelMenu container, Inventory playerInv, Component title) {
		super(container, playerInv, title);
		
		this.imageWidth = 256;
		this.imageHeight = 64;
		
		// Getting pixels		
		List<Integer> colors = TextureUtils.getColors(EDIT_LABEL_SCREEN_GUI, COLOR_COORDS);
		
		this.labelBorderStartColor = colors.get(0);
		this.labelBorderEndColor = colors.get(1);
		this.labelFieldColor = colors.get(2);
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.numSelectWidget = this.addRenderableWidget(new ArgSelectorWidget(
				this.minecraft,
				this.leftPos + NUM_SELECTOR_WIDGET_X,
				this.topPos + NUM_SELECTOR_WIDGET_Y,
				NUM_SELECTOR_WIDGET_WIDTH,
				Optional.of(new LabelNumArgSelector(this.menu.getNum()))
				));
		
		Button.OnPress resetButton$onPress = (button) -> {
			this.menu.setCachedName(TextComponent.EMPTY);
			this.menu.setUUID(new UUID(0L, 0L));
		};
		
		this.resetButton = this.addRenderableWidget(new Button(
				this.leftPos + RESET_BUTTON_X,
				this.topPos + RESET_BUTTON_Y,
				this.font.width(RESET_BUTTON_TEXT) + 8,
				RESET_BUTTON_HEIGHT,
				RESET_BUTTON_TEXT,
				resetButton$onPress
				));
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
		RenderSystem.setShaderTexture(0, EDIT_LABEL_SCREEN_GUI);
		
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		this.numSelectWidget.render(stack, mouseX, mouseY, partialTicks);
		this.resetButton.render(stack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
		RenderSystem.disableDepthTest();
		this.font.draw(stack, this.title, (float)(this.imageWidth - this.font.width(this.title)) * 0.5f, (float) TITLE_Y, TEXT_COLOR);
		
		if (Screen.hasShiftDown()) {
			Component tc = UUID_FIELD.copy().append(this.menu.getUUID().toString());
			int width = this.font.width(tc) + 8;
			this.fillGradient(stack, NAME_UUID_FIELD_X + 1, NAME_UUID_FIELD_Y + 1, NAME_UUID_FIELD_X + width + 1, NAME_UUID_FIELD_Y + NAME_UUID_FIELD_HEIGHT + 1, FIELD_SHADOW_COLOR, FIELD_SHADOW_COLOR);
			this.fillGradient(stack, NAME_UUID_FIELD_X, NAME_UUID_FIELD_Y, NAME_UUID_FIELD_X + width, NAME_UUID_FIELD_Y + NAME_UUID_FIELD_HEIGHT, this.labelBorderStartColor, this.labelBorderEndColor);
			this.fillGradient(stack, NAME_UUID_FIELD_X + 1, NAME_UUID_FIELD_Y + 1, NAME_UUID_FIELD_X + width - 1, NAME_UUID_FIELD_Y + NAME_UUID_FIELD_HEIGHT - 1, this.labelFieldColor, this.labelFieldColor);
			this.font.draw(stack, tc, NAME_UUID_FIELD_TEXT_X, NAME_UUID_FIELD_TEXT_Y, TEXT_COLOR);
		} else {
			Component cachedName = this.menu.getCachedName();
			if (cachedName.getContents().length() == 0) cachedName = TooltipUtils.NOT_AVAILABLE;
			this.font.draw(stack, NAME_FIELD.copy().append(cachedName), NAME_UUID_FIELD_TEXT_X, NAME_UUID_FIELD_TEXT_Y, TEXT_COLOR);
		}
		RenderSystem.enableDepthTest();
	}
	
	@Override
	protected void renderTooltip(PoseStack stack, int mouseX, int mouseY) {
		if (this.numSelectWidget.isHoveredOrFocused()) {
			Optional<ArgSelector<?>> optional = this.numSelectWidget.getSelector();
			List<Component> tooltip = optional
					.map(ArgSelector::getComponentTooltip)
					.orElseGet(() -> Arrays.asList(TooltipUtils.NOT_AVAILABLE));
			this.renderComponentTooltip(stack, tooltip, mouseX, mouseY);
		}
	}
	
	@Override
	public void onClose() {
		byte newNum = this.numSelectWidget.getSelector().map(as -> (byte) as.getSelectedArg().getArgNum()).orElse((byte) 0);
		IWNetwork.CHANNEL.sendToServer(new SEditLabelSyncMessage(this.menu.getHand(), newNum, this.menu.getUUID(), this.menu.getCachedName()));
		super.onClose();
	}

}
