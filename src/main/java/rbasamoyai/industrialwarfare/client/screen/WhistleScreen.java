package rbasamoyai.industrialwarfare.client.screen;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.HoldSelectImageButton;
import rbasamoyai.industrialwarfare.common.containers.whistle.WhistleContainer;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.WhistleScreenMessages.SStopAction;

public class WhistleScreen extends ContainerScreen<WhistleContainer> {

	private static final ResourceLocation WHISTLE_TAB_LOCATION = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/whistle_grid.png");
	
	private static final String TRANSLATION_TEXT_KEY = "gui." + IndustrialWarfare.MOD_ID + ".text.";
	private static final String COMBAT_MODE_KEY = TRANSLATION_TEXT_KEY + "combat_mode.";
	
	private static final int RIGHT_HAND_TAB_TEX_Y = 0;
	private static final int LEFT_HAND_TAB_TEX_Y = 104;
	private static final int GRID_START_X = 9;
	private static final int GRID_START_Y = 39;
	private static final int BUTTON_WIDTH = 18;
	private static final int BUTTON_HEIGHT = 18;
	private static final int BUTTON_X_SPACING = 1;
	private static final int BUTTON_Y_SPACING = 1;
	private static final int BUTTON_X_DIST = BUTTON_WIDTH + BUTTON_X_SPACING;
	private static final int BUTTON_Y_DIST = BUTTON_HEIGHT + BUTTON_Y_SPACING;
	private static final int BUTTON_TEX_X_START = 150;
	private static final int BUTTON_TEX_Y_START = 0;
	
	private ItemStack whistleItem = new ItemStack(ItemInit.WHISTLE.get());
	
	private Map<CombatMode, HoldSelectImageButton> modeMap = new HashMap<>();
	private Map<UnitFormationType<?>, HoldSelectImageButton> typeMap = new HashMap<>();
	
	public WhistleScreen(WhistleContainer container, PlayerInventory playerInv, ITextComponent title) {
		super(container, playerInv, title);
		
		this.imageWidth = 150;
		this.imageHeight = 104;
		this.titleLabelY = 25;
	}
	
	@Override
	protected void init() {
		super.init();
		
		boolean flag = this.minecraft.options.mainHand == HandSide.LEFT;
		
		this.topPos = this.height - this.imageHeight;
		this.leftPos = this.width / 2;
		
		if (flag) {
			this.leftPos -= this.imageWidth + 100;
		} else {
			this.leftPos += 100;
		}
		
		/* Mode buttons */
		
		this.modeMap.put(CombatMode.ATTACK, new HoldSelectImageButton(
				this.leftPos + GRID_START_X,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START,
				BUTTON_TEX_Y_START,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedMode(CombatMode.ATTACK),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		this.modeMap.put(CombatMode.DEFEND, new HoldSelectImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH,
				BUTTON_TEX_Y_START,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedMode(CombatMode.DEFEND),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		this.modeMap.put(CombatMode.STAND_GROUND, new HoldSelectImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST * 2,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH * 2,
				BUTTON_TEX_Y_START,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedMode(CombatMode.STAND_GROUND),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		this.modeMap.put(CombatMode.DONT_ATTACK, new HoldSelectImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST * 3,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH * 3,
				BUTTON_TEX_Y_START,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedMode(CombatMode.DONT_ATTACK),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		this.modeMap.values().forEach(this::addButton);
		
		/* Formation buttons */
		
		this.typeMap.put(UnitFormationTypeInit.LINE_10W3D.get(), new HoldSelectImageButton(
				this.leftPos + GRID_START_X,
				this.topPos + GRID_START_Y + BUTTON_Y_DIST,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START,
				BUTTON_TEX_Y_START + BUTTON_HEIGHT * 3,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedType(UnitFormationTypeInit.LINE_10W3D.get()),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		this.typeMap.put(UnitFormationTypeInit.COLUMN_4W10D.get(), new HoldSelectImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST,
				this.topPos + GRID_START_Y + BUTTON_Y_DIST,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH,
				BUTTON_TEX_Y_START + BUTTON_HEIGHT * 3,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedType(UnitFormationTypeInit.COLUMN_4W10D.get()),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		this.typeMap.values().forEach(this::addButton);
		
		this.addButton(new ImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST * 4,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH * 4,
				BUTTON_TEX_Y_START,
				BUTTON_HEIGHT,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				this::stopWhistle,
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY) {
			@Override
			public void playDownSound(SoundHandler handler) {
				handler.play(SimpleSound.forUI(SoundEvents.ANVIL_PLACE, 1.0f));
			}
		});
		
		this.setSelectedMode(this.menu.getMode());
		this.setSelectedType(this.menu.getFormation());
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {	
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		TextureManager texManager = this.minecraft.textureManager;
		
		boolean flag = this.minecraft.options.mainHand == HandSide.LEFT;
		
		texManager.bind(WHISTLE_TAB_LOCATION);
		
		int texY = flag ? LEFT_HAND_TAB_TEX_Y : RIGHT_HAND_TAB_TEX_Y;
		this.blit(stack, this.leftPos, this.topPos, 0, texY, this.imageWidth, this.imageHeight);
		
		ItemRenderer itemRenderer = this.minecraft.getItemRenderer();
		int itemX = this.leftPos;
		if (flag) {
			itemX += this.imageWidth - 31;
		} else {
			itemX += 15;
		}
		int itemY = this.height - this.imageHeight + 4;
		itemRenderer.renderGuiItem(this.whistleItem, itemX, itemY);
	}
	
	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
		this.font.draw(stack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
	}
	
	private void setSelectedMode(CombatMode mode) {
		this.modeMap.forEach((k, v) -> v.setSelected(k == mode));
		if (this.menu.getMode() == mode) return;
		this.minecraft.player.displayClientMessage(new TranslationTextComponent(COMBAT_MODE_KEY + mode.toString()), true);
		this.menu.setMode(mode);
		this.menu.updateServer();
	}
	
	private void setSelectedType(UnitFormationType<?> type) {
		this.typeMap.forEach((k, v) -> v.setSelected(k == type));
		if (this.menu.getFormation() == type) return;
		this.menu.setFormation(type);
		this.menu.updateServer();
	}
	
	private void stopWhistle(Button button) {
		IWNetwork.CHANNEL.sendToServer(new SStopAction());
	}
	
}
