package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;

public class DiplomacyStatusWidget extends AbstractWidget {
	
	public static final int WIDGET_LENGTH = 8;
	
	private static final int STATUS_ICONS_TEX_X = 238;
	private static final int STATUS_ICONS_TEX_START_Y = 116;
	
	private final OnTooltip onTooltip;
	
	private DiplomaticStatus status;
	
	public DiplomacyStatusWidget(int x, int y, DiplomaticStatus initialStatus, OnTooltip onTooltip) {
		super(x, y, WIDGET_LENGTH, WIDGET_LENGTH, TextComponent.EMPTY);
		
		this.status = initialStatus;
		this.onTooltip = onTooltip;
	}
	
	@Override
	public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShaderTexture(0, DiplomacyScreen.DIPLOMACY_GUI);
		
		int texY = STATUS_ICONS_TEX_START_Y + (int) this.status.getValue() * WIDGET_LENGTH;
		this.blit(stack, this.x, this.y, STATUS_ICONS_TEX_X, texY, this.width, this.height);
		
		if (this.isHovered) {
			this.onTooltip.onTooltip(this, stack, mouseX, mouseY);
		}
	}
	
	public void setStatus(DiplomaticStatus status) { this.status = status; }
	public DiplomaticStatus getStatus() { return this.status; }
	
	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
	}
	
	@FunctionalInterface
	public static interface OnTooltip {
		public void onTooltip(DiplomacyStatusWidget widget, PoseStack stack, int mouseX, int mouseY);
	}
	
}
