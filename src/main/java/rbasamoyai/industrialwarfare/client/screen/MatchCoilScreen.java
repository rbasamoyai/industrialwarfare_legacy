package rbasamoyai.industrialwarfare.client.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.MatchCoilContainer;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.SMatchCoilSyncMessage;

public class MatchCoilScreen extends ContainerScreen<MatchCoilContainer> {

	private static final int SHEARS_TEX_X = 176;
	private static final int SHEARS_TEX_Y = 0;
	private static final int SHEARS_WIDTH = 9;
	private static final int SHEARS_HEIGHT = 28;
	private static final int SHEARS_BUTTON_HEIGHT = 15;
	private static final int SHEARS_GUI_Y = 22;
	private static final int CORD_TEX_X = 0;
	private static final int CORD_TEX_Y = 168;
	private static final int CORD_DARK_TEX_X = 0;
	private static final int CORD_DARK_TEX_Y = 180;
	private static final int CORD_WIDTH = 100;
	private static final int CORD_HEIGHT = 12;
	private static final int CORD_GUI_X = 17;
	private static final int CORD_GUI_Y = 38;
	
	private static final int BASE_CORD_LENGTH = MatchCoilContainer.MINIMUM_CORD_LEFT;
	private static final int TOTAL_CORD_LENGTH = MatchCoilContainer.TOTAL_CORD_LENGTH;
	
	private static final String KEY_DURABILITY = "item.durability";
	private static final String BASE_KEY = "gui." + IndustrialWarfare.MOD_ID + ".match_coil";
	private static final String KEY_CUT_LENGTH = BASE_KEY + ".cut_length";
	private static final String KEY_CUT_LENGTH_TICKS = KEY_CUT_LENGTH + ".ticks";
	private static final String KEY_REMAINING_COIL = BASE_KEY + ".remaining_coil";
	private static final String KEY_REMAINING_COIL_TICKS = KEY_REMAINING_COIL + ".ticks";
	
	private static final int REMAINING_COIL_TEXT_X = 8;
	private static final int REMAINING_COIL_TEXT_Y = 60;
	
	private static final ResourceLocation MATCH_COIL_SCREEN = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/match_coil.png");
	
	private boolean isScrolling = false;
	private float scrollOffs = 0.0f;
	
	public MatchCoilScreen(MatchCoilContainer container, PlayerInventory playerInv, ITextComponent title) {
		super(container, playerInv, title);
		this.imageWidth = 176;
		this.imageHeight = 168;
		this.scrollOffs = (float) this.menu.getCutLength() / (float) TOTAL_CORD_LENGTH;
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
	
	@Override
	protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
		TextureManager texManager = this.minecraft.textureManager;
		texManager.bind(MATCH_COIL_SCREEN);
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		float cordFullness = (float)(this.menu.getCurrentCoilLength() - BASE_CORD_LENGTH) / (float) TOTAL_CORD_LENGTH;
		cordFullness = MathHelper.clamp(cordFullness, 0.0f, 1.0f);
		
		int cordX = this.leftPos + CORD_GUI_X;
		int cordY = this.topPos + CORD_GUI_Y;
		this.blit(stack, cordX, cordY, CORD_DARK_TEX_X, CORD_DARK_TEX_Y, (int)((float) CORD_WIDTH * cordFullness), CORD_HEIGHT);
		
		int cutLengthPx = (int)((float) CORD_WIDTH * this.menu.getCutLengthScaled());
		this.blit(stack, cordX, cordY, CORD_TEX_X, CORD_TEX_Y, cutLengthPx, CORD_HEIGHT);
		
		this.blit(stack, this.leftPos + CORD_GUI_X - SHEARS_WIDTH / 2 + cutLengthPx, this.topPos + SHEARS_GUI_Y, SHEARS_TEX_X, SHEARS_TEX_Y, SHEARS_WIDTH, SHEARS_HEIGHT);
	}
	
	@Override
	protected void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
		super.renderTooltip(stack, mouseX, mouseY);
		double d0 = mouseX - (double) this.leftPos;
		double d1 = mouseY - (double) this.topPos;
		if (this.hoveringShears(d0, d1)) {
			ItemStack shears = this.menu.getShears();
			List<ITextComponent> shearsTooltip = new ArrayList<>();
			shearsTooltip.add(new TranslationTextComponent(KEY_DURABILITY, shears.getMaxDamage() - shears.getDamageValue(), shears.getMaxDamage()));
			int cutLength = this.menu.getCutLength();
			if (Screen.hasShiftDown()) {
				shearsTooltip.add(new TranslationTextComponent(KEY_CUT_LENGTH_TICKS, cutLength));
			} else {
				int minutes = MathHelper.floor((float) cutLength / 1200.0f);
				float seconds = (float)(cutLength % 1200) / 20.0f;
				shearsTooltip.add(new TranslationTextComponent(KEY_CUT_LENGTH, minutes, seconds));
			}
			this.renderComponentTooltip(stack, shearsTooltip, mouseX, mouseY);
		}
	}
	
	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
		super.renderLabels(stack, mouseX, mouseY);
		ITextComponent remainingCoil;
		int coilLength = this.menu.getCurrentCoilLength();
		if (Screen.hasShiftDown()) {
			remainingCoil = new TranslationTextComponent(KEY_REMAINING_COIL_TICKS, coilLength);
		} else {
			int hours = MathHelper.floor((float) coilLength / 72000.0f);
			int minutes = MathHelper.floor((float)(coilLength % 72000) / 1200.0f);
			float seconds = (float)(coilLength % 1200) / 20.0f;
			remainingCoil = new TranslationTextComponent(KEY_REMAINING_COIL, hours, minutes, seconds);
		}
		this.font.draw(stack, remainingCoil, REMAINING_COIL_TEXT_X, REMAINING_COIL_TEXT_Y, 4210752);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			double d0 = mouseX - (double) this.leftPos;
			double d1 = mouseY - (double) this.topPos;
			
			if (this.hoveringShears(d0, d1)) {
				this.isScrolling = true;
				return true;
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
		double d0 = mouseX1 - (double) this.leftPos;
		if (this.isScrolling) {
			this.scrollOffs = ((float) d0 - (float) CORD_GUI_X) / (float) CORD_WIDTH;
			float cordFullness = (float)(this.menu.getCurrentCoilLength() - BASE_CORD_LENGTH) / (float) TOTAL_CORD_LENGTH;
			cordFullness = MathHelper.clamp(cordFullness, 0.0f, 1.0f);
			this.scrollOffs = MathHelper.clamp(this.scrollOffs, 0.0f, cordFullness);
			this.scrollTo(this.scrollOffs);
		}
		
		return super.mouseDragged(mouseX1, mouseY1, button, mouseX2, mouseY2);
	}
	
	private void scrollTo(float scrollOffs) {
		IWNetwork.CHANNEL.sendToServer(new SMatchCoilSyncMessage(BASE_CORD_LENGTH + (int)((float) TOTAL_CORD_LENGTH * scrollOffs)));
	}
	
	private boolean hoveringShears(double mouseX, double mouseY) {
		int shearsX = CORD_GUI_X - SHEARS_WIDTH / 2 + (int)((float) CORD_WIDTH * this.menu.getCutLengthScaled());
		return SHEARS_GUI_Y <= mouseY && mouseY < SHEARS_GUI_Y + SHEARS_BUTTON_HEIGHT && shearsX <= mouseX && mouseX < shearsX + SHEARS_WIDTH; 
	}
	
}
