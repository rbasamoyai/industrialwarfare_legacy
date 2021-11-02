package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.IScreenPage;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.ScreenPageDecorator;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;
import rbasamoyai.industrialwarfare.utils.WidgetUtils;

public class DiplomacyStatusesListDecorator extends ScreenPageDecorator {

	/* DEBUG */ private static final ITextComponent DEBUG_NPC = new StringTextComponent("*DEBUG*").withStyle(TextFormatting.DARK_RED).append(new StringTextComponent(" An NPC Faction").withStyle(TextFormatting.RESET));
	
	private static final String TRANSLATION_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".diplomacy";
	private static final ITextComponent PLAYER = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".player");
	private static final ITextComponent THEIR_STATUS = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".their_status");
	private static final ITextComponent THEIR_STATUS_SHORT = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".their_status.short");
	private static final ITextComponent OUR_STATUS = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".our_status");
	private static final ITextComponent OUR_STATUS_SHORT = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".our_status.short");
	
	private static final int DISPLAY_OFFSET_X = 2;
	private static final int DISPLAY_OFFSET_Y = 2;
	
	private static final int PLAYER_CENTER_X = 48;
	private static final int TITLE_Y = 0;
	private static final int TITLE_HEIGHT = 8;
	private static final int THEIR_STATUS_CENTER_X = PLAYER_CENTER_X + 80;
	private static final int OUR_STATUS_CENTER_X = THEIR_STATUS_CENTER_X + 30;
	private static final int ENTRY_SPACING = 12;
	
	private static final int PLAYER_NAME_OFFSET_X = 10;
	
	private static final int PLAYER_FACE_TEX_X = 8;
	private static final int PLAYER_FACE_TEX_Y = 8;
	private static final int PLAYER_FACE_WIDTH = 8;
	private static final int PLAYER_FACE_HEIGHT = 8;
	
	private static final int TEXT_COLOR = 4210752;
	
	private final int maxRowsVisible;
	private final int startX;
	private final int startY;
	
	private final Minecraft mc = this.getScreen().getMinecraft();
	private final FontRenderer font = this.mc.font;
	
	private final DiplomacyStatusWidget[][] dsWidgets;
	
	private final List<Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>>> sortedList = new ArrayList<>();
	
	private int topIndex = 0;
	
	public DiplomacyStatusesListDecorator(IScreenPage page, int maxRows, int startX, int startY) {
		super(page);
		
		this.maxRowsVisible = maxRows;
		this.startX = startX;
		this.startY = startY;
		
		this.dsWidgets = new DiplomacyStatusWidget[this.maxRowsVisible][2];
		
		for (int i = 0; i < this.dsWidgets.length; i++) {
			for (int j = 0; j < this.dsWidgets[i].length; j++) {
				int x = this.startX;
				DiplomacyStatusWidget.ITooltip dsWidget$tooltip;
				switch (j) {
				case 1:
					x += OUR_STATUS_CENTER_X - DiplomacyStatusWidget.WIDGET_LENGTH / 2;
					dsWidget$tooltip = (widget, stack, mouseX, mouseY) -> {
						DiplomaticStatus status = widget.getStatus();
						ITextComponent text =
								OUR_STATUS
								.copy()
								.append(new StringTextComponent(": "))
								.append(
										new TranslationTextComponent(TRANSLATION_KEY_ROOT + "." + status.getName())
										.withStyle(status.getStyle())
										);
						this.getScreen().renderTooltip(stack, text, mouseX, mouseY);
					};
					break;
				default:
					x += THEIR_STATUS_CENTER_X - DiplomacyStatusWidget.WIDGET_LENGTH / 2;
					dsWidget$tooltip = (widget, stack, mouseX, mouseY) -> {
						DiplomaticStatus status = widget.getStatus();
						ITextComponent text =
								OUR_STATUS
								.copy()
								.append(new StringTextComponent(": "))
								.append(
										new TranslationTextComponent(TRANSLATION_KEY_ROOT + "." + status.getName())
										.withStyle(status.getStyle())
										);
						this.getScreen().renderTooltip(stack, text, mouseX, mouseY);
					};
				}
				
				int y = this.startY + DISPLAY_OFFSET_Y + ENTRY_SPACING + i * ENTRY_SPACING;
				
				this.dsWidgets[i][j] = new DiplomacyStatusWidget(x, y, DiplomaticStatus.UNKNOWN, dsWidget$tooltip);
			}
		}
		
		this.sortUsing(DiplomacyListComparator.noSort());
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		
		ClientPlayNetHandler netHandler = this.mc.getConnection();
		TextureManager texManager = this.mc.getTextureManager();
		
		stack.pushPose();
		stack.translate(this.startX + DISPLAY_OFFSET_X, this.startY + DISPLAY_OFFSET_Y, 0);
		
		this.font.draw(stack, PLAYER, (float) PLAYER_CENTER_X - (float) this.font.width(PLAYER) * 0.5f, (float) TITLE_Y, TEXT_COLOR);
		
		float theirStatusHoverWidth = (float) this.font.width(THEIR_STATUS_SHORT);
		float theirStatusHoverX = (float) THEIR_STATUS_CENTER_X - theirStatusHoverWidth * 0.5f;
		
		this.font.draw(stack, THEIR_STATUS_SHORT, theirStatusHoverX, (float) TITLE_Y, TEXT_COLOR);
		
		float ourStatusHoverWidth = (float) this.font.width(OUR_STATUS_SHORT);
		float ourStatusHoverX = (float) OUR_STATUS_CENTER_X - ourStatusHoverWidth * 0.5f;
		
		this.font.draw(stack, OUR_STATUS_SHORT, ourStatusHoverX, (float) TITLE_Y, TEXT_COLOR);
		
		for (int i = this.topIndex; i < this.topIndex + this.getVisibleRowCount(); i++) {
			int visualIndex = i - this.topIndex + 1;
			int drawY = visualIndex * ENTRY_SPACING;
			
			Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> entry = this.sortedList.get(i);
			
			PlayerIDTag tag = entry.getKey();
			
			if (tag.isPlayer()) {
				NetworkPlayerInfo playerInfo = netHandler.getPlayerInfo(tag.getUUID());
				texManager.bind(playerInfo.getSkinLocation());
				
				this.getScreen().blit(stack, 0, drawY, PLAYER_FACE_TEX_X, PLAYER_FACE_TEX_Y, PLAYER_FACE_WIDTH, PLAYER_FACE_HEIGHT);
				this.font.draw(stack, new StringTextComponent(playerInfo.getProfile().getName()), (float) PLAYER_NAME_OFFSET_X, (float) drawY, TEXT_COLOR);
			} else {
				// TODO: draw/render something related to the NPC faction
				this.font.draw(stack, DEBUG_NPC, (float) PLAYER_NAME_OFFSET_X, (float) drawY, TEXT_COLOR);
			}
		}
		
		stack.popPose();
		
		for (int i = 0; i < this.dsWidgets.length; i++) {
			for (int j = 0; j < this.dsWidgets[i].length; j++) {
				this.dsWidgets[i][j].render(stack, mouseX, mouseY, partialTicks);
			}
		}
		
		int titleY1 = this.startY + TITLE_Y;
		int titleY2 = titleY1 + TITLE_HEIGHT;
		
		if (mouseY < titleY1 || titleY2 <= mouseY) return;
		
		int tsX1 = this.startX + (int) theirStatusHoverX;
		int tsX2 = this.startX + (int)(theirStatusHoverX + theirStatusHoverWidth);
		
		int osX1 = this.startX + (int) ourStatusHoverX;
		int osX2 = this.startX + (int)(ourStatusHoverX + ourStatusHoverWidth);
		
		if (tsX1 <= mouseX && mouseX < tsX2) {
			this.getScreen().renderTooltip(stack, THEIR_STATUS, mouseX, mouseY);
		}
		if (osX1 <= mouseX && mouseX < osX2) {
			this.getScreen().renderTooltip(stack, OUR_STATUS, mouseX, mouseY);
		}
	}
	
	public void setTopIndex(int topIndex) {
		this.topIndex = topIndex;
		this.updateWidgets();
	}
	
	public void sortUsing(DiplomacyListComparator... comparators) {
		Screen screen = this.getScreen();
		if (!(screen instanceof DiplomacyScreen)) return;
		DiplomacyScreen diplomacyScreen = (DiplomacyScreen) screen;
		
		Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> statusesUnsorted = diplomacyScreen.getMenu().getDiplomaticStatuses();
		this.sortedList.clear();
		statusesUnsorted.entrySet().forEach(this.sortedList::add);
		for (DiplomacyListComparator sort : comparators) {
			this.sortedList.sort(sort);
		}
		
		this.updateWidgets();
	}
	
	private int getVisibleRowCount() {
		return MathHelper.clamp(this.sortedList.size() - this.topIndex, 0, this.maxRowsVisible);
	}
	
	private void updateWidgets() {
		int visibleRowCount = this.getVisibleRowCount();
		
		for (int i = 0; i < this.dsWidgets.length; i++) {
			if (i >= visibleRowCount) {
				WidgetUtils.setActiveAndVisible(this.dsWidgets[i][0], false);
				WidgetUtils.setActiveAndVisible(this.dsWidgets[i][1], false);
				continue;
			}
			
			Pair<DiplomaticStatus, DiplomaticStatus> statuses = this.sortedList.get(i).getValue();
			this.dsWidgets[i][0].setStatus(statuses.getFirst());
			this.dsWidgets[i][1].setStatus(statuses.getSecond());
		}
	}
	
}
