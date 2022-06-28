package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.WidgetUtils;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.BaseScreenPage;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.DraggableDecorator;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.DraggableDecorator.FloatPoint;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.IScreenPage;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.TextDecorator;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.WidgetCollectionDecorator;
import rbasamoyai.industrialwarfare.common.containers.DiplomacyMenu;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;

public class DiplomacyScreen extends AbstractContainerScreen<DiplomacyMenu> {

	public static final ResourceLocation DIPLOMACY_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/diplomacy.png");
	
	public static final String TRANSLATION_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".diplomacy";
	
	/* DEBUG */ public static final Component DEBUG_NPC = (new TextComponent("*DEBUG*").withStyle(ChatFormatting.DARK_RED)).append(new TextComponent(" An NPC Faction").withStyle(ChatFormatting.RESET));
	
	private static final Component DIPLOMATIC_RELATIONS_PAGE_TITLE = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".diplomatic_relations");
	private static final Component NPC_FACTION_RELATIONSHIPS_PAGE_TITLE = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".npc_faction_relationships");
	
	public static final Component THEIR_STATUS = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".their_status");
	public static final Component THEIR_STATUS_SHORT = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".their_status.short");
	public static final Component OUR_STATUS = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".our_status");
	public static final Component OUR_STATUS_SHORT = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".our_status.short");
	public static final Component LOADING_PLAYER_INFO = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".loading_player_info");
	public static final Component COULD_NOT_LOAD = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".could_not_load").withStyle(ChatFormatting.DARK_RED);
	public static final Component NO_DIPLOMACY_AVAILABLE = new TranslatableComponent(TRANSLATION_KEY_ROOT + ".no_diplomacy_available").withStyle(ChatFormatting.DARK_RED);
	
	private static final List<Component> LOADING_FRAMES =
			Arrays.asList(
					new TextComponent(""),
					new TextComponent("."),
					new TextComponent(".."),
					new TextComponent("...")
					);
	
	public static final Map<DiplomaticStatus, Component> STATUSES =
			Arrays.stream(DiplomaticStatus.values())
					.collect(Collectors.toMap(ds -> ds, ds -> {
						return new TranslatableComponent(TRANSLATION_KEY_ROOT + "." + ds.getName());
					}));
	
	public static final Map<DiplomaticStatus, Component> STATUSES_COLORED =
			STATUSES
					.entrySet()
					.stream()
					.collect(Collectors.toMap(Entry::getKey, e -> {
						return e.getValue().copy().withStyle(e.getKey().getStyle());
					}));
	
	public static final Map<DiplomaticStatus, Component> OUR_STATUSES =
			STATUSES_COLORED
					.entrySet()
					.stream()
					.collect(Collectors.toMap(Entry::getKey, e -> {
						return OUR_STATUS.copy().append(": ").append(e.getValue());
					}));
	
	public static final Map<DiplomaticStatus, Component> THEIR_STATUSES =
			STATUSES
					.entrySet()
					.stream()
					.collect(Collectors.toMap(Entry::getKey, e -> {
						return THEIR_STATUS.copy().append(": ").append(e.getValue());
					}));
	
	public static final Map<DiplomaticStatus, Component> THEIR_STATUSES_COLORED =
			STATUSES_COLORED
					.entrySet()
					.stream()
					.collect(Collectors.toMap(Entry::getKey, e -> {
						return THEIR_STATUS.copy().append(": ").append(e.getValue());
					}));
	
	private static final int PAGE_BUTTON_WIDTH = 7;
	private static final int PAGE_BUTTON_HEIGHT = 11;
	private static final int PAGE_BUTTON_NEXT_GUI_X = 224;
	private static final int PAGE_BUTTON_PREV_GUI_X = 7;
	private static final int PAGE_BUTTON_GUI_Y = 20;
	private static final int PAGE_BUTTON_TEX_X = 238;
	private static final int PAGE_BUTTON_NEXT_TEX_Y = 72;
	private static final int PAGE_BUTTON_PREV_TEX_Y = PAGE_BUTTON_NEXT_TEX_Y + PAGE_BUTTON_HEIGHT * 2;
	
	private static final int SCROLL_BAR_WIDGET_TEX_X = 238;
	private static final int SCROLL_BAR_WIDGET_TEX_Y = 0;
	private static final int SCROLL_BAR_WIDGET_WIDTH = 12;
	private static final int SCROLL_BAR_WIDGET_HEIGHT = 15;
	private static final int SCROLL_BAR_WIDGET_GUI_START_X = 224;
	private static final int SCROLL_BAR_WIDGET_GUI_START_Y = 53;
	
	private static final int SCROLL_BAR_HEIGHT = 96;
	private static final int SCROLL_BAR_DRAG_HEIGHT = SCROLL_BAR_HEIGHT - SCROLL_BAR_WIDGET_HEIGHT;
	
	private static final int LIST_X = 8;
	private static final int LIST_Y = 34;
	private static final int LIST_ROWS = 8;
	
	private static final int INFO_X = 7;
	private static final int INFO_Y = 148;
	
	private static final float PAGE_TITLE_Y = 22.0f;
	
	private static final int TEXT_COLOR = 4210752;
	private static final int PAGE_TITLE_COLOR = -1;
	
	private static final int REFRESH_AFTER = 20;
	
	private final List<IScreenPage> pages = new ArrayList<>(2);
	
	private final Map<PlayerIDTag, DiplomaticStatus> statusBuffer = new HashMap<>();
	
	private int page = 0;
	
	private float scrollOutput = 0.0f;
	
	private DraggableDecorator drpScrollbar;
	private WidgetCollectionDecorator drpStatuses;
	private DiplomacyStatusesListDecorator drpList;
	private DiplomacyStatusInfoDecorator drpInfo;
	
	private Button prevPageButton;
	private Button nextPageButton;
	
	private int ticks = REFRESH_AFTER;
	
	public DiplomacyScreen(DiplomacyMenu container, Inventory playerInv, Component title) {
		super(container, playerInv, title);
		
		this.imageWidth = 238;
		this.imageHeight = 223;
		
		this.titleLabelX = this.imageWidth / 2;
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.prevPageButton = this.addRenderableWidget(new ImageButton(
				this.leftPos + PAGE_BUTTON_PREV_GUI_X,
				this.topPos + PAGE_BUTTON_GUI_Y,
				PAGE_BUTTON_WIDTH,
				PAGE_BUTTON_HEIGHT,
				PAGE_BUTTON_TEX_X,
				PAGE_BUTTON_PREV_TEX_Y,
				PAGE_BUTTON_HEIGHT,
				DIPLOMACY_GUI,
				this::prevPage));
		
		this.nextPageButton = this.addRenderableWidget(new ImageButton(
				this.leftPos + PAGE_BUTTON_NEXT_GUI_X,
				this.topPos + PAGE_BUTTON_GUI_Y,
				PAGE_BUTTON_WIDTH,
				PAGE_BUTTON_HEIGHT,
				PAGE_BUTTON_TEX_X,
				PAGE_BUTTON_NEXT_TEX_Y,
				PAGE_BUTTON_HEIGHT,
				DIPLOMACY_GUI,
				this::nextPage));
		
		this.pages.clear();
		
		IScreenPage diplomaticRelationsPage =
				IScreenPage.builder(new BaseScreenPage(this))
				.add(page -> new TextDecorator(page, DIPLOMATIC_RELATIONS_PAGE_TITLE, this.titleLabelX, PAGE_TITLE_Y, true, true, PAGE_TITLE_COLOR))
				.build();
		
		DraggableDecorator.Properties drpScrollBar$properties =
				((DraggableDecorator.Properties) new DraggableDecorator.Properties()
				.startingOutput(new DraggableDecorator.FloatPoint(0.0f, this.scrollOutput))
				.dragDimensions(new Point(0, SCROLL_BAR_DRAG_HEIGHT))
				.startingPoint(new Point(SCROLL_BAR_WIDGET_GUI_START_X, SCROLL_BAR_WIDGET_GUI_START_Y))
				.dragged(this::setDrpListTopIndex)
				.texturePos(new Point(SCROLL_BAR_WIDGET_TEX_X, SCROLL_BAR_WIDGET_TEX_Y))
				.textureDimensions(new Point(SCROLL_BAR_WIDGET_WIDTH, SCROLL_BAR_WIDGET_HEIGHT))
				.textureLocation(DIPLOMACY_GUI));
		this.drpScrollbar = new DraggableDecorator(diplomaticRelationsPage, this, drpScrollBar$properties);
		this.drpStatuses = new WidgetCollectionDecorator(this.drpScrollbar, new ArrayList<>());
		this.drpList = new DiplomacyStatusesListDecorator(
				this.drpStatuses,
				LIST_ROWS,
				LIST_X, LIST_Y,
				this::setInfoDecoratorData,
				this::clearChanges,
				this::commitChanges,
				this::onKeyScroll);
		this.drpInfo = new DiplomacyStatusInfoDecorator(this.drpList, INFO_X, INFO_Y, this::dsiCallbackToDrp, this::dsbDisplay, this::dsbPressable);
		
		diplomaticRelationsPage = this.drpInfo;
		
		this.pages.add(diplomaticRelationsPage);
		
		IScreenPage npcFactionRelationshipsPage =
				IScreenPage.builder(new BaseScreenPage(this))
				.add(page -> new TextDecorator(page, NPC_FACTION_RELATIONSHIPS_PAGE_TITLE, this.titleLabelX, PAGE_TITLE_Y, true, true, PAGE_TITLE_COLOR))
				.build();
		
		this.pages.add(npcFactionRelationshipsPage);
		
		this.updatePageButtons();
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);		
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, DIPLOMACY_GUI);
		
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		stack.pushPose();
		stack.translate(this.leftPos, this.topPos, 0);
		this.getCurrentPage().render(stack, mouseX - this.leftPos, mouseY - this.topPos, partialTicks);
		stack.popPose();
	}
	
	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
		this.font.draw(stack, this.title, this.titleLabelX - (float) this.font.width(this.title) * 0.5f, this.titleLabelY, TEXT_COLOR);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.getCurrentPage().mouseClicked(this.offsetMouseX(mouseX), this.offsetMouseY(mouseY), button)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.getCurrentPage().mouseReleased(this.offsetMouseX(mouseX), this.offsetMouseY(mouseY), button)) {
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX1, double mouseY1, int button, double mouseX2, double mouseY2) {
		if (this.getCurrentPage().mouseDragged(this.offsetMouseX(mouseX1), this.offsetMouseY(mouseY1), button, this.offsetMouseX(mouseX2), this.offsetMouseY(mouseY2))) {
			return true;
		}
		return super.mouseDragged(mouseX1, mouseY1, button, mouseX2, mouseY2);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifier) {
		if (this.getCurrentPage().keyPressed(keyCode, scanCode, modifier)) {
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifier);
	}
	
	@Override
	public void containerTick() {
		if (++this.ticks >= REFRESH_AFTER) {
			IWNetwork.CHANNEL.sendToServer(new DiplomacyScreenMessages.SRequestUpdate());
			if (this.menu.isDirty() && this.drpList != null) {
				this.drpList.sortUsing(DiplomacyListComparator.noSort());
				this.menu.setDirty(false);
			}
			this.ticks = 0;
		}
		this.getCurrentPage().tick();
		this.drpScrollbar.setActive(this.drpList == null ? false : this.drpList.getSize() > LIST_ROWS);
		super.containerTick();
	}
	
	@Override
	public void onClose() {
		super.onClose();
	}
	
	private double offsetMouseX(double mouseX) {
		return mouseX - (double) this.leftPos;
	}
	
	private double offsetMouseY(double mouseY) {
		return mouseY - (double) this.topPos;
	}
	
	private void prevPage(Button button) {
		this.changePage(this.page > 0, -1);
	}
	
	private void nextPage(Button button) {
		this.changePage(this.page + 1 < this.pages.size(), 1);
	}
	
	private IScreenPage getCurrentPage() {
		return this.page < this.pages.size() ? this.pages.get(this.page) : new BaseScreenPage(this);
	}
	
	private void changePage(boolean shouldChange, int modifier) {
		if (this.validatePage() && shouldChange) this.page += modifier;
		this.updatePageButtons();
	}
	
	private boolean validatePage() {
		if (this.page < 0 || this.page >= this.pages.size()) {
			IndustrialWarfare.LOGGER.warn("An opened DiplomacyScreen was on page {}. This is an invalid page, and the screen will be set to page 0. Look into it, will ya?", this.page);
			this.page = 0;
			return false;
		}
		return true;
	}
	
	private void updatePageButtons() {
		WidgetUtils.setActiveAndVisible(this.prevPageButton, this.page > 0);
		WidgetUtils.setActiveAndVisible(this.nextPageButton, this.page + 1 < this.pages.size());
	}
	
	private void setInfoDecoratorData(PlayerIDTag tag) {
		this.drpInfo.setTag(tag);
	}
	
	public static Component getLoadingText(int frame) {
		return LOADING_PLAYER_INFO.copy().append(LOADING_FRAMES.get(frame));
	}
	
	private void setDrpListTopIndex(float ox, float oy) {
		if (this.drpList == null) return;
		float size = Math.max((float) this.drpList.getSize() - 8.0f, 0.0f);
		int index = Mth.floor(oy * size);
		this.drpList.setTopIndex(index);
	}
	
	private void onKeyScroll(int newTopIndex) {
		if (this.drpList.getSize() - LIST_ROWS <= 0) {
			this.drpScrollbar.setOutput(new FloatPoint(0.0f, 0.0f));
			return;
		}
		
		float oy = (float) newTopIndex / (float)(this.drpList.getSize() - LIST_ROWS);
		this.drpScrollbar.setOutput(new FloatPoint(0.0f, oy));
	}
	
	private void clearChanges(Button button) {
		this.statusBuffer.clear();
	}
	
	private void commitChanges(Button button) {
		if (this.statusBuffer.isEmpty()) return;
		IWNetwork.CHANNEL.sendToServer(new DiplomacyScreenMessages.SDiplomaticStatusChangeSync(this.statusBuffer));
		this.statusBuffer.clear();
	}
	
	private void dsiCallbackToDrp(Button button) {
		this.drpList.setButtonsActive(true);
	}
	
	private DiplomacyStatusButton.OnDisplay dsbDisplay(DiplomaticStatus status) {
		return tag -> {
			Pair<DiplomaticStatus, DiplomaticStatus> statuses = this.menu.getDiplomaticStatuses().get(tag);
			if (statuses == null) {
				return DiplomacyStatusButton.DisplayType.NOT_SELECTED;
			}
			boolean sameAsOldStatus = statuses.getSecond() == status;
			boolean selected = this.statusBuffer.get(tag) == status;
			if (sameAsOldStatus) {
				return this.statusBuffer.containsKey(tag) ? DiplomacyStatusButton.DisplayType.PREVIOUS : DiplomacyStatusButton.DisplayType.SELECTED;
			}
			return selected ? DiplomacyStatusButton.DisplayType.SELECTED : DiplomacyStatusButton.DisplayType.NOT_SELECTED;
		};
	}
	
	private DiplomacyStatusButton.OnPress dsbPressable(DiplomaticStatus status) {
		return tag -> {
			Pair<DiplomaticStatus, DiplomaticStatus> statuses = this.menu.getDiplomaticStatuses().get(tag);
			if (statuses == null) return;
			if (statuses.getSecond() == status) {
				this.statusBuffer.remove(tag);
			} else {
				this.statusBuffer.put(tag, status);
			}
		};
	}
			
}
