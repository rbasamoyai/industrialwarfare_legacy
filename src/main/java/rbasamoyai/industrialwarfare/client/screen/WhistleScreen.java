package rbasamoyai.industrialwarfare.client.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.HoldSelectImageButton;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.BaseScreenPage;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.IScreenPage;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.WidgetCollectionDecorator;
import rbasamoyai.industrialwarfare.common.containers.WhistleContainer;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.FormationCategory;
import rbasamoyai.industrialwarfare.core.init.FormationAttackTypeInit;
import rbasamoyai.industrialwarfare.core.init.UnitFormationTypeInit;
import rbasamoyai.industrialwarfare.core.init.items.ItemInit;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.WhistleScreenMessages.SStopAction;

public class WhistleScreen extends ContainerScreen<WhistleContainer> {

	private static final ResourceLocation WHISTLE_TAB_LOCATION = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/whistle_grid.png");
	
	private static final String TRANSLATION_TEXT_KEY = "tooltip." + IndustrialWarfare.MOD_ID + ".whistle";
	private static final String COMBAT_MODE_KEY = TRANSLATION_TEXT_KEY + ".combat_mode.";
	private static final String INTERVAL_KEY = TRANSLATION_TEXT_KEY + ".interval";
	private static final String INTERVAL2_KEY = INTERVAL_KEY + "2";
	
	private static final ITextComponent INTERVAL_TEXT1 = new TranslationTextComponent(INTERVAL_KEY + "1");
	private static final ITextComponent BACK_TO_MAIN_PAGE_TEXT = new TranslationTextComponent(TRANSLATION_TEXT_KEY + ".back_to_main_page");
	
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
	private static final int BUTTON_TEX_X2_START = 0;
	private static final int BUTTON_TEX_Y2_START = 208;
	
	private final ItemStack whistleItem = new ItemStack(ItemInit.WHISTLE.get());
	
	private final Map<CombatMode, HoldSelectImageButton> modeMap = new HashMap<>();
	private final Map<FormationCategory, HoldSelectImageButton> typeMap = new HashMap<>();
	private final Map<FormationCategory, Map<UnitFormationType<?>, HoldSelectImageButton>> selectedTypes = new HashMap<>();
	private final Map<FormationCategory, Map<FormationAttackType, HoldSelectImageButton>> attackTypes = new HashMap<>();
	
	private IScreenPage mainPage;
	private final Map<FormationCategory, IScreenPage> formationTypePages = new HashMap<>();
	private final Map<FormationCategory, IScreenPage> attackTypePages = new HashMap<>();
	private IScreenPage currentPage;
	
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
		this.modeMap.clear();
		
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
				(button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslationTextComponent(COMBAT_MODE_KEY + CombatMode.ATTACK.toString()), mouseX, mouseY),
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
				(button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslationTextComponent(COMBAT_MODE_KEY + CombatMode.DEFEND.toString()), mouseX, mouseY),
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
				(button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslationTextComponent(COMBAT_MODE_KEY + CombatMode.STAND_GROUND.toString()), mouseX, mouseY),
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
				(button, stack, mouseX, mouseY) -> this.renderTooltip(stack, new TranslationTextComponent(COMBAT_MODE_KEY + CombatMode.DONT_ATTACK.toString()), mouseX, mouseY),
				StringTextComponent.EMPTY));
		
		/* Formation buttons */
		this.typeMap.clear();
		
		this.typeMap.put(FormationCategory.LINE, new HoldSelectImageButton(
				this.leftPos + GRID_START_X,
				this.topPos + GRID_START_Y + BUTTON_Y_DIST,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH * 4,
				BUTTON_TEX_Y_START,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedCategory(FormationCategory.LINE),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		this.typeMap.put(FormationCategory.COLUMN, new HoldSelectImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST,
				this.topPos + GRID_START_Y + BUTTON_Y_DIST,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START,
				BUTTON_TEX_Y_START + BUTTON_HEIGHT * 3,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedCategory(FormationCategory.COLUMN),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		Button stopActionButton = new ImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST * 6,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X2_START,
				BUTTON_TEX_Y2_START,
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
		};
		
		Button openFormationTypesButton = new ImageButton(
				this.leftPos + GRID_START_X,
				this.topPos + GRID_START_Y + BUTTON_Y_DIST * 2,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X2_START + BUTTON_WIDTH,
				BUTTON_TEX_Y2_START,
				BUTTON_HEIGHT,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setCurrentPage(this.formationTypePages.get(this.menu.getCategory())),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY);
		
		Button openAttackTypesButton = new ImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST,
				this.topPos + GRID_START_Y + BUTTON_Y_DIST * 2,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X2_START + BUTTON_WIDTH * 2,
				BUTTON_TEX_Y2_START,
				BUTTON_HEIGHT,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setCurrentPage(this.attackTypePages.get(this.menu.getCategory())),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY);
		
		Button nextIntervalButton = new ImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST * 6,
				this.topPos + GRID_START_Y + BUTTON_Y_DIST * 1,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X2_START + BUTTON_WIDTH * 5,
				BUTTON_TEX_Y2_START,
				BUTTON_HEIGHT,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				this::nextInterval,
				this::renderIntervalTooltip,
				StringTextComponent.EMPTY);
		
		this.mainPage =
				IScreenPage.builder(new BaseScreenPage(this))
				.add(p -> new WidgetCollectionDecorator(p, Util.make(new ArrayList<>(), list -> {
					list.addAll(this.modeMap.values());
					list.addAll(this.typeMap.values());
					list.add(stopActionButton);
					list.add(openFormationTypesButton);
					list.add(openAttackTypesButton);
					list.add(nextIntervalButton);
				})))
				.build();
		
		Button backToMainPageButton = new ImageButton(
				this.leftPos + GRID_START_X,
				this.topPos + GRID_START_Y + BUTTON_Y_DIST * 2,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X2_START + BUTTON_WIDTH * 3,
				BUTTON_TEX_Y2_START,
				BUTTON_HEIGHT,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setCurrentPage(this.mainPage),
				(button, stack, mouseX, mouseY) -> this.renderTooltip(stack, BACK_TO_MAIN_PAGE_TEXT, mouseX, mouseY),
				StringTextComponent.EMPTY);
		
		/* Category buttons */
		
		Map<UnitFormationType<?>, HoldSelectImageButton> lineCatButtons = new HashMap<>();
		
		lineCatButtons.put(UnitFormationTypeInit.LINE_10W3D.get(), new HoldSelectImageButton(
				this.leftPos + GRID_START_X,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH * 4,
				BUTTON_TEX_Y_START,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedTypeInCategory(FormationCategory.LINE, UnitFormationTypeInit.LINE_10W3D.get()),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		lineCatButtons.put(UnitFormationTypeInit.LINE_15W2D.get(), new HoldSelectImageButton(
				this.leftPos + GRID_START_X + BUTTON_Y_DIST,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH * 4,
				BUTTON_TEX_Y_START,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedTypeInCategory(FormationCategory.LINE, UnitFormationTypeInit.LINE_15W2D.get()),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY));
		
		this.selectedTypes.put(FormationCategory.LINE, lineCatButtons);
		
		this.formationTypePages.put(FormationCategory.LINE,
				IScreenPage.builder(new BaseScreenPage(this))
				.add(p -> new WidgetCollectionDecorator(p, Util.make(new ArrayList<>(), list -> {
					list.add(backToMainPageButton);
					list.addAll(lineCatButtons.values());
				})))
				.build());
		
		/* Attack type buttons */
		this.attackTypes.clear();
		
		Map<FormationAttackType, HoldSelectImageButton> lineAttackTypeButtons = new HashMap<>();
		
		lineAttackTypeButtons.put(FormationAttackTypeInit.FIRE_AT_WILL.get(), new HoldSelectImageButton(
				this.leftPos + GRID_START_X,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH * 4,
				BUTTON_TEX_Y_START + BUTTON_HEIGHT * 3,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedAttackTypeInCategory(FormationCategory.LINE, FormationAttackTypeInit.FIRE_AT_WILL.get()),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY
				));
		
		lineAttackTypeButtons.put(FormationAttackTypeInit.FIRE_BY_COMPANY.get(), new HoldSelectImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START,
				BUTTON_TEX_Y_START + BUTTON_HEIGHT * 6,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedAttackTypeInCategory(FormationCategory.LINE, FormationAttackTypeInit.FIRE_BY_COMPANY.get()),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY
				));
		
		lineAttackTypeButtons.put(FormationAttackTypeInit.FIRE_BY_RANK.get(), new HoldSelectImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST * 2,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH,
				BUTTON_TEX_Y_START + BUTTON_HEIGHT * 6,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedAttackTypeInCategory(FormationCategory.LINE, FormationAttackTypeInit.FIRE_BY_RANK.get()),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY
				));
		
		lineAttackTypeButtons.put(FormationAttackTypeInit.FIRE_BY_FILE.get(), new HoldSelectImageButton(
				this.leftPos + GRID_START_X + BUTTON_X_DIST * 3,
				this.topPos + GRID_START_Y,
				BUTTON_WIDTH,
				BUTTON_HEIGHT,
				BUTTON_TEX_X_START + BUTTON_WIDTH * 2,
				BUTTON_TEX_Y_START + BUTTON_HEIGHT * 6,
				WHISTLE_TAB_LOCATION,
				256,
				256,
				button -> this.setSelectedAttackTypeInCategory(FormationCategory.LINE, FormationAttackTypeInit.FIRE_BY_FILE.get()),
				Button.NO_TOOLTIP,
				StringTextComponent.EMPTY
				));
		
		this.attackTypes.put(FormationCategory.LINE, lineAttackTypeButtons);
		
		this.attackTypePages.put(FormationCategory.LINE,
				IScreenPage.builder(new BaseScreenPage(this))
				.add(p -> new WidgetCollectionDecorator(p, Util.make(new ArrayList<>(), list -> {
					list.add(backToMainPageButton);
					list.addAll(lineAttackTypeButtons.values());
				})))
				.build());
		
		this.setSelectedMode(this.menu.getMode());
		this.setSelectedCategory(this.menu.getCategory());
		this.setSelectedTypeInCategory(this.menu.getCategory(), this.menu.getFormation());
		this.setSelectedAttackTypeInCategory(this.menu.getCategory(), this.menu.getSelectedAttackType(this.menu.getCategory()));
		
		this.setCurrentPage(this.mainPage);
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
		
		if (this.currentPage != null) this.currentPage.render(stack, mouseX, mouseY, partialTicks);
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
	
	private void setSelectedCategory(FormationCategory category) {
		this.typeMap.forEach((k, v) -> v.setSelected(k == category));
		this.menu.setCategory(category);
		UnitFormationType<?> type = this.menu.getSelectedTypes(category);
		if (this.menu.getFormation() == type) return;
		this.menu.setFormation(type);
		this.menu.updateServer();
	}
	
	private void setSelectedTypeInCategory(FormationCategory category, UnitFormationType<?> type) {
		this.selectedTypes.get(category).forEach((k, v) -> v.setSelected(k == type));
		UnitFormationType<?> oldType = this.menu.getSelectedTypes(category);
		if (type == oldType) return;
		this.menu.setCategoryType(category, type);
		if (this.menu.getCategory() == category) {
			this.menu.setFormation(type);
		}
		this.menu.updateServer();
	}
	
	private void setSelectedAttackTypeInCategory(FormationCategory category, FormationAttackType type) {
		this.attackTypes.get(category).forEach((k, v) -> v.setSelected(k == type));
		FormationAttackType oldType = this.menu.getSelectedAttackType(category);
		if (type == oldType) return;
		this.menu.setCategoryAttackType(category, type);
		this.menu.updateServer();
	}
	
	private void nextInterval(Button button) {
		this.menu.setInterval(this.menu.getInterval().next());
		this.menu.updateServer();
	}
	
	private void stopWhistle(Button button) {
		IWNetwork.CHANNEL.sendToServer(new SStopAction());
	}
	
	private void setCurrentPage(IScreenPage page) {
		this.currentPage = page == null ? this.mainPage : page;
	}
	
	private void renderIntervalTooltip(Button button, MatrixStack stack, int mouseX, int mouseY) {
		this.renderComponentTooltip(stack, ImmutableList.of(INTERVAL_TEXT1, new TranslationTextComponent(INTERVAL2_KEY, this.menu.getInterval().toString())), mouseX, mouseY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.currentPage.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
}
