package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.BaseScreenPage;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.DraggableDecorator;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.IScreenPage;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.TextDecorator;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.WidgetCollectionDecorator;
import rbasamoyai.industrialwarfare.common.containers.DiplomacyContainer;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.DiplomacyScreenMessages;
import rbasamoyai.industrialwarfare.utils.WidgetUtils;

public class DiplomacyScreen extends ContainerScreen<DiplomacyContainer> {

	public static final ResourceLocation DIPLOMACY_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/diplomacy.png");
	
	private static final String TRANSLATION_ROOT_KEY = "gui." + IndustrialWarfare.MOD_ID + ".diplomacy";
	
	private static final ITextComponent DIPLOMATIC_RELATIONS_PAGE_TITLE = new TranslationTextComponent(TRANSLATION_ROOT_KEY + ".diplomatic_relations");
	private static final ITextComponent NPC_FACTION_RELATIONSHIPS_PAGE_TITLE = new TranslationTextComponent(TRANSLATION_ROOT_KEY + ".npc_faction_relationships");
	
	private static final int PAGE_BUTTON_WIDTH = 7;
	private static final int PAGE_BUTTON_HEIGHT = 11;
	private static final int PAGE_BUTTON_NEXT_GUI_X = 224;
	private static final int PAGE_BUTTON_PREV_GUI_X = 7;
	private static final int PAGE_BUTTON_GUI_Y = 20;
	private static final int PAGE_BUTTON_TEX_X = 238;
	private static final int PAGE_BUTTON_NEXT_TEX_Y = 64;
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
	
	private static final float PAGE_TITLE_Y = 22.0f;
	
	private static final int TEXT_COLOR = 4210752;
	private static final int PAGE_TITLE_COLOR = -1;
	
	private static final int REFRESH_AFTER = 20;
	
	private final List<IScreenPage> pages = new ArrayList<>(2);
	
	private int page = 0;
	
	private float scrollOutput = 0.0f;
	
	private DraggableDecorator drpScrollbar;
	private WidgetCollectionDecorator drpStatuses;
	private DiplomacyStatusesListDecorator drpList;
	
	private Button prevPageButton;
	private Button nextPageButton;
	
	private int ticks = 0;
	
	public DiplomacyScreen(DiplomacyContainer container, PlayerInventory playerInv, ITextComponent title) {
		super(container, playerInv, title);
		
		this.imageWidth = 238;
		this.imageHeight = 223;
		
		this.titleLabelX = this.imageWidth / 2;
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.prevPageButton = this.addButton(new ImageButton(
				this.leftPos + PAGE_BUTTON_PREV_GUI_X,
				this.topPos + PAGE_BUTTON_GUI_Y,
				PAGE_BUTTON_WIDTH,
				PAGE_BUTTON_HEIGHT,
				PAGE_BUTTON_TEX_X,
				PAGE_BUTTON_PREV_TEX_Y,
				PAGE_BUTTON_HEIGHT,
				DIPLOMACY_GUI,
				this::prevPage));
		
		this.nextPageButton = this.addButton(new ImageButton(
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
				.texturePos(new Point(SCROLL_BAR_WIDGET_TEX_X, SCROLL_BAR_WIDGET_TEX_Y))
				.textureDimensions(new Point(SCROLL_BAR_WIDGET_WIDTH, SCROLL_BAR_WIDGET_HEIGHT))
				.textureLocation(DIPLOMACY_GUI));
		this.drpScrollbar = new DraggableDecorator(diplomaticRelationsPage, this, drpScrollBar$properties);
		this.drpStatuses = new WidgetCollectionDecorator(drpScrollbar, new Widget[] {});
		this.drpList = new DiplomacyStatusesListDecorator(drpStatuses, LIST_ROWS, LIST_X, LIST_Y);
		
		diplomaticRelationsPage = this.drpList;
		
		this.pages.add(diplomaticRelationsPage);
		
		IScreenPage npcFactionRelationshipsPage =
				IScreenPage.builder(new BaseScreenPage(this))
				.add(page -> new TextDecorator(page, NPC_FACTION_RELATIONSHIPS_PAGE_TITLE, this.titleLabelX, PAGE_TITLE_Y, true, true, PAGE_TITLE_COLOR))
				.build();
		
		this.pages.add(npcFactionRelationshipsPage);
		
		this.updatePageButtons();
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);		
		super.render(stack, mouseX, mouseY, partialTicks);
		this.renderTooltip(stack, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		TextureManager texManager = this.minecraft.textureManager;
		texManager.bind(DIPLOMACY_GUI);
		
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		stack.pushPose();
		stack.translate(this.leftPos, this.topPos, 0);
		this.getCurrentPage().render(stack, mouseX - this.leftPos, mouseY - this.topPos, partialTicks);
		stack.popPose();
	}
	
	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
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
	public void tick() {
		if (++this.ticks >= REFRESH_AFTER) {
			IWNetwork.CHANNEL.sendToServer(new DiplomacyScreenMessages.SRequestUpdate());
			this.ticks = 0;
		}
		super.tick();
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
	
}
