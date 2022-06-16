package rbasamoyai.industrialwarfare.client.screen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.WidgetUtils;
import rbasamoyai.industrialwarfare.common.containers.resourcestation.ResourceStationContainer;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.SSelectTab;
import rbasamoyai.industrialwarfare.core.network.messages.ResourceStationMessages.SSetRunning;

public class ResourceStationScreen extends ContainerScreen<ResourceStationContainer> {
	
	private static final ResourceLocation RESOURCE_GATHERING_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/workstations/resource_station.png");
	private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	
	private static final int TEX_WIDTH = 512;
	private static final int TEX_HEIGHT = 256;
	private static final int TOP_MENU_START_X = 7;
	private static final int TOP_MENU_START_Y = 17;
	private static final int TOP_MENU_INVENTORY_TEX_X = 176;
	private static final int TOP_MENU_INVENTORY_TEX_Y = 0;
	private static final int TOP_MENU_INVENTORY_WIDTH = 162;
	private static final int TOP_MENU_INVENTORY_HEIGHT = 54;
	private static final int TOP_MENU_REQUESTS_WIDTH = 61;
	private static final int TOP_MENU_REQUESTS_HEIGHT = 54;
	private static final int TOP_MENU_REQUESTS_TEX_X = 176;
	private static final int TOP_MENU_REQUESTS_TEX_Y = 54;
	private static final int SCROLL_BAR_TEX_X = 237;
	private static final int SCROLL_BAR_TEX_Y = 54;
	private static final int SCROLL_BAR_BUTTON_WIDTH = 6;
	private static final int SCROLL_BAR_BUTTON_HEIGHT = 9;
	private static final int SCROLL_BAR_X = 61;
	private static final int SCROLL_BAR_Y = 18;
	private static final int SCROLL_BAR_BG_WIDTH = 6;
	private static final int SCROLL_BAR_BG_HEIGHT = 52;
	private static final int SCROLL_BAR_BG_HEIGHT_PRACTICAL = SCROLL_BAR_BG_HEIGHT - SCROLL_BAR_BUTTON_HEIGHT;
	private static final int SLOT_SPACING = 18;
	private static final int SLOT_SIDE = 16;
	private static final int REQUESTS_ROWS = 3;
	private static final int REQUESTS_COLUMNS = 3;
	private static final int REQUESTS_COUNT = REQUESTS_ROWS * REQUESTS_COLUMNS;
	private static final int TIME_BETWEEN_ITEMS = 20;
	
	private static final ITextComponent SUPPLIES_KEY = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".resource_station.supplies");
	private static final ITextComponent BUFFER_KEY = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".resource_station.buffer");
	private static final ITextComponent STOP_TEXT = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".resource_station.stop");
	private static final ITextComponent START_TEXT = new TranslationTextComponent("gui." + IndustrialWarfare.MOD_ID + ".resource_station.start");
	
	private boolean isScrolling = false;
	private float scrollOffs = 0.0f;
	private long renderTicks = 0;
	
	private final List<List<ItemStack>> matchingItemsCache = new ArrayList<>();
	
	private final List<ITextComponent> titles;
	
	private Button stopButton;
	private Button startButton;
	
	public ResourceStationScreen(ResourceStationContainer menu, PlayerInventory playerInv, ITextComponent title) {
		super(menu, playerInv, title);
		this.imageHeight = 168;
		this.inventoryLabelY = this.imageHeight - 94;
		this.titles = ImmutableList.of(this.title, SUPPLIES_KEY, BUFFER_KEY);
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.stopButton = this.addButton(new Button(this.leftPos + TOP_MENU_START_X + 65, this.topPos + TOP_MENU_START_Y + 1, 80, 20, STOP_TEXT, b -> this.setRunning(b, false)));
		this.startButton = this.addButton(new Button(this.leftPos + TOP_MENU_START_X + 65, this.topPos + TOP_MENU_START_Y + 1, 80, 20, START_TEXT, b -> this.setRunning(b, true)));
		boolean running = this.menu.isRunning();
		WidgetUtils.setActiveAndVisible(this.startButton, !running);
		WidgetUtils.setActiveAndVisible(this.stopButton, running);
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
		
		int selected = this.menu.getSelected();
		if (selected != 0) {
			texManager.bind(TABS_LOCATION);
			this.renderTabButton(stack, this.leftPos, this.topPos - 28, this.menu.getIcon(), 0, false);
		}
		if (selected != 1) {
			texManager.bind(TABS_LOCATION);
			this.renderTabButton(stack, this.leftPos + 29, this.topPos - 28, new ItemStack(Items.CHEST), 1, false);
		}
		if (selected != 2) {
			texManager.bind(TABS_LOCATION);
			this.renderTabButton(stack, this.leftPos + 58, this.topPos - 28, new ItemStack(Items.HOPPER), 2, false);
		}
		
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		texManager.bind(RESOURCE_GATHERING_GUI);
		blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, TEX_WIDTH, TEX_HEIGHT);
		if (selected == 0) {
			blit(stack, this.leftPos + TOP_MENU_START_X, this.topPos + TOP_MENU_START_Y, TOP_MENU_REQUESTS_TEX_X, TOP_MENU_REQUESTS_TEX_Y, TOP_MENU_REQUESTS_WIDTH, TOP_MENU_REQUESTS_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
			
			int base = MathHelper.ceil((float) this.matchingItemsCache.size() * this.scrollOffs / 3.0f) * 3;
			
			this.setBlitOffset(100);
			this.itemRenderer.blitOffset = 100.0f;
			
			for (int i = base; i < REQUESTS_COUNT + base && i < this.matchingItemsCache.size(); ++i) {
				int rx = i % REQUESTS_COLUMNS * SLOT_SPACING + TOP_MENU_START_X + 1;
				int ry = i / REQUESTS_COLUMNS * SLOT_SPACING + TOP_MENU_START_Y + 1;
				List<ItemStack> fulfillingItems = this.matchingItemsCache.get(i);
				int index = (int)((this.renderTicks / (long) TIME_BETWEEN_ITEMS) % fulfillingItems.size());
				this.itemRenderer.renderAndDecorateItem(fulfillingItems.get(index), this.leftPos + rx, this.topPos + ry);
			}
			
			texManager.bind(RESOURCE_GATHERING_GUI);
			
			this.itemRenderer.blitOffset = 0.0f;
			this.setBlitOffset(0);
			
			int texX = this.canScroll() ? SCROLL_BAR_TEX_X : SCROLL_BAR_TEX_X + SCROLL_BAR_BUTTON_WIDTH;
			int guiYOffs = this.canScroll() ? (int)(this.scrollOffs * (float) SCROLL_BAR_BG_HEIGHT_PRACTICAL) : 0; 
			
			blit(stack, this.leftPos + SCROLL_BAR_X, this.topPos + SCROLL_BAR_Y + guiYOffs, texX, SCROLL_BAR_TEX_Y, SCROLL_BAR_BUTTON_WIDTH, SCROLL_BAR_BUTTON_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
		} else {
			blit(stack, this.leftPos + TOP_MENU_START_X, this.topPos + TOP_MENU_START_Y, TOP_MENU_INVENTORY_TEX_X, TOP_MENU_INVENTORY_TEX_Y, TOP_MENU_INVENTORY_WIDTH, TOP_MENU_INVENTORY_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
		}
		
		texManager.bind(TABS_LOCATION);
		if (selected == 0) this.renderTabButton(stack, this.leftPos, this.topPos - 28, this.menu.getIcon(), 0, true);
		else if (selected == 1) this.renderTabButton(stack, this.leftPos + 29, this.topPos - 28, new ItemStack(Items.CHEST), 1, true);
		else if (selected == 2) this.renderTabButton(stack, this.leftPos + 58, this.topPos - 28, new ItemStack(Items.HOPPER), 2, true);
	}
	
	private void renderTabButton(MatrixStack stack, int x, int y, ItemStack icon, int index, boolean selected) {
		int texX = 28 * index;
		int texY = selected ? 32 : 0;
		
		RenderSystem.enableBlend();
		this.itemRenderer.blitOffset = 100.0f;
		this.blit(stack, x, y, texX, texY, 28, 32);
		this.itemRenderer.renderAndDecorateItem(icon, x + 6, y + 9);
		this.itemRenderer.blitOffset = 0.0f;
	}
	
	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
		int selected = this.menu.getSelected();
		ITextComponent title = 0 <= selected && selected < this.titles.size() ? this.titles.get(selected) : this.title;
		this.font.draw(stack, title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
		
		this.font.draw(stack, this.inventory.getDisplayName(), (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
	}
	
	@Override
	protected void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
		super.renderTooltip(stack, mouseX, mouseY);
		
		int d0 = mouseX - this.leftPos;
		int d1 = mouseY - this.topPos;
		
		int hovered = this.insideTab(d0, d1);
		if (hovered != -1) {
			ITextComponent title = 0 <= hovered && hovered < this.titles.size() ? this.titles.get(hovered) : this.title;
			this.renderTooltip(stack, title, mouseX, mouseY);
		}
		
		if (this.menu.getSelected() == 0) {
			int base = MathHelper.ceil((float) this.matchingItemsCache.size() * this.scrollOffs / 3.0f) * 3;
			for (int i = base; i < REQUESTS_COUNT + base && i < this.matchingItemsCache.size(); ++i) {
				int rx = i % REQUESTS_COLUMNS * SLOT_SPACING + TOP_MENU_START_X + 1;
				int ry = i / REQUESTS_COLUMNS * SLOT_SPACING + TOP_MENU_START_Y + 1;
				
				if (rx <= d0 && d0 < rx + SLOT_SIDE && ry <= d1 && d1 < ry + SLOT_SIDE) {
					List<ItemStack> fulfillingItems = this.matchingItemsCache.get(i);
					int index = (int)((this.renderTicks / (long) TIME_BETWEEN_ITEMS) % fulfillingItems.size());
					this.renderTooltip(stack, fulfillingItems.get(index), mouseX, mouseY);
				}
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		double d0 = mouseX - (double) this.leftPos;
		double d1 = mouseY - (double) this.topPos;
		
		if (button == 0 && this.canScroll() && this.isHoveringScrollBar(d0, d1)) {
			this.isScrolling = true;
			return true;
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX1, double mouseY1, int button, double mouseX2, double mouseY2) {
		if (this.isScrolling && this.canScroll()) {
			this.scrollOffs = ((float) mouseY1 - (float) this.topPos - (float) SCROLL_BAR_X - (float)(SCROLL_BAR_BUTTON_HEIGHT) * 0.5f) / (float) SCROLL_BAR_BG_HEIGHT_PRACTICAL;
			this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0f, 1.0f);
		}
		
		return super.mouseDragged(mouseX1, mouseY1, button, mouseX2, mouseY2);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			double d0 = mouseX - (double) this.leftPos;
			double d1 = mouseY - (double) this.topPos;
			this.isScrolling = false;
			
			int tab = this.insideTab(d0, d1);
			if (tab != -1 && tab != this.menu.getSelected()) {
				this.menu.setSelected(tab);
				IWNetwork.CHANNEL.sendToServer(new SSelectTab(tab));
				return true;
			}
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDist) {
		double d0 = mouseX - (double) this.leftPos;
		double d1 = mouseY - (double) this.topPos;
		
		if (this.canScroll() && this.isHoveringScrollBar(d0, d1)) {
			this.scrollOffs = this.scrollOffs - (float) scrollDist / (float) SCROLL_BAR_BG_HEIGHT_PRACTICAL * 2.0f;
			this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0f, 1.0f);
		}
		return super.mouseScrolled(mouseX, mouseY, scrollDist);
	}
	
	private boolean canScroll() {
		return this.menu.getSelected() == 0 && false;
	}
	
	private boolean isHoveringScrollBar(double mouseX, double mouseY) {
		return SCROLL_BAR_X <= mouseX && mouseX < SCROLL_BAR_X + SCROLL_BAR_BG_WIDTH && SCROLL_BAR_Y <= mouseY && mouseY < SCROLL_BAR_Y + SCROLL_BAR_BG_HEIGHT;
	}
	
	private int insideTab(double mouseX, double mouseY) {
		if (mouseY < - 28 || 0 <= mouseY || mouseX % 29 >= 28) return -1;
		int tab = (int) mouseX / 29;
		return 0 <= tab && tab < 3 ? tab : -1;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		++this.renderTicks;
		
		if (this.menu.isChanged()) {
			this.matchingItemsCache.clear();
			this.menu.getRequests().forEach(r -> this.matchingItemsCache.add(r.getItemsForDisplay()));		
			this.menu.setChanged(false);
		}
		
		int tab = this.menu.getSelected();
		boolean running = this.menu.isRunning();
		WidgetUtils.setActiveAndVisible(this.startButton, tab == 0 && !running);
		WidgetUtils.setActiveAndVisible(this.stopButton, tab == 0 && running);
		this.startButton.active = !this.menu.isFinished();
	}
	
	private void setRunning(Button button, boolean running) {
		IWNetwork.CHANNEL.sendToServer(new SSetRunning(running));
	}
	
}
