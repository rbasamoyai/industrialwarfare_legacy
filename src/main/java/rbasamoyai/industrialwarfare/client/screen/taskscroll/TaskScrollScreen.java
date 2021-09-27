package rbasamoyai.industrialwarfare.client.screen.taskscroll;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.selectors.ArgSelector;
import rbasamoyai.industrialwarfare.client.screen.selectors.TaskScrollArgSelectorWidget;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.TaskScrollOrder;
import rbasamoyai.industrialwarfare.common.taskscrollcmds.TaskScrollCommand;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.STaskScrollSyncMessage;
import rbasamoyai.industrialwarfare.utils.TextureUtils;
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
	
	private static final int SELECTOR_COLUMNS = 2;
	private static final int ROW_SPACING = 20;
	private static final int TEXTURE_SIZE = 256;
	
	private static final int LIST_WIDGET_START_X = 24;
	private static final int LIST_WIDGET_START_Y = 21;
	private static final int LIST_WIDGET_SPACING = 2; // Only describes the spacing between adjacent widget edges, not the spacing between the top left corners.
	private static final int LIST_WIDGET_WIDTH = 58;
	private static final int LIST_WIDGET_HEIGHT = ROW_SPACING - LIST_WIDGET_SPACING;
	
	private static final int PAGE_NEXT_BUTTON_GUI_X = 115;
	private static final int PAGE_PREV_BUTTON_GUI_X = 45;
	private static final int PAGE_BUTTON_GUI_Y = 126;
	private static final int PAGE_BUTTON_TEX_X = 176;
	private static final int PAGE_NEXT_BUTTON_TEX_Y = 18;
	private static final int PAGE_PREV_BUTTON_TEX_Y = 42;
	private static final int PAGE_BUTTON_WIDTH = 16;
	private static final int PAGE_BUTTON_HEIGHT = 12;
	
	private static final int SLOT_GUI_X = 144;
	private static final int SLOT_TEX_X = 176;
	private static final int SLOT_TEX_Y = 0;
	private static final int SLOT_TEX_WIDTH = 18;
	private static final int SLOT_ITEM_X = SLOT_GUI_X + 1;
	private static final int SLOT_ITEM_START_Y = LIST_WIDGET_START_Y + 1;
	private static final int SHADE_COLOR = 1056964608;
	
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
	private static final List<Integer[]> COLOR_COORDS = Arrays.asList(
			new Integer[] {BORDER_START_COLOR_X, COLORS_Y},
			new Integer[] {BORDER_END_COLOR_X, COLORS_Y},
			new Integer[] {FIELD_COLOR_X, COLORS_Y}
			);
	
	private static final int TEXT_COLOR = 4210752;
	private static final int ATTACH_LABEL_TEXT_COLOR = 8350000;
	
	private int page = 0;
	private int lastPage = 0;
	private final List<TaskScrollCommand> validCmds;
	private float scrollOffs = 0;
	private boolean isScrolling = false;
	private int hoveringOverRowIndex = 0;
	
	private final int labelBorderStartColor;
	private final int labelBorderEndColor;
	private final int labelFieldColor;
	
	private Button prevPageButton;
	private Button nextPageButton;
	private Button addOrderButton;
	private Button removeOrderButton;
	private Button insertOrderButton;
	
	private final TaskScrollArgSelectorWidget[][] asWidgetArray = new TaskScrollArgSelectorWidget[TaskScrollContainer.ROW_COUNT][SELECTOR_COLUMNS];
	private final RowIndexWidget[] riWidgetArray = new RowIndexWidget[TaskScrollContainer.ROW_COUNT];
	private final FilterItemWidget[] fiWidgetArray = new FilterItemWidget[TaskScrollContainer.ROW_COUNT];
	
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
		WidgetUtils.setActiveAndVisible(this.prevPageButton, false);
		
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
		WidgetUtils.setActiveAndVisible(this.nextPageButton, this.lastPage > 0);
		
		this.addOrderButton = this.addButton(new Button(
				this.leftPos + ADD_ORDER_BUTTON_X,
				this.topPos + ADD_ORDER_BUTTON_START_Y,
				this.font.width(ADD_ORDER_BUTTON_TEXT) + 8,
				ROW_SPACING,
				ADD_ORDER_BUTTON_TEXT,
				this::addOrder
				));
		WidgetUtils.setActiveAndVisible(this.addOrderButton, !this.menu.isOrderListFull());
		
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
		
		for (int i = 0; i < this.asWidgetArray.length; i++) {
			for (int j = 0; j < this.asWidgetArray[0].length; j++) {
				int x = this.leftPos + LIST_WIDGET_START_X + (LIST_WIDGET_WIDTH + LIST_WIDGET_SPACING) * j;
				int y = this.topPos + LIST_WIDGET_START_Y + (LIST_WIDGET_HEIGHT + LIST_WIDGET_SPACING) * i;
				Optional<TaskScrollOrder> optional = this.menu.getOrder(topIndex + i);
				
				int argPos = -2 + j;
				
				TaskScrollArgSelectorWidget argSelWidget = new TaskScrollArgSelectorWidget(this.minecraft, this, x, y, LIST_WIDGET_WIDTH, optional, argPos);
				argSelWidget.setOrder(optional);
				
				switch (j) {
				case 0:
					argSelWidget.setSelector(Optional.of(new TaskCmdArgSelector(this.validCmds, optional.orElseGet(() -> new TaskScrollOrder(this.validCmds.get(0))))));
					break;
				case 1:
					argSelWidget.setSelector(Optional.of(new BlockPosArgSelector(this.getPlayer(), optional.map(TaskScrollOrder::getPos).orElse(this.getPlayer().blockPosition()))));
					break;
				}
				int jCopy = j; // please shut up functional interface
				WidgetUtils.setActiveAndVisible(argSelWidget, optional.map(o -> jCopy != 1 || o.usesBlockPos()).orElse(false));
				this.asWidgetArray[i][j] = this.addWidget(argSelWidget);
			}
		}
		
		// Don't want to interlace row hover widgets with selector widgets
		for (int i = 0; i < this.riWidgetArray.length; i++) {
			int y = this.topPos + LIST_WIDGET_START_Y + ROW_SPACING * i;
			this.riWidgetArray[i] = this.addWidget(new RowIndexWidget(this.leftPos + ROW_INDEX_WIDGET_X, y, LIST_WIDGET_HEIGHT, LIST_WIDGET_HEIGHT, i));
			WidgetUtils.setActiveAndVisible(this.riWidgetArray[i], this.menu.isValidSlotOffs(topIndex + i));
		}
		
		for (int i = 0; i < this.fiWidgetArray.length; i++) {
			int y = this.topPos + SLOT_ITEM_START_Y + ROW_SPACING * i;
			int index = topIndex + i;
			this.fiWidgetArray[i] = this.addWidget(new FilterItemWidget(this.leftPos + SLOT_ITEM_X, y, index, this.menu.getOrderList(), this.itemRenderer));
			boolean activate = this.menu.getOrder(index).map(TaskScrollOrder::canUseFilter).orElse(false);
			WidgetUtils.setActiveAndVisible(this.fiWidgetArray[i], activate);
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
		this.minecraft.getTextureManager().bind(TASK_SCROLL_SCREEN_GUI);
		
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		int scrollBarTexYDiff = this.canScroll() ? 0 : SCROLL_BAR_BUTTON_HEIGHT ;
		int scrollBarY = this.topPos + SCROLL_BAR_BG_GUI_Y + MathHelper.floor((float) SCROLL_BAR_BG_EFFECTIVE_HEIGHT * this.scrollOffs);
		this.blit(stack, this.leftPos + SCROLL_BAR_BG_GUI_X, scrollBarY, SCROLL_BAR_BUTTON_TEX_X, SCROLL_BAR_BUTTON_TEX_Y + scrollBarTexYDiff, SCROLL_BAR_WIDTH, SCROLL_BAR_BUTTON_HEIGHT);
		
		// Rendering attach label
		int width = ATTACH_LABEL_BASE_LENGTH + this.font.width(ATTACH_LABEL_TEXT);
		this.fillGradient(stack, this.leftPos + ATTACH_LABEL_X, this.topPos + ATTACH_LABEL_Y, this.leftPos + ATTACH_LABEL_X + width, this.topPos + ATTACH_LABEL_Y + ATTACH_LABEL_HEIGHT, this.labelBorderStartColor, this.labelBorderEndColor);
		this.fillGradient(stack, this.leftPos + ATTACH_LABEL_X + 1, this.topPos + ATTACH_LABEL_Y + 1, this.leftPos + ATTACH_LABEL_X + width - 1, this.topPos + ATTACH_LABEL_Y + ATTACH_LABEL_HEIGHT - 1, this.labelFieldColor, this.labelFieldColor);
		this.blit(stack, this.leftPos + ATTACH_LABEL_SLOT_X, this.topPos + ATTACH_LABEL_SLOT_Y, SLOT_TEX_X, SLOT_TEX_Y, SLOT_TEX_WIDTH, SLOT_TEX_WIDTH);
		
		if (this.page == 0) { // Rendering filter slots
			for (int i = 0; i < this.menu.getVisibleRowCount(); i++) {
				int y = LIST_WIDGET_START_Y + ROW_SPACING * i;
				this.blit(stack, this.leftPos + SLOT_GUI_X, this.topPos + y, SLOT_TEX_X, SLOT_TEX_Y, SLOT_TEX_WIDTH, SLOT_TEX_WIDTH);
			}
		}
		
		for (int i = 0; i < this.asWidgetArray.length; i++) {
			for (int j = 0; j < this.asWidgetArray[0].length; j++) {
				this.asWidgetArray[i][j].render(stack, mouseX, mouseY, partialTicks);
			}
		}
		
		// The RowIndexWidgets are invisible but Widget#render has to be called for hovering to work
		for (int i = 0; i < this.riWidgetArray.length; i++) this.riWidgetArray[i].render(stack, mouseX, mouseY, partialTicks);
		for (int i = 0; i < this.fiWidgetArray.length; i++) this.fiWidgetArray[i].render(stack, mouseX, mouseY, partialTicks);
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
			
			if (!this.menu.getOrder(topIndex + i).map(TaskScrollOrder::canUseFilter).orElse(true) && this.page == 0) {
				int y2 = SLOT_ITEM_START_Y + ROW_SPACING * i;
				this.fillGradient(stack, SLOT_ITEM_X, y2, SLOT_ITEM_X + 16, y2 + 16, SHADE_COLOR, SHADE_COLOR);
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
					Optional<ArgSelector<?>> optional = asWidget.getSelectorOptional();
					List<ITextComponent> tooltip = optional
							.map(ArgSelector::getComponentTooltip)
							.orElseGet(() -> Arrays.asList(TaskScrollArgSelectorWidget.NOT_AVAILABLE));
					this.renderComponentTooltip(stack, tooltip, mouseX, mouseY);
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
			
			for (int i = 0; i < this.fiWidgetArray.length; i++) {
				FilterItemWidget fiWidget = this.fiWidgetArray[i];
				if (fiWidget.isHovered()) {
					fiWidget.setItem(this.menu.getPlayer().inventory.getCarried());
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
		this.saveSelectorsToOrders();
		IWNetwork.CHANNEL.sendToServer(new STaskScrollSyncMessage(this.menu.getHand(), this.menu.getOrderList(), this.menu.getLabelItem()));
		super.onClose();
	}
	
	private void prevPage(Button button) {
		if (this.page > 0) {
			this.updateSelectorRelatedFeatures();
			if (this.pageIsValid()) --this.page;
			this.updatePage();
		}
	}
	
	private void nextPage(Button button) {
		if (this.page < this.lastPage) {
			this.updateSelectorRelatedFeatures();
			if (this.pageIsValid()) ++this.page;
			this.updatePage();
		}
	}
	
	private boolean pageIsValid() {
		if (this.page < 0 || this.page > this.lastPage) {
			IndustrialWarfare.LOGGER.warn("A TaskScrollScreen opened by " + this.getPlayer().getDisplayName().getString() + " was on page " + this.page + ". The page is not valid and will be switched to page 0 (displayed as 1). Look into it, will ya?");
			this.page = 0;
			return false;
		}
		return true;
	}
	
	private void updateWidgets() {
		int widgetArgIndexBase = this.page * 2 - 2;
		int topIndex = this.menu.getTopIndex();
		
		for (int i = 0; i < this.asWidgetArray.length; i++) {
			int orderIndex = topIndex + i;
			Optional<TaskScrollOrder> orderOptional = this.menu.getOrder(orderIndex);
			
			if (orderOptional.isPresent()) {
				
				TaskScrollOrder order = orderOptional.get();
				for (int j = 0; j < this.asWidgetArray[0].length; j++) {
					TaskScrollArgSelectorWidget asWidget = this.asWidgetArray[i][j];
					int widgetArgIndex = widgetArgIndexBase + j;
					if (this.page == 0) {
						switch (j) {
						case 0:
							asWidget.setSelector(Optional.of(new TaskCmdArgSelector(this.validCmds, order)));
							break;
						case 1:
							asWidget.setSelector(Optional.of(new BlockPosArgSelector(this.inventory.player, order.getPos())));
							break;
						}
						WidgetUtils.setActiveAndVisible(asWidget, j != 1 || order.usesBlockPos());
					} else {
						TaskScrollCommand orderCmd = order.getCmd();
						if (widgetArgIndex < orderCmd.getArgCount()) {
							WidgetUtils.setActiveAndVisible(asWidget, true);
							int arg = widgetArgIndex < order.getArgs().size() ? order.getArgs().get(widgetArgIndex) : 0;
							ArgSelector<Byte> selector = orderCmd.getSelectorAt(widgetArgIndex).apply(arg);
							asWidget.setSelector(Optional.ofNullable(selector));
						} else WidgetUtils.setActiveAndVisible(asWidget, false);
					}
				}
				
			} else {
				for (int j = 0; j < this.asWidgetArray[0].length; j++)
					WidgetUtils.setActiveAndVisible(this.asWidgetArray[i][j], false);
			}
		}
		
		boolean isVisible = this.menu.getVisibleRowCount() < TaskScrollContainer.ROW_COUNT;
		WidgetUtils.setActiveAndVisible(this.addOrderButton, isVisible && !this.menu.isOrderListFull());
		this.addOrderButton.y = this.topPos + ADD_ORDER_BUTTON_START_Y + this.menu.getVisibleRowCount() % 5 * ROW_SPACING;
		
		for (int i = 0; i < this.riWidgetArray.length; i++)
			WidgetUtils.setActiveAndVisible(this.riWidgetArray[i], this.menu.isValidSlotOffs(topIndex + i));
		
		for (int i = 0; i < this.fiWidgetArray.length; i++) {
			int index = topIndex + i;
			boolean activate = this.menu.getOrder(index).map(TaskScrollOrder::canUseFilter).orElse(false) && this.page == 0;
			WidgetUtils.setActiveAndVisible(this.fiWidgetArray[i], activate);
		}
	}
	
	private void updatePage() {
		this.updateWidgets();
		this.updatePageButtons();
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
			int possibleLength = MathHelper.ceil((float)order.getCmd().getArgCount() * 0.5f);
			if (possibleLength > this.lastPage) this.lastPage = possibleLength;
		}
		if (this.page > this.lastPage) this.page = this.lastPage;
		this.updatePageButtons();
	}
	
	private void updateScreen() {
		this.updateMaxPages();
		this.updatePage();
	}
	
	public void updateSelectorRelatedFeatures() {
		this.saveSelectorsToOrders();
		this.updateMaxPages();
	}
	
	@SuppressWarnings("unchecked")
	private void saveSelectorsToOrders() {
		int widgetArgIndexBase = this.page * 2 - 2;
		
		for (int i = 0; i < this.asWidgetArray.length; i++) {
			int orderIndex = this.menu.getTopIndex() + i;
			Optional<TaskScrollOrder> orderOptional = this.menu.getOrder(orderIndex);
			
			if (orderOptional.isPresent()) {
				TaskScrollOrder order = this.menu.getOrder(orderIndex).get();
				
				for (int j = 0; j < this.asWidgetArray[0].length; j++) {
					int widgetArgIndex = widgetArgIndexBase + j;
					TaskScrollArgSelectorWidget asWidget = this.asWidgetArray[i][j];
					if (this.menu.isValidSlotOffs(orderIndex)) {
						// If the selector's not present, don't save the data
						Optional<ArgSelector<?>> optional = asWidget.getSelectorOptional();
						if (optional.isPresent()) {
							Object selectedArg = optional.get().getSelectedArg();
							if (this.page == 0) {
								if (j == 0 && selectedArg instanceof TaskScrollCommand) {
									order.setCmdFromSelector((ArgSelector<TaskScrollCommand>) optional.get());
									WidgetUtils.setActiveAndVisible(this.asWidgetArray[i][1], order.usesBlockPos());
								} else if (j == 1 && selectedArg instanceof BlockPos) {
									if (order.usesBlockPos()) order.setPosFromSelector((ArgSelector<BlockPos>) optional.get());
								} else {
									String type = "";
									switch (j) {
									case 0: type = "TaskScrollCommand"; break;
									case 1: type = "BlockPos"; break;
									}
									// good god this is a long warning message
									IndustrialWarfare.LOGGER.warn("ArgSelectorWidget at row " + (i + 1) + " column " + (j + 1) + " in TaskScrollScreen opened by player " + this.getPlayer().getDisplayName().getString() + " has an ArgSelector whose generic type does not match up with the required type of the order position (generic type should be " + type + ")");
								}
							} else {
								TaskScrollCommand orderCmd = order.getCmd();
								if (widgetArgIndex < orderCmd.getArgCount()) {
									if (selectedArg instanceof Byte) {
										order.setArgFromSelectorAndIndex((ArgSelector<Byte>) optional.get(), widgetArgIndex);
									} else {// An etude in long warning messages, part 2
										IndustrialWarfare.LOGGER.warn("ArgSelectorWidget at row " + (i + 1) + " column " + (j + 1) + " in TaskScrollScreen opened by player " + this.getPlayer().getDisplayName().getString() + " has an ArgSelector whose generic type does not match up with the required type of the order position (generic type should be Byte)");
									}
								}
							}
						} else {
							IndustrialWarfare.LOGGER.warn("ArgSelectorWidget at row " + (i + 1) + " column " + (j + 1) + " in TaskScrollScreen opened by player " + this.getPlayer().getDisplayName().getString() + " does not have an ArgSelector present");
						}
					}
				}
			}
		}
	}
	
	private void scrollTo(float f) {
		int o = this.menu.isOrderListFull() ? 0 : 1;
		this.saveSelectorsToOrders();
		
		this.menu.setTopIndex(MathHelper.floor(f * (float)(this.menu.getOrderListSize() - TaskScrollContainer.ROW_COUNT + o)));
		int newTopIndex = this.menu.getTopIndex();
		
		for (int i = 0; i < this.fiWidgetArray.length; i++) {
			FilterItemWidget fiWidget = this.fiWidgetArray[i];
			fiWidget.setIndex(newTopIndex + i);
		}

		this.updatePage();
	}
	
	private void updateScrollOffs(float oldSize) {
		this.scrollOffs = MathHelper.clamp(this.scrollOffs * oldSize / (float) this.getScrollLength(), 0.0f, 1.0f);
	}
	
	private void addOrder(Button button) {
		if (!this.menu.isOrderListFull()) {
			float oldSize = (float) this.getScrollLength();
			this.saveSelectorsToOrders();
			this.menu.getOrderList().add(new TaskScrollOrder(this.validCmds.get(0), this.inventory.player.blockPosition()));
			this.updateScreen();
			this.updateScrollOffs(oldSize);
		}
	}
	
	private void removeOrder(Button button) {
		if (this.hoveringOverRowIndex == -1) return;
		
		int index = this.menu.getTopIndex() + this.hoveringOverRowIndex;
		if (this.menu.isValidSlotOffs(index)) {
			float oldSize = (float) this.getScrollLength();
			this.saveSelectorsToOrders();
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
			this.saveSelectorsToOrders();
			this.menu.getOrderList().add(index, new TaskScrollOrder(this.validCmds.get(0), this.inventory.player.blockPosition()));
			this.updateScreen();
			this.updateScrollOffs(oldSize);
		}
	}
	
	public PlayerEntity getPlayer() {
		return this.inventory.player;
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
	
}
