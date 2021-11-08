package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;

public class DiplomacyStatusWidget extends Widget {
	
	public static final int WIDGET_LENGTH = 8;
	
	private static final int STATUS_ICONS_TEX_X = 238;
	private static final int STATUS_ICONS_TEX_START_Y = 116;
	
	private final ITooltip onTooltip;
	
	private DiplomaticStatus status;
	
	public DiplomacyStatusWidget(int x, int y, DiplomaticStatus initialStatus, ITooltip onTooltip) {
		super(x, y, WIDGET_LENGTH, WIDGET_LENGTH, StringTextComponent.EMPTY);
		
		this.status = initialStatus;
		this.onTooltip = onTooltip;
	}
	
	@Override
	public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bind(DiplomacyScreen.DIPLOMACY_GUI);
		
		int texY = STATUS_ICONS_TEX_START_Y + (int) this.status.getValue() * WIDGET_LENGTH;
		this.blit(stack, this.x, this.y, STATUS_ICONS_TEX_X, texY, this.width, this.height);
		
		if (this.isHovered) {
			this.onTooltip.onTooltip(this, stack, mouseX, mouseY);
		}
	}
	
	public void setStatus(DiplomaticStatus status) { this.status = status; }
	public DiplomaticStatus getStatus() { return this.status; }
	
	@FunctionalInterface
	public static interface ITooltip {
		public void onTooltip(DiplomacyStatusWidget widget, MatrixStack stack, int mouseX, int mouseY);
	}
	
}
