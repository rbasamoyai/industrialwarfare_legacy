package rbasamoyai.industrialwarfare.client.screen.resource_station;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.ResourceStationContainer;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.SResourceStationMessage;

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
	
	private boolean isScrolling = false;
	private float scrollOffs = 0.0f;
	
	public ResourceStationScreen(ResourceStationContainer menu, PlayerInventory playerInv, ITextComponent title) {
		super(menu, playerInv, title);
		this.imageHeight = 168;
		this.inventoryLabelY = this.imageHeight - 94;
	}
	
	@Override
	protected void init() {
		super.init();
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
			this.scrollTo(this.scrollOffs);
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
				IWNetwork.CHANNEL.sendToServer(new SResourceStationMessage(tab));
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
			this.scrollTo(this.scrollOffs);
		}
		return super.mouseScrolled(mouseX, mouseY, scrollDist);
	}
	
	private boolean canScroll() {
		return this.menu.getSelected() == 0 && false;
	}
	
	private boolean isHoveringScrollBar(double mouseX, double mouseY) {
		return SCROLL_BAR_X <= mouseX && mouseX < SCROLL_BAR_X + SCROLL_BAR_BG_WIDTH && SCROLL_BAR_Y <= mouseY && mouseY < SCROLL_BAR_Y + SCROLL_BAR_BG_HEIGHT;
	}
	
	private void scrollTo(float scroll) {
		
	}
	
	private int insideTab(double mouseX, double mouseY) {
		if (mouseY < - 28 || 0 <= mouseY || mouseX % 29 >= 28) return -1;
		int tab = (int) mouseX / 29;
		return 0 <= tab && tab < 3 ? tab : -1;
	}
	
}
