package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.TaskCommandArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.core.IWModRegistries;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.STaskScrollSyncMessage;
import rbasamoyai.industrialwarfare.utils.TextureUtils;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;
import rbasamoyai.industrialwarfare.utils.WidgetUtils;

/**
 * A lot of scrolling-related code here is based on {@link net.minecraft.client.gui.screen.inventory.CreativeScreen}.
 * 
 * @author rbasmaoyai
 */
public class TaskScrollScreen extends ContainerScreen<TaskScrollContainer> {

	private static final ResourceLocation TASK_SCROLL_SCREEN_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/task_scroll.png");
	
	private static final String TASK_SCROLL_SCREEN_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".task_scroll";
	private static final IFormattableTextComponent ADD_ORDER_BUTTON_TEXT = new TranslationTextComponent(TASK_SCROLL_SCREEN_KEY_ROOT + ".add_order");
	private static final IFormattableTextComponent TOOLTIP_REMOVE_ORDER = new TranslationTextComponent(TASK_SCROLL_SCREEN_KEY_ROOT + ".remove_order");
	private static final IFormattableTextComponent TOOLTIP_INSERT_ORDER = new TranslationTextComponent(TASK_SCROLL_SCREEN_KEY_ROOT + ".insert_order");
	private static final IFormattableTextComponent ATTACH_LABEL_TEXT = new TranslationTextComponent(TASK_SCROLL_SCREEN_KEY_ROOT + ".attach_label");
	
	private static final int ARG_COLUMNS = 2;
	private static final int TEXTURE_SIZE = 256;
	
	private static final int LIST_WIDGET_START_X = 24;
	private static final int LIST_WIDGET_START_Y = 21;
	private static final int LIST_WIDGET_SPACING = 2; // Only describes the spacing between adjacent widget edges, not the spacing between the top left corners.
	private static final int LIST_WIDGET_WIDTH = 68;
	private static final int LIST_WIDGET_HEIGHT = 18;
	
	private static final int COLUMN_SPACING = LIST_WIDGET_WIDTH + LIST_WIDGET_SPACING;
	private static final int ROW_SPACING = LIST_WIDGET_HEIGHT + LIST_WIDGET_SPACING;
	
	private static final int ITEM_ARG_WIDGET_TITLE_START_X = LIST_WIDGET_START_X + 20;
	private static final int ITEM_ARG_WIDGET_TITLE_START_Y = LIST_WIDGET_START_Y + 5;
	private static final int ITEM_ARG_WIDGET_TITLE_WIDTH = LIST_WIDGET_WIDTH - 20;
	
	private static final int PAGE_NEXT_BUTTON_GUI_X = 115;
	private static final int PAGE_PREV_BUTTON_GUI_X = 45;
	private static final int PAGE_BUTTON_GUI_Y = 126;
	private static final int PAGE_BUTTON_TEX_X = 176;
	private static final int PAGE_NEXT_BUTTON_TEX_Y = 18;
	private static final int PAGE_PREV_BUTTON_TEX_Y = 42;
	private static final int PAGE_BUTTON_WIDTH = 16;
	private static final int PAGE_BUTTON_HEIGHT = 12;
	
	private static final int SLOT_TEX_X = 176;
	private static final int SLOT_TEX_Y = 0;
	private static final int SLOT_TEX_WIDTH = 18;
	
	private static final int LIST_INDEX_RIGHT_X = 22;
	private static final int LIST_INDEX_START_Y = 25;
	private static final int PAGE_NUMBER_Y = 129;
	
	private static final int SCROLL_BAR_BG_GUI_X = 165;
	private static final int SCROLL_BAR_BG_GUI_Y = 22;
	private static final int SCROLL_BAR_BG_GUI_HEIGHT = 96;
	private static final int SCROLL_BAR_BUTTON_TEX_X = 176;
	private static final int SCROLL_BAR_BUTTON_TEX_Y = 90;
	private static final int SCROLL_BAR_BUTTON_HEIGHT = 11;
	private static final int SCROLL_BAR_WIDTH = 7;
	private static final int SCROLL_BAR_BG_EFFECTIVE_HEIGHT = SCROLL_BAR_BG_GUI_HEIGHT - SCROLL_BAR_BUTTON_HEIGHT;
	
	private static final int ADD_ORDER_BUTTON_X = 24;
	private static final int ADD_ORDER_BUTTON_START_Y = 21;
	
	private static final int ORDER_OPERATION_BUTTONS_GUI_X = 15;
	private static final int ORDER_OPERATION_BUTTONS_GUI_START_Y = 22;
	private static final int ORDER_OPERATION_BUTTONS_WIDTH = 8;
	private static final int ORDER_OPERATION_BUTTONS_TEX_START_X = 176;
	private static final int ORDER_OPERATION_BUTTONS_TEX_START_Y = 66;
	
	private static final int ROW_INDEX_WIDGET_X = 6;
	
	private static final int ATTACH_LABEL_X = 176;
	private static final int ATTACH_LABEL_Y = 0;
	private static final int ATTACH_LABEL_BASE_LENGTH = SLOT_TEX_WIDTH + 13;
	private static final int ATTACH_LABEL_HEIGHT = 24;
	private static final int ATTACH_LABEL_SLOT_X = ATTACH_LABEL_X + 3;
	private static final int ATTACH_LABEL_SLOT_Y = ATTACH_LABEL_Y + 3;
	
	private static final int ATTACH_LABEL_TEXT_X = ATTACH_LABEL_SLOT_X + SLOT_TEX_WIDTH + 4;
	private static final int ATTACH_LABEL_TEXT_Y = ATTACH_LABEL_SLOT_Y + 5;
	
	private static final int BORDER_START_COLOR_X = 176;
	private static final int BORDER_END_COLOR_X = 177;
	private static final int FIELD_COLOR_X = 178;
	private static final int COLORS_Y = 112;
	private static final List<Pair<Integer, Integer>> COLOR_COORDS = Arrays.asList(
			Pair.of(BORDER_START_COLOR_X, COLORS_Y),
			Pair.of(BORDER_END_COLOR_X, COLORS_Y),
			Pair.of(FIELD_COLOR_X, COLORS_Y)
			);
	
	private static final int TEXT_COLOR = 4210752;
	private static final int ATTACH_LABEL_TEXT_COLOR = 8350000;
	
	private int page = 0;
	private int lastPage = 0;
	private final List<TaskScrollCommand> validCmds;
	private float scrollOffs = 0;
	private boolean isScrolling = false;
	private int hoveringOverRowIndex = -1;
	
	private final int labelBorderStartColor;
	private final int labelBorderEndColor;
	private final int labelFieldColor;
	
	private Button prevPageButton;
	private Button nextPageButton;
	private Button addOrderButton;
	private Button removeOrderButton;
	private Button insertOrderButton;
	
	private final TaskScrollArgSelectorWidget[][] asWidgetArray = new TaskScrollArgSelectorWidget[TaskScrollContainer.ROW_COUNT][ARG_COLUMNS];
	private final RowIndexWidget[] riWidgetArray = new RowIndexWidget[TaskScrollContainer.ROW_COUNT];
	private final ItemArgWidget[][] iaWidgetArray = new ItemArgWidget[TaskScrollContainer.ROW_COUNT][ARG_COLUMNS];
	
	public TaskScrollScreen(TaskScrollContainer container, PlayerInventory playerInv, ITextComponent localTitle) {
		super(container, playerInv, localTitle);
		
		this.imageWidth = 176;
		this.imageHeight = 240;
		this.titleLabelY = 5;
		this.inventoryLabelY = this.imageHeight - 94;
		
		this.validCmds = this.menu.getCommands();
		
		List<Integer> colors = TextureUtils.getColors(TASK_SCROLL_SCREEN_GUI, COLOR_COORDS);
		this.labelBorderStartColor = colors.get(0);
		this.labelBorderEndColor = colors.get(1);
		this.labelFieldColor = colors.get(2);
	}
	
	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		
		this.prevPageButton = this.addButton(new ImageButton(
				this.leftPos + PAGE_PREV_BUTTON_GUI_X,
				this.topPos + PAGE_BUTTON_GUI_Y,
				PAGE_BUTTON_WIDTH,
				PAGE_BUTTON_HEIGHT,
				PAGE_BUTTON_TEX_X,
				PAGE_PREV_BUTTON_TEX_Y,
				PAGE_BUTTON_HEIGHT,
				TASK_SCROLL_SCREEN_GUI,
				this::prevPage
				));
		
		this.nextPageButton = this.addButton(new ImageButton(
				this.leftPos + PAGE_NEXT_BUTTON_GUI_X,
				this.topPos + PAGE_BUTTON_GUI_Y,
				PAGE_BUTTON_WIDTH,
				PAGE_BUTTON_HEIGHT,
				PAGE_BUTTON_TEX_X,
				PAGE_NEXT_BUTTON_TEX_Y,
				PAGE_BUTTON_HEIGHT,
				TASK_SCROLL_SCREEN_GUI,
				this::nextPage
				));
		
		this.addOrderButton = this.addButton(new Button(
				this.leftPos + ADD_ORDER_BUTTON_X,
				this.topPos + ADD_ORDER_BUTTON_START_Y,
				this.font.width(ADD_ORDER_BUTTON_TEXT) + 8,
				ROW_SPACING,
				ADD_ORDER_BUTTON_TEXT,
				this::addOrder
				));
		
		Button.ITooltip removeOrderButton$tooltip = (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, TOOLTIP_REMOVE_ORDER, mouseX, mouseY);
		
		this.removeOrderButton = this.addButton(new DeactivatableImageButton(
				this.leftPos + ORDER_OPERATION_BUTTONS_GUI_X,
				this.topPos + ORDER_OPERATION_BUTTONS_GUI_START_Y,
				ORDER_OPERATION_BUTTONS_WIDTH,
				ORDER_OPERATION_BUTTONS_WIDTH,
				ORDER_OPERATION_BUTTONS_TEX_START_X,
				ORDER_OPERATION_BUTTONS_TEX_START_Y,
				TASK_SCROLL_SCREEN_GUI,
				TEXTURE_SIZE,
				TEXTURE_SIZE,
				this::removeOrder,
				removeOrderButton$tooltip,
				TOOLTIP_REMOVE_ORDER
				));
		WidgetUtils.setActiveAndVisible(this.removeOrderButton, false);
		
		Button.ITooltip insertOrderButton$tooltip = (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, TOOLTIP_INSERT_ORDER, mouseX, mouseY);
		
		this.insertOrderButton = this.addButton(new DeactivatableImageButton(
				this.leftPos + ORDER_OPERATION_BUTTONS_GUI_X,
				this.topPos + ORDER_OPERATION_BUTTONS_GUI_START_Y + ORDER_OPERATION_BUTTONS_WIDTH,
				ORDER_OPERATION_BUTTONS_WIDTH,
				ORDER_OPERATION_BUTTONS_WIDTH,
				ORDER_OPERATION_BUTTONS_TEX_START_X + ORDER_OPERATION_BUTTONS_WIDTH,
				ORDER_OPERATION_BUTTONS_TEX_START_Y,
				TASK_SCROLL_SCREEN_GUI,
				TEXTURE_SIZE,
				TEXTURE_SIZE,
				this::insertOrder,
				insertOrderButton$tooltip,
				TOOLTIP_INSERT_ORDER
				));
		WidgetUtils.setActiveAndVisible(this.insertOrderButton, false);
		
		int topIndex = this.menu.getTopIndex();
		int baseArgIndex = this.page * 2 - 1;
		
		for (int i = 0; i < this.asWidgetArray.length; i++) {
			for (int j = 0; j < this.asWidgetArray[i].length; j++) {
				int x = this.leftPos + LIST_WIDGET_START_X + COLUMN_SPACING * j;
				int y = this.topPos + LIST_WIDGET_START_Y + ROW_SPACING * i;
				Optional<TaskScrollOrder> optional = this.menu.getOrder(topIndex + i);	
				Optional<ArgSelector<?>> selector;
				if (optional.isPresent()) {
					TaskScrollOrder order = optional.get();
					int argIndex = baseArgIndex + j;
					if (argIndex == -1) {
						selector = Optional.of(new TaskCommandArgSelector(this.validCmds, order));
					} else {
						selector = order.getArgHolder(argIndex).getSelector(this.menu);
					}
				} else {
					selector = Optional.empty();
				}
					
				this.asWidgetArray[i][j] = this.addWidget(new TaskScrollArgSelectorWidget(this.minecraft, this, x, y, LIST_WIDGET_WIDTH, selector));
			}
		}
		
		for (int i = 0; i < this.iaWidgetArray.length; i++) {
			for (int j = 0; j < this.iaWidgetArray[i].length; j++) {
				int x = this.leftPos + LIST_WIDGET_START_X + COLUMN_SPACING * j + 1;
				int y = this.topPos + LIST_WIDGET_START_Y + ROW_SPACING * i + 1;
				int orderIndex = topIndex + i;
				int argIndex = j - 1;
				this.iaWidgetArray[i][j] = this.addWidget(new ItemArgWidget(x, y, orderIndex, argIndex, this.menu.getOrderList(), this.itemRenderer));
			}
		}
		
		for (int i = 0; i < this.riWidgetArray.length; i++) {
			int y = this.topPos + LIST_WIDGET_START_Y + ROW_SPACING * i;
			this.riWidgetArray[i] = this.addWidget(new RowIndexWidget(this.leftPos + ROW_INDEX_WIDGET_X, y, LIST_WIDGET_HEIGHT, LIST_WIDGET_HEIGHT, i));
		}
		
		this.updateScreen();
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
		TextureManager texManager = this.minecraft.getTextureManager();
		texManager.bind(TASK_SCROLL_SCREEN_GUI);
		
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		int scrollBarTexYDiff = this.canScroll() ? 0 : SCROLL_BAR_BUTTON_HEIGHT ;
		int scrollBarY = this.topPos + SCROLL_BAR_BG_GUI_Y + MathHelper.floor((float) SCROLL_BAR_BG_EFFECTIVE_HEIGHT * this.scrollOffs);
		this.blit(stack, this.leftPos + SCROLL_BAR_BG_GUI_X, scrollBarY, SCROLL_BAR_BUTTON_TEX_X, SCROLL_BAR_BUTTON_TEX_Y + scrollBarTexYDiff, SCROLL_BAR_WIDTH, SCROLL_BAR_BUTTON_HEIGHT);
		
		// Rendering attach label
		int width = ATTACH_LABEL_BASE_LENGTH + this.font.width(ATTACH_LABEL_TEXT);
		this.fillGradient(stack, this.leftPos + ATTACH_LABEL_X, this.topPos + ATTACH_LABEL_Y, this.leftPos + ATTACH_LABEL_X + width, this.topPos + ATTACH_LABEL_Y + ATTACH_LABEL_HEIGHT, this.labelBorderStartColor, this.labelBorderEndColor);
		this.fillGradient(stack, this.leftPos + ATTACH_LABEL_X + 1, this.topPos + ATTACH_LABEL_Y + 1, this.leftPos + ATTACH_LABEL_X + width - 1, this.topPos + ATTACH_LABEL_Y + ATTACH_LABEL_HEIGHT - 1, this.labelFieldColor, this.labelFieldColor);
		this.blit(stack, this.leftPos + ATTACH_LABEL_SLOT_X, this.topPos + ATTACH_LABEL_SLOT_Y, SLOT_TEX_X, SLOT_TEX_Y, SLOT_TEX_WIDTH, SLOT_TEX_WIDTH);
		
		// Could combine asWidgetArray and iaWidgetArray, but not now
		for (int i = 0; i < this.asWidgetArray.length; i++) {
			for (int j = 0; j < this.asWidgetArray[i].length; j++) {
				this.asWidgetArray[i][j].render(stack, mouseX, mouseY, partialTicks);
			}
		}
		
		// ArgSelectorWidget binds to the widgets texture, so need to bind back
		texManager.bind(TASK_SCROLL_SCREEN_GUI);
		
		int topIndex = this.menu.getTopIndex();
		int baseArgIndex = this.page * 2 -1;
		for (int i = 0; i < this.iaWidgetArray.length; i++) {
			for (int j = 0; j < this.iaWidgetArray[i].length; j++) {
				Optional<TaskScrollOrder> optional = this.menu.getOrder(topIndex + i);
				if (optional.isPresent()) {
					IArgHolder holder = optional.get().getArgHolder(baseArgIndex + j);
					if (holder.isItemStackArg()) {
						int x = this.leftPos + LIST_WIDGET_START_X + COLUMN_SPACING * j;
						int y = this.topPos + LIST_WIDGET_START_Y + ROW_SPACING * i;
						this.blit(stack, x, y, SLOT_TEX_X, SLOT_TEX_Y, SLOT_TEX_WIDTH, SLOT_TEX_WIDTH);
					}
				}
			}
		}
		
		// This has to be separate from the block above as for some reason it's messing with texManager's binds
		for (int i = 0; i < this.iaWidgetArray.length; i++) {
			for (int j = 0; j < this.iaWidgetArray[i].length; j++) {
				this.iaWidgetArray[i][j].render(stack, mouseX, mouseY, partialTicks);
			}
		}
		
		texManager.bind(TASK_SCROLL_SCREEN_GUI);
		
		// The RowIndexWidgets are invisible but Widget#render has to be called for hovering to work
		for (int i = 0; i < this.riWidgetArray.length; i++) this.riWidgetArray[i].render(stack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
		
		this.font.draw(stack, ATTACH_LABEL_TEXT, ATTACH_LABEL_TEXT_X, ATTACH_LABEL_TEXT_Y, ATTACH_LABEL_TEXT_COLOR);
		
		String pageNumberStr = (this.page + 1) + " / " + (this.lastPage + 1);
		this.font.draw(stack, pageNumberStr, (float)(this.imageWidth - this.font.width(pageNumberStr)) * 0.5f, (float) PAGE_NUMBER_Y, TEXT_COLOR);
		
		int topIndex = this.menu.getTopIndex();
		for (int i = 0; i < this.menu.getVisibleRowCount(); i++) {
			StringTextComponent listIndexLabel = new StringTextComponent(String.valueOf(topIndex + i + 1));
			int y1 = LIST_INDEX_START_Y + ROW_SPACING * i;
			if (this.hoveringOverRowIndex != i) this.font.draw(stack, listIndexLabel, LIST_INDEX_RIGHT_X - this.font.width(listIndexLabel), y1, TEXT_COLOR);
		}
		
		int baseArgIndex = this.page * 2 - 1;
		
		for (int i = 0; i < this.iaWidgetArray.length; i++) {
			for (int j = 0; j < this.iaWidgetArray[i].length; j++) {
				int argIndex = baseArgIndex + j;
				Optional<TaskScrollOrder> optional = this.menu.getOrder(topIndex + i);
				if (optional.isPresent()) {
					IArgHolder holder = optional.get().getArgHolder(argIndex);
					if (holder.isItemStackArg()) {
						ITextComponent title = holder.getSelector(this.menu)
								.map(ArgSelector::getTitle)
								.orElse(StringTextComponent.EMPTY);
						
						int x = ITEM_ARG_WIDGET_TITLE_START_X + COLUMN_SPACING * j;
						int y = ITEM_ARG_WIDGET_TITLE_START_Y + ROW_SPACING * i;
						IFormattableTextComponent shortened = TooltipUtils.getShortenedTitle((IFormattableTextComponent) title, this.font, ITEM_ARG_WIDGET_TITLE_WIDTH);
						this.font.draw(stack, shortened, x, y, TEXT_COLOR);
					}
				}
			}
		}
	}
	
	@Override
	protected void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
		super.renderTooltip(stack, mouseX, mouseY);

		for (int i = 0; i < this.asWidgetArray.length; i++) {
			for (int j = 0; j < this.asWidgetArray[0].length; j++) {
				TaskScrollArgSelectorWidget asWidget = this.asWidgetArray[i][j];
				if (asWidget.isHovered()) {
					Optional<ArgSelector<?>> optional = asWidget.getSelector();
					List<ITextComponent> tooltip = optional
							.map(ArgSelector::getComponentTooltip)
							.orElseGet(() -> Arrays.asList(TooltipUtils.NOT_AVAILABLE));
					this.renderComponentTooltip(stack, tooltip, mouseX, mouseY);
				}
			}
		}
		
		for (int i = 0; i < this.iaWidgetArray.length; i++) {
			for (int j = 0; j < this.iaWidgetArray[i].length; j++) {
				ItemArgWidget iaWidget = this.iaWidgetArray[i][j];
				if (iaWidget.isHovered() && !iaWidget.getItem().isEmpty()) {
					this.renderTooltip(stack, iaWidget.getItem(), mouseX, mouseY);
				}
			}
		}
		
		if (this.removeOrderButton.isHovered() && this.removeOrderButton.active) this.removeOrderButton.renderToolTip(stack, mouseX, mouseY);
		if (this.insertOrderButton.isHovered() && this.insertOrderButton.active) this.insertOrderButton.renderToolTip(stack, mouseX, mouseY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			double d0 = mouseX - (double) this.leftPos;
			double d1 = mouseY - (double) this.topPos;
			
			if (this.insideScrollBar(d0, d1)) {
				this.isScrolling = this.canScroll();
				return true;
			}
			
			for (int i = 0; i < this.iaWidgetArray.length; i++) {
				for (int j = 0; j < this.iaWidgetArray[i].length; j++) {
					ItemArgWidget iaWidget = this.iaWidgetArray[i][j];
					if (iaWidget.isHovered()) {
						iaWidget.setItem(this.menu.getCarriedItem());
					}
				}
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			this.isScrolling = false;
		}
		
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX1, double mouseY1, int button, double mouseX2, double mouseY2) {
		if (this.isScrolling && this.insideScrollBar(mouseX1 - (double) this.leftPos, mouseY1 - (double) this.topPos)) {
			this.scrollOffs = ((float) mouseY1 - (float) this.topPos - (float) SCROLL_BAR_BG_GUI_Y - (float)(SCROLL_BAR_BUTTON_HEIGHT) * 0.5f) / (float) SCROLL_BAR_BG_EFFECTIVE_HEIGHT;
			this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0f, 1.0f);
			this.scrollTo(this.scrollOffs);

		}
		
		return super.mouseDragged(mouseX1, mouseY1, button, mouseX2, mouseY2);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDist) {
		if (this.canScroll()) {
			if (this.insideScrollBar(mouseX - (double) this.leftPos, mouseY - (double) this.topPos)){
				this.scrollOffs = this.scrollOffs - (float) scrollDist / (float) SCROLL_BAR_BG_EFFECTIVE_HEIGHT * 5.0f;
				this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0f, 1.0f);
				this.scrollTo(this.scrollOffs);
			}
		}
		
		return super.mouseScrolled(mouseX, mouseY, scrollDist);
	}
	
	@Override
	public void tick() {
		this.hoveringOverRowIndex = -1;
		for (int i = 0; i < this.riWidgetArray.length; i++) {
			RowIndexWidget riWidget = this.riWidgetArray[i];
			if (riWidget.isHovered() && this.menu.isValidSlotOffs(this.menu.getTopIndex() + riWidget.getRowIndex()))
				this.hoveringOverRowIndex = riWidget.getRowIndex();
		}
		
		boolean isHovering = this.hoveringOverRowIndex != -1;
		WidgetUtils.setActiveAndVisible(this.removeOrderButton, isHovering);
		WidgetUtils.setActiveAndVisible(this.insertOrderButton, isHovering);
		
		this.insertOrderButton.active = !this.menu.isOrderListFull();
		
		this.removeOrderButton.y = this.topPos + ORDER_OPERATION_BUTTONS_GUI_START_Y + ROW_SPACING * this.hoveringOverRowIndex;
		this.insertOrderButton.y = this.removeOrderButton.y + ORDER_OPERATION_BUTTONS_WIDTH;
		
		super.tick();
	}
	
	@Override
	public void onClose() {
		this.saveWidgetsToOrders();
		IWNetwork.CHANNEL.sendToServer(new STaskScrollSyncMessage(this.menu.getHand(), this.menu.getOrderList(), this.menu.getLabelItem()));
		super.onClose();
	}
	
	private void prevPage(Button button) {
		if (this.page > 0) {
			this.saveWidgetsToOrders();
			if (this.pageIsValid()) --this.page;
			this.updatePage();
		}
	}
	
	private void nextPage(Button button) {
		if (this.page < this.lastPage) {
			this.saveWidgetsToOrders();
			if (this.pageIsValid()) ++this.page;
			this.updatePage();
		}
	}
	
	private boolean pageIsValid() {
		if (this.page < 0 || this.page > this.lastPage) {
			IndustrialWarfare.LOGGER.warn("A TaskScrollScreen opened by the client was on page " + this.page + ". The page is not valid and will be switched to page 0 (displayed as 1). Look into it, will ya?");
			this.page = 0;
			return false;
		}
		return true;
	}
	
	public int getPage() {
		return this.page;
	}
	
	private void updatePageButtons() {
		WidgetUtils.setActiveAndVisible(this.prevPageButton, this.page > 0);
		WidgetUtils.setActiveAndVisible(this.nextPageButton, this.page < this.lastPage);
	}
	
	private void updateMaxPages() {
		this.lastPage = 0;
		List<TaskScrollOrder> orderList = this.menu.getOrderList();
		for (TaskScrollOrder order : orderList) {
			int possibleLength = MathHelper.ceil((float)(order.getCommand().getArgCount() + 1) * 0.5f) - 1;
			if (possibleLength > this.lastPage) this.lastPage = possibleLength;
		}
		if (this.page > this.lastPage) this.page = this.lastPage;
		this.updatePageButtons();
	}
	
	private void updatePage() {
		this.updateWidgets();
		this.updatePageButtons();
	}
	
	private void updateScreen() {
		this.updateWidgets();
		this.updateMaxPages();
	}
	
	public void updateSelectorRelatedFeatures() {
		this.saveWidgetsToOrders();
		this.updateWidgets();
		this.updateMaxPages();
	}
	
	private void updateWidgets() {
		int argIndexBase = this.page * 2 - 1;
		int topIndex = this.menu.getTopIndex();
		
		for (int i = 0; i < this.asWidgetArray.length; i++) {
			int orderIndex = topIndex + i;
			Optional<TaskScrollOrder> optional = this.menu.getOrder(orderIndex);
			if (optional.isPresent()) {
				TaskScrollOrder order = optional.get();
				for (int j = 0; j < this.asWidgetArray[i].length; j++) {
					TaskScrollArgSelectorWidget asWidget = this.asWidgetArray[i][j];
					int argIndex = argIndexBase + j;
					IArgHolder holder = order.getArgHolder(argIndex);
					if (argIndex == -1) {
						asWidget.setSelector(Optional.of(new TaskCommandArgSelector(this.validCmds, order)));
					} else if (!holder.isItemStackArg()) {
						asWidget.setSelector(holder.getSelector(this.menu));
					}
					boolean inRange = -1 <= argIndex && argIndex < order.getCommand().getArgCount();
					WidgetUtils.setActiveAndVisible(asWidget, inRange && !holder.isItemStackArg());
				}
			} else {
				for (int j = 0; j < this.asWidgetArray[0].length; j++)
					WidgetUtils.setActiveAndVisible(this.asWidgetArray[i][j], false);
			}
		}
		
		for (int i = 0; i < this.iaWidgetArray.length; i++) {
			int orderIndex = topIndex + i;
			Optional<TaskScrollOrder> optional = this.menu.getOrder(orderIndex);
			if (optional.isPresent()) {
				TaskScrollOrder order = optional.get();
				for (int j = 0; j < this.iaWidgetArray[i].length; j++) {
					ItemArgWidget iaWidget = this.iaWidgetArray[i][j];
					int argIndex = argIndexBase + j;
					iaWidget.setOrderIndex(orderIndex);
					iaWidget.setArgIndex(argIndex);
					boolean inRange = 0 <= argIndex && argIndex < order.getCommand().getArgCount();
					WidgetUtils.setActiveAndVisible(iaWidget, inRange && order.getArgHolder(argIndex).isItemStackArg());
				}
			} else {
				for (int j = 0; j < this.iaWidgetArray[i].length; j++)
					WidgetUtils.setActiveAndVisible(this.iaWidgetArray[i][j], false);
			}
		}
		
		boolean isVisible = this.menu.getVisibleRowCount() < TaskScrollContainer.ROW_COUNT;
		WidgetUtils.setActiveAndVisible(this.addOrderButton, isVisible && !this.menu.isOrderListFull());
		this.addOrderButton.y = this.topPos + ADD_ORDER_BUTTON_START_Y + this.menu.getVisibleRowCount() % 5 * ROW_SPACING;
		
		for (int i = 0; i < this.riWidgetArray.length; i++)
			WidgetUtils.setActiveAndVisible(this.riWidgetArray[i], this.menu.isValidSlotOffs(topIndex + i));
	}
	
	private void saveWidgetsToOrders() {
		int topIndex = this.menu.getTopIndex();
		int argIndexBase = this.page * 2 - 1;
		
		Set<Integer> changedCommands = new HashSet<>();
		
		for (int i = 0; i < this.asWidgetArray.length; i++) {
			int orderIndex = topIndex + i;
			Optional<TaskScrollOrder> orderOptional = this.menu.getOrder(orderIndex);
			
			if (orderOptional.isPresent() && !changedCommands.contains(orderIndex)) {
				TaskScrollOrder order = orderOptional.get();
				
				for (int j = 0; j < this.asWidgetArray[i].length; j++) {
					TaskScrollArgSelectorWidget asWidget = this.asWidgetArray[i][j];
					int argIndex = argIndexBase + j;
					IArgHolder holder = order.getArgHolder(argIndex);
					if (argIndex == -1) {
						Optional<ArgSelector<?>> selector = asWidget.getSelector();
						if (selector.isPresent()) {
							TaskScrollCommand oldCommand = order.getCommand();
							order.setCommand(selector.get().getSelectedArg().getLoc()
									.map(IWModRegistries.TASK_SCROLL_COMMANDS::getValue)
									.orElse(this.validCmds.get(0)));
							if (order.getCommand() != oldCommand) changedCommands.add(orderIndex);
						}
					} else if (!holder.isItemStackArg()) {
						asWidget.getSelector().ifPresent(as -> {
							holder.accept(as.getSelectedArg());
						});
					}
				}
			}
		}
		
		for (int i = 0; i < this.iaWidgetArray.length; i++) {
			int orderIndex = topIndex + i;
			Optional<TaskScrollOrder> orderOptional = this.menu.getOrder(orderIndex);
			
			if (orderOptional.isPresent() && !changedCommands.contains(orderIndex)) {
				TaskScrollOrder order = orderOptional.get();
				
				for (int j = 0; j < this.iaWidgetArray[i].length; j++) {
					ItemArgWidget iaWidget = this.iaWidgetArray[i][j];
					int argIndex = argIndexBase + j;
					IArgHolder holder = order.getArgHolder(argIndex);
					if (holder.isItemStackArg()) {
						holder.accept(new ArgWrapper(iaWidget.getItem()));
					}
				}
				
			}
		}
	}
	
	private void scrollTo(float f) {
		int o = this.menu.isOrderListFull() ? 0 : 1;
		this.saveWidgetsToOrders();
		
		this.menu.setTopIndex(MathHelper.floor(f * (float)(this.menu.getOrderListSize() - TaskScrollContainer.ROW_COUNT + o)));
		int newTopIndex = this.menu.getTopIndex();
		
		for (int i = 0; i < this.iaWidgetArray.length; i++) {
			for (int j = 0; j < this.iaWidgetArray[i].length; j++) {
				ItemArgWidget iaWidget = this.iaWidgetArray[i][j];
				iaWidget.setOrderIndex(newTopIndex + i);
			}
		}

		this.updateWidgets();
	}
	
	private void updateScrollOffs(float oldSize) {
		this.scrollOffs = MathHelper.clamp(this.scrollOffs * oldSize / (float) this.getScrollLength(), 0.0f, 1.0f);
	}
	
	private boolean canScroll() {
		return this.menu.getOrderListSize() >= TaskScrollContainer.ROW_COUNT;
	}
	
	private boolean insideScrollBar(double mouseX, double mouseY) {
		return SCROLL_BAR_BG_GUI_X <= mouseX && mouseX < SCROLL_BAR_BG_GUI_X + SCROLL_BAR_WIDTH && SCROLL_BAR_BG_GUI_Y <= mouseY && mouseY < SCROLL_BAR_BG_GUI_Y + SCROLL_BAR_BG_GUI_HEIGHT; 
	}
	
	private int getScrollLength() {
		return this.menu.getOrderListSize() + (this.menu.isOrderListFull() ? 0 : 1);
	}
	
	private void addOrder(Button button) {
		if (!this.menu.isOrderListFull()) {
			float oldSize = (float) this.getScrollLength();
			this.saveWidgetsToOrders();
			this.menu.getOrderList().add(TaskScrollOrder.withPos(this.validCmds.get(0), this.menu.getPlayer().blockPosition()));
			this.updateScreen();
			this.updateScrollOffs(oldSize);
		}
	}
	
	private void removeOrder(Button button) {
		if (this.hoveringOverRowIndex == -1) return;
		
		int index = this.menu.getTopIndex() + this.hoveringOverRowIndex;
		if (this.menu.isValidSlotOffs(index)) {
			float oldSize = (float) this.getScrollLength();
			this.saveWidgetsToOrders();
			this.menu.getOrderList().remove(index);
			this.updateScreen();
			this.updateScrollOffs(oldSize);
		}
	}
	
	private void insertOrder(Button button) {
		if (this.hoveringOverRowIndex == -1) return;
		
		int index = this.menu.getTopIndex() + this.hoveringOverRowIndex;
		if (this.menu.isValidSlotOffs(index) && !this.menu.isOrderListFull()) {
			float oldSize = (float) this.getScrollLength();
			this.saveWidgetsToOrders();
			this.menu.getOrderList().add(index, TaskScrollOrder.withPos(this.validCmds.get(0), this.menu.getPlayer().blockPosition()));
			this.updateScreen();
			this.updateScrollOffs(oldSize);
		}
	}
	
}
