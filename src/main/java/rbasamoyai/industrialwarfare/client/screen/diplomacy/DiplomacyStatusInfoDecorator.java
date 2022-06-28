package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import rbasamoyai.industrialwarfare.client.PlayerInfo;
import rbasamoyai.industrialwarfare.client.screen.ScreenUtils;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.IScreenPage;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.ScreenPageDecorator;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

public class DiplomacyStatusInfoDecorator extends ScreenPageDecorator implements IFetchDiplomacyData {
	
	private static final int ICON_WINDOW_TEX_X = 238;
	private static final int ICON_WINDOW_TEX_Y = 30;
	private static final int ICON_WINDOW_WIDTH = 18;
	private static final int ICON_WINDOW_HEIGHT = 18;
	private static final int ICON_OFFSET_X = 1;
	private static final int ICON_OFFSET_Y = 1;
	private static final float ICON_SCALED = 2.0f;
	
	private static final int NAME_TEXT_X = ICON_WINDOW_WIDTH + 2;
	private static final int NAME_TEXT_Y = 5;
	
	private static final int LINE_SPACING = 10;
	
	private static final int THEIR_STATUS_TEXT_X = 0;
	private static final int THEIR_STATUS_TEXT_Y = ICON_WINDOW_HEIGHT + 3;
	
	private static final int OUR_STATUS_TEXT_X = 0;
	private static final int OUR_STATUS_TEXT_Y = THEIR_STATUS_TEXT_Y + LINE_SPACING;
	
	private static final int STATUS_BUTTON_X = 0;
	private static final int STATUS_TEXT_X = STATUS_BUTTON_X + 10;
	private static final int STATUS_ALLY_Y = OUR_STATUS_TEXT_Y + LINE_SPACING;
	private static final int STATUS_NEUTRAL_Y = STATUS_ALLY_Y + LINE_SPACING;
	private static final int STATUS_ENEMY_Y = STATUS_NEUTRAL_Y + LINE_SPACING;
	
	private static final int CLOSE_BUTTON_GUI_X = 217;
	private static final int CLOSE_BUTTON_GUI_Y = 0;
	private static final int CLOSE_BUTTON_WIDTH = 7;
	private static final int CLOSE_BUTTON_HEIGHT = 7;
	private static final int CLOSE_BUTTON_TEX_X = 238;
	private static final int CLOSE_BUTTON_TEX_Y_START = 148; 
	
	private static final int TEXT_COLOR = 4210752;
	
	private final int leftPos;
	private final int topPos;
	private final Minecraft mc;
	private final Font font;
	
	private List<DiplomacyStatusButton> buttons = new ArrayList<>();
	private Button closeButton;
	
	private PlayerIDTag tag = PlayerIDTag.NO_OWNER;
	
	public DiplomacyStatusInfoDecorator(IScreenPage page,
			int leftPos,
			int topPos,
			Button.OnPress callback,
			Function<DiplomaticStatus, DiplomacyStatusButton.OnDisplay> displayProvider,
			Function<DiplomaticStatus, DiplomacyStatusButton.OnPress> pressableProvider) {
		super(page);
		
		this.leftPos = leftPos;
		this.topPos = topPos;
		this.mc = this.getScreen().getMinecraft();
		this.font = this.mc.font;
		
		this.buttons.add(new DiplomacyStatusButton(
				this.leftPos + STATUS_BUTTON_X,
				this.topPos + STATUS_ALLY_Y,
				displayProvider.apply(DiplomaticStatus.ALLY),
				pressableProvider.apply(DiplomaticStatus.ALLY)));
		
		this.buttons.add(new DiplomacyStatusButton(
				this.leftPos + STATUS_BUTTON_X,
				this.topPos + STATUS_NEUTRAL_Y,
				displayProvider.apply(DiplomaticStatus.NEUTRAL),
				pressableProvider.apply(DiplomaticStatus.NEUTRAL)));
		
		this.buttons.add(new DiplomacyStatusButton(
				this.leftPos + STATUS_BUTTON_X,
				this.topPos + STATUS_ENEMY_Y,
				displayProvider.apply(DiplomaticStatus.ENEMY),
				pressableProvider.apply(DiplomaticStatus.ENEMY)));
		
		this.closeButton = new ImageButton(
				this.leftPos + CLOSE_BUTTON_GUI_X,
				this.topPos + CLOSE_BUTTON_GUI_Y,
				CLOSE_BUTTON_WIDTH,
				CLOSE_BUTTON_HEIGHT,
				CLOSE_BUTTON_TEX_X,
				CLOSE_BUTTON_TEX_Y_START,
				CLOSE_BUTTON_HEIGHT,
				DiplomacyScreen.DIPLOMACY_GUI,
				button -> {
					this.setTag(PlayerIDTag.NO_OWNER);
					callback.onPress(button);
				});
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		
		if (this.tag.equals(PlayerIDTag.NO_OWNER)) return;
		
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, DiplomacyScreen.DIPLOMACY_GUI);
		
		stack.pushPose();
		stack.translate(this.leftPos, this.topPos, 0);
		this.getScreen().blit(stack, 0, 0, ICON_WINDOW_TEX_X, ICON_WINDOW_TEX_Y, ICON_WINDOW_WIDTH, ICON_WINDOW_HEIGHT);
		
		Component tc;
		if (this.tag.isPlayer()) {
			GameProfile profile = PlayerInfo.get(this.tag.getUUID());
			if (profile == null) {
				tc = DiplomacyScreen.getLoadingText(0);
			} else {
				String name = profile.getName();
				tc = name == null
						? DiplomacyScreen.COULD_NOT_LOAD.copy()
						: new TextComponent(name);
			}
			ScreenUtils.drawFace(this.tag, stack, ICON_OFFSET_X, ICON_OFFSET_Y, ICON_SCALED);
		} else {
			tc = DiplomacyScreen.DEBUG_NPC.copy();
		}
		
		this.font.draw(stack, tc, (float) NAME_TEXT_X, (float) NAME_TEXT_Y, TEXT_COLOR);
		
		Pair<DiplomaticStatus, DiplomaticStatus> statuses = this.getStatuses(this.getScreen()).get(this.tag);
		
		boolean noDiplo = false;
		if (statuses.getFirst() != DiplomaticStatus.UNKNOWN && statuses.getSecond() != DiplomaticStatus.UNKNOWN) {
			Component theirStatus = DiplomacyScreen.THEIR_STATUSES.get(statuses.getFirst());
			this.font.draw(stack, theirStatus, (float) THEIR_STATUS_TEXT_X, (float) THEIR_STATUS_TEXT_Y, TEXT_COLOR);
			
			this.font.draw(stack, DiplomacyScreen.OUR_STATUS, (float) OUR_STATUS_TEXT_X, (float) OUR_STATUS_TEXT_Y, TEXT_COLOR);
			this.font.draw(stack, DiplomacyScreen.STATUSES.get(DiplomaticStatus.ALLY), (float) STATUS_TEXT_X, (float) STATUS_ALLY_Y, TEXT_COLOR);
			this.font.draw(stack, DiplomacyScreen.STATUSES.get(DiplomaticStatus.NEUTRAL), (float) STATUS_TEXT_X, (float) STATUS_NEUTRAL_Y, TEXT_COLOR);
			this.font.draw(stack, DiplomacyScreen.STATUSES.get(DiplomaticStatus.ENEMY), (float) STATUS_TEXT_X, (float) STATUS_ENEMY_Y, TEXT_COLOR);
		} else {
			this.font.draw(stack, DiplomacyScreen.NO_DIPLOMACY_AVAILABLE, (float) THEIR_STATUS_TEXT_X, (float) THEIR_STATUS_TEXT_Y, TEXT_COLOR);
			noDiplo = true;
		}
		
		stack.popPose();
		
		this.closeButton.render(stack, mouseX, mouseY, partialTicks);
		
		if (!noDiplo) {
			this.buttons.forEach(dsb -> dsb.render(stack, mouseX, mouseY, partialTicks));
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int action) {
		for (DiplomacyStatusButton button : this.buttons) {
			if (button.mouseClicked(mouseX, mouseY, action)) return true;
		}
		return this.closeButton.mouseClicked(mouseX, mouseY, action) ? true : super.mouseClicked(mouseX, mouseY, action);
	}
	
	
	
	public void setTag(PlayerIDTag tag) {
		this.tag = tag;
		this.buttons.forEach(dsb -> dsb.setTag(tag));
	}
	
	public void setStatusBuffer(DiplomaticStatus status) {
		Screen screen = this.getScreen();
		if (!(screen instanceof DiplomacyScreen)) return;
	}
	
}
