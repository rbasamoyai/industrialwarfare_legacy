package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import rbasamoyai.industrialwarfare.client.PlayerInfo;
import rbasamoyai.industrialwarfare.client.screen.ScreenUtils;
import rbasamoyai.industrialwarfare.client.screen.widgets.WidgetUtils;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.IScreenPage;
import rbasamoyai.industrialwarfare.client.screen.widgets.page.ScreenPageDecorator;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

public class DiplomacyStatusesListDecorator extends ScreenPageDecorator implements IFetchDiplomacyData {
	
	private static final Component PLAYER = new TranslatableComponent(DiplomacyScreen.TRANSLATION_KEY_ROOT + ".player");
	private static final Component CLEAR_CHANGES = new TranslatableComponent(DiplomacyScreen.TRANSLATION_KEY_ROOT + ".clear_changes");
	private static final Component COMMIT_CHANGES = new TranslatableComponent(DiplomacyScreen.TRANSLATION_KEY_ROOT + ".commit_changes");
	
	private static final int DECORATOR_WIDTH = 206;
	
	private static final int BUTTON_GUI_Y = 114;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_PADDING = 8;
	private static final int BUTTON_SPACING = 2;
	
	private static final int DISPLAY_OFFSET_X = 2;
	private static final int DISPLAY_OFFSET_Y = 2;
	
	private static final int PLAYER_CENTER_X = 48;
	private static final int TITLE_Y = 0;
	private static final int TITLE_HEIGHT = 8;
	private static final int THEIR_STATUS_CENTER_X = PLAYER_CENTER_X + 100;
	private static final int OUR_STATUS_CENTER_X = THEIR_STATUS_CENTER_X + 30;
	private static final int ENTRY_SPACING = 12;
	
	private static final float ICON_SCALE = 1.0f;
	
	private static final int PLAYER_NAME_OFFSET_X = 10;
	
	private static final int LOADING_ANIMATION_RATE = 5;
	
	private static final int TEXT_COLOR = 4210752;
	
	private final int maxRowsVisible;
	private final int leftPos;
	private final int topPos;
	
	private final Minecraft mc = this.getScreen().getMinecraft();
	private final Font font = this.mc.font;
	
	private final DiplomacyStatusWidget[][] dsWidgets;
	
	private final List<Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>>> sortedList = new ArrayList<>();
	
	private final IPressable pressable;
	private final INonScroll nonScroll;
	
	private List<Button> buttons = new ArrayList<>(2);
	
	private int topIndex = 0;
	private int tick = 0;
	private int loadingFrame = 0;
	
	public DiplomacyStatusesListDecorator(IScreenPage page, int maxRows, int leftPos, int topPos, IPressable pressable, Button.OnPress clearPressable, Button.OnPress commitPressable, INonScroll nonScroll) {
		super(page);
		
		this.maxRowsVisible = maxRows;
		this.leftPos = leftPos;
		this.topPos = topPos;
		
		this.dsWidgets = new DiplomacyStatusWidget[this.maxRowsVisible][2];
		
		this.pressable = pressable;
		this.nonScroll = nonScroll;
		
		for (int i = 0; i < this.dsWidgets.length; i++) {
			for (int j = 0; j < this.dsWidgets[i].length; j++) {
				int x = this.leftPos;
				DiplomacyStatusWidget.OnTooltip dsWidget$tooltip;
				switch (j) {
				case 1:
					x += OUR_STATUS_CENTER_X - DiplomacyStatusWidget.WIDGET_LENGTH / 2;
					dsWidget$tooltip = (widget, stack, mouseX, mouseY) -> {
						this.getScreen().renderTooltip(stack, DiplomacyScreen.OUR_STATUSES.get(widget.getStatus()), mouseX, mouseY);
					};
					break;
				default:
					x += THEIR_STATUS_CENTER_X - DiplomacyStatusWidget.WIDGET_LENGTH / 2;
					dsWidget$tooltip = (widget, stack, mouseX, mouseY) -> {
						this.getScreen().renderTooltip(stack, DiplomacyScreen.THEIR_STATUSES_COLORED.get(widget.getStatus()), mouseX, mouseY);
					};
				}
				
				int y = this.topPos + DISPLAY_OFFSET_Y + ENTRY_SPACING + i * ENTRY_SPACING;
				
				this.dsWidgets[i][j] = new DiplomacyStatusWidget(x, y, DiplomaticStatus.UNKNOWN, dsWidget$tooltip);
			}
		}
		
		int clearButtonWidth = this.font.width(CLEAR_CHANGES) + BUTTON_PADDING;
		this.buttons.add(new Button(
				this.leftPos,
				this.topPos + BUTTON_GUI_Y,
				clearButtonWidth,
				BUTTON_HEIGHT,
				CLEAR_CHANGES,
				clearPressable));
		
		this.buttons.add(new Button(
				this.leftPos + clearButtonWidth + BUTTON_SPACING,
				this.topPos + BUTTON_GUI_Y,
				this.font.width(COMMIT_CHANGES) + BUTTON_PADDING,
				BUTTON_HEIGHT,
				COMMIT_CHANGES,
				commitPressable));
		
		this.sortUsing(DiplomacyListComparator.noSort());
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		
		stack.pushPose();
		stack.translate(this.leftPos + DISPLAY_OFFSET_X, this.topPos + DISPLAY_OFFSET_Y, 0);
		
		this.font.draw(stack, PLAYER, (float) PLAYER_CENTER_X - (float) this.font.width(PLAYER) * 0.5f, (float) TITLE_Y, TEXT_COLOR);
		
		float theirStatusHoverWidth = (float) this.font.width(DiplomacyScreen.THEIR_STATUS_SHORT);
		float theirStatusHoverX = (float) THEIR_STATUS_CENTER_X - theirStatusHoverWidth * 0.5f;
		
		this.font.draw(stack, DiplomacyScreen.THEIR_STATUS_SHORT, theirStatusHoverX, (float) TITLE_Y, TEXT_COLOR);
		
		float ourStatusHoverWidth = (float) this.font.width(DiplomacyScreen.OUR_STATUS_SHORT);
		float ourStatusHoverX = (float) OUR_STATUS_CENTER_X - ourStatusHoverWidth * 0.5f;
		
		this.font.draw(stack, DiplomacyScreen.OUR_STATUS_SHORT, ourStatusHoverX, (float) TITLE_Y, TEXT_COLOR);
		
		for (int i = this.topIndex; i < this.topIndex + this.getVisibleRowCount(); i++) {
			int visualIndex = i - this.topIndex + 1;
			int drawY = visualIndex * ENTRY_SPACING;
			
			Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> entry = this.sortedList.get(i);
			
			PlayerIDTag tag = entry.getKey();
			
			Component tc;
			if (tag.isPlayer()) {
				GameProfile profile = PlayerInfo.get(tag.getUUID());
				if (profile == null) {
					tc = DiplomacyScreen.getLoadingText(this.loadingFrame);
				} else {
					String name = profile.getName();
					tc = name == null
							? DiplomacyScreen.COULD_NOT_LOAD.copy()
							: new TextComponent(name);
				}
				ScreenUtils.drawFace(tag, stack, 0, drawY, ICON_SCALE);
			} else {
				// TODO: draw/render something related to the NPC faction
				tc = DiplomacyScreen.DEBUG_NPC.copy();
			}
			this.font.draw(stack, tc, (float) PLAYER_NAME_OFFSET_X, (float) drawY, TEXT_COLOR);
		}
		
		stack.popPose();
		
		for (int i = 0; i < this.dsWidgets.length; i++) {
			for (int j = 0; j < this.dsWidgets[i].length; j++) {
				this.dsWidgets[i][j].render(stack, mouseX, mouseY, partialTicks);
			}
		}
		
		this.buttons.forEach(button -> button.render(stack, mouseX, mouseY, partialTicks));
		
		int titleY1 = this.topPos + TITLE_Y;
		int titleY2 = titleY1 + TITLE_HEIGHT;
		
		if (mouseY < titleY1 || titleY2 <= mouseY) return;
		
		int tsX1 = this.leftPos + (int) theirStatusHoverX;
		int tsX2 = this.leftPos + (int)(theirStatusHoverX + theirStatusHoverWidth);
		
		int osX1 = this.leftPos + (int) ourStatusHoverX;
		int osX2 = this.leftPos + (int)(ourStatusHoverX + ourStatusHoverWidth);
		
		if (tsX1 <= mouseX && mouseX < tsX2) {
			this.getScreen().renderTooltip(stack, DiplomacyScreen.THEIR_STATUS, mouseX, mouseY);
		}
		if (osX1 <= mouseX && mouseX < osX2) {
			this.getScreen().renderTooltip(stack, DiplomacyScreen.OUR_STATUS, mouseX, mouseY);
		}
	}
	
	@Override
	public void tick() {
		if (++this.tick >= LOADING_ANIMATION_RATE) {
			this.loadingFrame = (this.loadingFrame + 1) % 4;
			this.tick = 0;
		}
		super.tick();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int action) {
		double x1 = (double) this.leftPos;
		double x2 = x1 + (double) DECORATOR_WIDTH;
		double y1 = (double) this.topPos + ENTRY_SPACING;
		double y2 = y1 + (double)(ENTRY_SPACING * this.maxRowsVisible + ENTRY_SPACING);
		
		if (x1 <= mouseX && mouseX < x2 && y1 <= mouseY && mouseY < y2) {
			int i = Mth.floor(Mth.clamp((mouseY - y1) / (double) ENTRY_SPACING, 0.0d, (double) this.maxRowsVisible));
			if (i < this.getVisibleRowCount()) {
				this.pressable.onPress(this.sortedList.get(i + this.topIndex).getKey());
				this.setButtonsActive(false);
				return true;
			}
		}
		
		for (Button button : this.buttons) {
			if (button.mouseClicked(mouseX, mouseY, action)) return true;
		}
		
		return super.mouseClicked(mouseX, mouseY, action);
	}
	
	// In case there are too many entries, keys are available as a step-by-step 
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifier) {
		if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
			if (this.topIndex < this.getSize() - this.maxRowsVisible) {
				this.setTopIndex(this.topIndex + 1);
				this.nonScroll.onNonScroll(this.topIndex);
				return true;
			}
		}
		if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
			if (this.topIndex > 0) {
				this.setTopIndex(this.topIndex - 1);
				this.nonScroll.onNonScroll(this.topIndex);
				return true;
			}
		}
		
		return super.keyPressed(keyCode, scanCode, modifier);
	}
	
	public void setButtonsActive(boolean active) {
		this.buttons.forEach(button -> WidgetUtils.setActiveAndVisible(button, active));
	}
	
	public void setTopIndex(int topIndex) {
		this.topIndex = topIndex;
		this.updateDecorator();
	}
	
	public void sortUsing(DiplomacyListComparator... comparators) {
		this.sortedList.clear();
		this.getStatuses(this.getScreen()).entrySet().forEach(this.sortedList::add);
		for (DiplomacyListComparator sort : comparators) {
			this.sortedList.sort(sort);
		}
		
		this.updateDecorator();
	}
	
	public int getSize() {
		return this.sortedList.size();
	}
	
	private int getVisibleRowCount() {
		return Mth.clamp(this.getSize() - this.topIndex, 0, this.maxRowsVisible);
	}
	
	private void updateDecorator() {
		int visibleRowCount = this.getVisibleRowCount();
		
		for (int i = 0; i < this.dsWidgets.length; i++) {
			boolean visible = i < visibleRowCount;
			WidgetUtils.setActiveAndVisible(this.dsWidgets[i][0], visible);
			WidgetUtils.setActiveAndVisible(this.dsWidgets[i][1], visible);
			if (!visible) continue;
			
			Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> entry = this.sortedList.get(i + this.topIndex);
			Pair<DiplomaticStatus, DiplomaticStatus> statuses = entry.getValue();
			this.dsWidgets[i][0].setStatus(statuses.getFirst());
			this.dsWidgets[i][1].setStatus(statuses.getSecond());
			
			PlayerIDTag tag = entry.getKey();
			if (!tag.isPlayer()) continue;
			
			UUID uuid = tag.getUUID();
			if (!PlayerInfo.has(uuid)) {
				PlayerInfo.queueProfileFill(uuid, this.mc);
			}
		}
	}
	
	@FunctionalInterface
	public static interface IPressable {
		public void onPress(PlayerIDTag tag);
	}
	
	@FunctionalInterface
	public static interface INonScroll {
		public void onNonScroll(int newTopIndex);
	}
		
}
