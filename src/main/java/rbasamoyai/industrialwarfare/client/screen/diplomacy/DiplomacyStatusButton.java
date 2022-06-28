package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

public class DiplomacyStatusButton extends Button {

	private static final int BUTTON_WIDTH = 8;
	private static final int BUTTON_HEIGHT = 8;
	private static final int BUTTON_TEX_X = 238;
	private static final int BUTTON_TEX_Y_START = 48;
	
	private final OnDisplay display;
	private final OnPress pressable;
	
	private PlayerIDTag tag = PlayerIDTag.NO_OWNER;
	
	public DiplomacyStatusButton(int x, int y, OnDisplay display, OnPress pressable) {
		super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, TextComponent.EMPTY, null);
		
		this.display = display;
		this.pressable = pressable;
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShaderTexture(0, DiplomacyScreen.DIPLOMACY_GUI);
		
		int y = BUTTON_TEX_Y_START;
		switch (this.display.display(this.tag)) {
		case NOT_SELECTED:
			break;
		case SELECTED:
			y += BUTTON_HEIGHT;
			break;
		case PREVIOUS:
			y += BUTTON_HEIGHT * 2;
			break;
		}
		
		this.blit(stack, this.x, this.y, BUTTON_TEX_X, y, this.width, this.height);
	}
	
	@Override
	public void onPress() {
		this.pressable.onPress(this.tag);
	}
	
	public void setTag(PlayerIDTag tag) { this.tag = tag; }
	
	@FunctionalInterface
	public static interface OnDisplay {
		public DisplayType display(PlayerIDTag tag);
	}
	
	@FunctionalInterface
	public static interface OnPress {
		public void onPress(PlayerIDTag tag);
	}
	
	public static enum DisplayType {
		NOT_SELECTED,
		SELECTED,
		PREVIOUS
	}
	
}
