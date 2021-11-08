package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

public class DiplomacyStatusButton extends Button {

	private static final int BUTTON_WIDTH = 8;
	private static final int BUTTON_HEIGHT = 8;
	private static final int BUTTON_TEX_X = 238;
	private static final int BUTTON_TEX_Y_START = 48;
	
	private final IDisplay display;
	private final IPressable pressable;
	
	private PlayerIDTag tag = PlayerIDTag.NO_OWNER;
	
	public DiplomacyStatusButton(int x, int y, IDisplay display, IPressable pressable) {
		super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, StringTextComponent.EMPTY, null);
		
		this.display = display;
		this.pressable = pressable;
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		mc.textureManager.bind(DiplomacyScreen.DIPLOMACY_GUI);
		
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
	public static interface IDisplay {
		public DisplayType display(PlayerIDTag tag);
	}
	
	@FunctionalInterface
	public static interface IPressable {
		public void onPress(PlayerIDTag tag);
	}
	
	public static enum DisplayType {
		NOT_SELECTED,
		SELECTED,
		PREVIOUS
	}
	
}
