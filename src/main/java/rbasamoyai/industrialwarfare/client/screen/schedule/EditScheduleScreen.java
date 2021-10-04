package rbasamoyai.industrialwarfare.client.screen.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.IntStream;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.containers.schedule.EditScheduleContainer;
import rbasamoyai.industrialwarfare.core.network.IWNetwork;
import rbasamoyai.industrialwarfare.core.network.messages.SEditScheduleSyncMessage;
import rbasamoyai.industrialwarfare.utils.ScheduleUtils;
import rbasamoyai.industrialwarfare.utils.TextureUtils;
import rbasamoyai.industrialwarfare.utils.TimeUtils;
import rbasamoyai.industrialwarfare.utils.TooltipUtils;
import rbasamoyai.industrialwarfare.utils.WidgetUtils;

public class EditScheduleScreen extends ContainerScreen<EditScheduleContainer> {

	private static final ResourceLocation SCHEDULE_GUI = new ResourceLocation(IndustrialWarfare.MOD_ID, "textures/gui/edit_schedule.png");
	
	private static final String TRANSLATION_KEY_ROOT = "gui." + IndustrialWarfare.MOD_ID + ".edit_schedule";
	private static final String TIME_FORMAT_1_KEY = TRANSLATION_KEY_ROOT + ".time_format1";
	private static final String TIME_FORMAT_2_KEY = TRANSLATION_KEY_ROOT + ".time_format2";
	private static final IFormattableTextComponent INFO_TITLE = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".info");
	private static final IFormattableTextComponent TIME_FIELD = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".time");
	private static final IFormattableTextComponent START_TIME_field = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".start_time");
	private static final IFormattableTextComponent END_TIME_FIELD = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".end_time");
	private static final IFormattableTextComponent SHIFT_LENGTH_FIELD = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".shift_length");
	private static final IFormattableTextComponent ALLOTTED_MINUTES_FIELD = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".minutes_allotted");
	private static final IFormattableTextComponent ALLOTTED_SHIFTS_FIELD = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".shifts_allotted");
	private static final IFormattableTextComponent PROMPT = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".prompt");
	private static final IFormattableTextComponent TOOLTIP_ADD_SHIFT = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".tooltip.add_shift");
	private static final IFormattableTextComponent TOOLTIP_REMOVE_SHIFT = new TranslationTextComponent(TRANSLATION_KEY_ROOT + ".tooltip.remove_shift");
	
	private static final IFormattableTextComponent[] DAY_NUMBERS = IntStream.rangeClosed(1, 7)
			.boxed()
			.map(i -> new StringTextComponent(Integer.toString(i)))
			.toArray(sz -> new IFormattableTextComponent[sz]);
	
	private static final int TEXTURE_SIZE = 256;
	
	private static final int DAY_NUM_START_X = 8;
	private static final int DAY_NUM_Y = 19;
	private static final int DAY_NUM_SPACING = 20;
	
	private static final int TEXT_SPACING = 9;
	private static final int INFO_TITLE_Y = 57;
	private static final int INFO_X = 6;
	private static final int INFO_START_Y = INFO_TITLE_Y + TEXT_SPACING;
	private static final int INFO_WIDTH = 153 - INFO_X * 2;
	
	private static final int SCHEDULE_START_X = 6;
	private static final int SCHEDULE_Y = 33;
	private static final int SCHEDULE_LENGTH = 140;
	private static final int SCHEDULE_HEIGHT = 7;
	private static final int TOP_MARKERS_GUI_Y = 27;
	private static final int BOTTOM_MARKERS_GUI_Y = SCHEDULE_Y;
	
	private static final int TIME_MARKER_TEX_X = 162;
	private static final int TIME_MARKER_TEX_Y = 0;
	private static final int TIME_MARKER_WIDTH = 5;
	private static final int TIME_MARKER_HEAD_HEIGHT = 6;
	private static final int TIME_MARKER_HEIGHT = 13;
	
	private static final int ADD_MARKER_TEX_X = 153;
	private static final int ADD_MARKER_TEX_Y = 23;
	private static final int ADD_MARKER_WIDTH = 5;
	private static final int ADD_MARKER_TIP_HEIGHT = 13;
	private static final int ADD_SHIFT_BUTTON_TEX_Y = ADD_MARKER_TEX_Y + ADD_MARKER_TIP_HEIGHT;
	private static final int ADD_SHIFT_BUTTON_HEIGHT = 5;
	private static final int ADD_MARKER_HEIGHT = ADD_MARKER_TIP_HEIGHT + ADD_SHIFT_BUTTON_HEIGHT;
	
	private static final int REMOVE_SHIFT_BUTTON_TEX_X = 153;
	private static final int REMOVE_SHIFT_BUTTON_TEX_Y = 13;
	private static final int REMOVE_SHIFT_BUTTON_WIDTH = 5;
	private static final int REMOVE_SHIFT_BUTTON_HEIGHT = 5;
	
	private static final int SHIFT_START_MARKER_TEX_X = 153;
	private static final int SHIFT_START_MARKER_TEX_Y = 0;
	private static final int SHIFT_START_MARKER_WIDTH = 3;
	private static final int SHIFT_START_MARKER_HEIGHT = 13;
	
	private static final int SHIFT_END_MARKER_TEX_X = 158;
	private static final int SHIFT_END_MARKER_TEX_Y = 0;
	private static final int SHIFT_END_MARKER_WIDTH = 3;
	private static final int SHIFT_END_MARKER_HEIGHT = 13;
	
	private static final int SHIFT_OVERLAY_COLOR_X = 0;
	private static final int DARK_TEXT_COLOR_X = 1;
	private static final int LIGHT_TEXT_COLOR_X = 2;
	private static final int COLORS_Y = 128;
	private static final List<Pair<Integer, Integer>> COLOR_COORDS = Arrays.asList(
			Pair.of(SHIFT_OVERLAY_COLOR_X, COLORS_Y),
			Pair.of(DARK_TEXT_COLOR_X, COLORS_Y),
			Pair.of(LIGHT_TEXT_COLOR_X, COLORS_Y)
			);	
	
	private final int shiftColor;
	private final int darkTextColor;
	private final int lightTextColor;
	
	private Button addShiftButton;
	private Button removeShiftButton;
	
	private int hoveredMinute = 0;
	private int hoveredShift = -1;
	private boolean scheduleHovered = false;
	private ScheduleDragging dragging = ScheduleDragging.NONE;
	private List<ITextProperties> info = new ArrayList<>();
	
	public EditScheduleScreen(EditScheduleContainer container, PlayerInventory playerInv, ITextComponent title) {
		super(container, playerInv, title);
		
		this.imageWidth = 153;
		this.imageHeight = 128;
		this.titleLabelY = 4;		
		
		List<Integer> colors = TextureUtils.getColors(SCHEDULE_GUI, COLOR_COORDS);
		
		this.shiftColor = colors.get(0);
		this.darkTextColor = colors.get(1);
		this.lightTextColor = colors.get(2);
	}
	
	@Override
	protected void init() {
		super.init();
		
		Button.ITooltip addShiftButton$tooltip = (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, TOOLTIP_ADD_SHIFT, mouseX, mouseY);
		
		this.addShiftButton = this.addButton(new ImageButton(
				this.leftPos + SCHEDULE_START_X - 2,
				this.topPos + SCHEDULE_Y + ADD_MARKER_TIP_HEIGHT,
				ADD_MARKER_WIDTH,
				ADD_SHIFT_BUTTON_HEIGHT,
				ADD_MARKER_TEX_X,
				ADD_SHIFT_BUTTON_TEX_Y,
				ADD_SHIFT_BUTTON_HEIGHT,
				SCHEDULE_GUI,
				TEXTURE_SIZE,
				TEXTURE_SIZE,
				this::addShift,
				addShiftButton$tooltip,
				TOOLTIP_ADD_SHIFT
				));
		WidgetUtils.setActiveAndVisible(this.addShiftButton, false);
		
		Button.ITooltip removeShiftButton$tooltip = (button, stack, mouseX, mouseY) -> this.renderTooltip(stack, TOOLTIP_REMOVE_SHIFT, mouseX, mouseY);;
		
		this.removeShiftButton = this.addButton(new ImageButton(
				this.leftPos + SCHEDULE_START_X,
				this.topPos + SCHEDULE_Y + SHIFT_START_MARKER_HEIGHT,
				REMOVE_SHIFT_BUTTON_WIDTH,
				REMOVE_SHIFT_BUTTON_HEIGHT,
				REMOVE_SHIFT_BUTTON_TEX_X,
				REMOVE_SHIFT_BUTTON_TEX_Y,
				REMOVE_SHIFT_BUTTON_HEIGHT,
				SCHEDULE_GUI,
				TEXTURE_SIZE,
				TEXTURE_SIZE,
				this::removeShift,
				removeShiftButton$tooltip,
				TOOLTIP_REMOVE_SHIFT
				));
		WidgetUtils.setActiveAndVisible(this.removeShiftButton, false);
		
		List<Pair<Integer, Integer>> schedule = this.menu.getSchedule();
		int minutes = schedule.stream()
				.map(shift -> shift.getSecond() - shift.getFirst())
				.reduce(Integer::sum)
				.orElse(0);
		
		this.info.clear();
		this.info.add(ALLOTTED_MINUTES_FIELD.copy().append(": " + minutes + " / " + this.menu.getMaxMinutes()));
		this.info.add(ALLOTTED_SHIFTS_FIELD.copy().append(": " + schedule.size() + " / " + this.menu.getMaxShifts()));
		this.info.add(new StringTextComponent(""));
		List<ITextProperties> splitPrompt = this.font.getSplitter().splitLines(PROMPT.copy(), INFO_WIDTH, Style.EMPTY);
		this.info.addAll(splitPrompt);
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
		texManager.bind(SCHEDULE_GUI);
		
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		int timeMarkerX = this.leftPos + SCHEDULE_START_X - 2 + MathHelper.floor(this.menu.getMinuteOfTheWeekScaled() * (float) SCHEDULE_LENGTH) % SCHEDULE_LENGTH;
		this.blit(stack, timeMarkerX, this.topPos + TOP_MARKERS_GUI_Y, TIME_MARKER_TEX_X, TIME_MARKER_TEX_Y, TIME_MARKER_WIDTH, TIME_MARKER_HEIGHT);
		
		if (this.canDisplayAdd()) {
			int addMarkerX = this.leftPos + SCHEDULE_START_X - 2 + MathHelper.floor((float) this.hoveredMinute / TimeUtils.WEEK_MINUTES * (float) SCHEDULE_LENGTH);
			this.blit(stack, addMarkerX, this.topPos + BOTTOM_MARKERS_GUI_Y, ADD_MARKER_TEX_X, ADD_MARKER_TEX_Y, ADD_MARKER_WIDTH, ADD_MARKER_TIP_HEIGHT);
		}
		
		this.menu.getSchedule().forEach(shift -> {
			int start = shift.getFirst();
			int end = shift.getSecond();
			
			int startScaled = this.leftPos + SCHEDULE_START_X + MathHelper.floor((float) start / TimeUtils.WEEK_MINUTES * (float) SCHEDULE_LENGTH);
			int endScaled = this.leftPos + SCHEDULE_START_X + MathHelper.floor((float) end / TimeUtils.WEEK_MINUTES * (float) SCHEDULE_LENGTH);
			
			this.fillGradient(stack, startScaled, this.topPos + SCHEDULE_Y, endScaled, this.topPos + SCHEDULE_Y + SCHEDULE_HEIGHT, this.shiftColor, this.shiftColor);
			
			this.blit(stack, startScaled, this.topPos + BOTTOM_MARKERS_GUI_Y, SHIFT_START_MARKER_TEX_X, SHIFT_START_MARKER_TEX_Y, SHIFT_START_MARKER_WIDTH, SHIFT_START_MARKER_HEIGHT);
			this.blit(stack, endScaled - SHIFT_END_MARKER_WIDTH + 1, this.topPos + TOP_MARKERS_GUI_Y, SHIFT_END_MARKER_TEX_X, SHIFT_END_MARKER_TEX_Y, SHIFT_END_MARKER_WIDTH, SHIFT_END_MARKER_HEIGHT);
		});
	}
	
	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
		this.font.draw(stack, this.title, (this.imageWidth - this.font.width(this.title)) / 2, this.titleLabelY, this.darkTextColor);
		for (int i = 0; i < DAY_NUMBERS.length; i++) {
			int x = DAY_NUM_START_X + i * DAY_NUM_SPACING;
			this.font.draw(stack, DAY_NUMBERS[i], x, DAY_NUM_Y, this.lightTextColor);
		}
		this.font.draw(stack, INFO_TITLE, (this.imageWidth - this.font.width(INFO_TITLE)) / 2, INFO_TITLE_Y, this.darkTextColor);
		
		for (int i = 0; i < this.info.size(); i++) {
			int y = INFO_START_Y + i * TEXT_SPACING;
			this.font.draw(stack, LanguageMap.getInstance().getVisualOrder(this.info.get(i)), INFO_X, y, this.darkTextColor);
		}
		//this.font.draw(stack, new StringTextComponent(Integer.toString(this.hoveredShift) + " " + this.dragging.toString()), 6, this.imageHeight - 10, this.darkTextColor);
	}
	
	@Override
	protected void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
		super.renderTooltip(stack, mouseX, mouseY);
		
		double d0 = mouseX - (double) this.leftPos;
		double d1 = mouseY - (double) this.topPos;
		if (this.hoveringTimeMarker(d0, d1)) {
			ITextComponent timeTooltip = formatTime(TIME_FIELD.copy().withStyle(TooltipUtils.FIELD_STYLE).append(": "), this.menu.getMinuteOfTheWeek(), TooltipUtils.VALUE_STYLE);
			this.renderTooltip(stack, timeTooltip, mouseX, mouseY);
		}
		
		if (this.addShiftButton.isHovered() && this.addShiftButton.active) this.addShiftButton.renderToolTip(stack, mouseX, mouseY);
		if (this.removeShiftButton.isHovered() && this.removeShiftButton.active) this.removeShiftButton.renderToolTip(stack, mouseX, mouseY);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			if (this.scheduleHovered) {
				List<Pair<Integer, Integer>> schedule = this.menu.getSchedule();
				for (int i = 0; i < schedule.size(); i++) {
					Pair<Integer, Integer> shift = schedule.get(i);
					if (shift.getFirst() == this.hoveredMinute) this.dragging = ScheduleDragging.START;
					else if (shift.getSecond() == this.hoveredMinute) this.dragging = ScheduleDragging.END;
					if (this.dragging != ScheduleDragging.NONE) {
						this.hoveredShift = i;
						break;
					}
				}
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			if (this.dragging != ScheduleDragging.NONE) {
				// Optimize
				List<Pair<Integer, Integer>> schedule = this.menu.getSchedule();
				if (0 <= this.hoveredShift && this.hoveredShift < schedule.size()) {
					
					Pair<Integer, Integer> thisShift = schedule.get(this.hoveredShift);
					
					if (this.dragging == ScheduleDragging.START && this.hoveredShift - 1 >= 0) {
						Pair<Integer, Integer> prevShift = schedule.get(this.hoveredShift - 1);
						if (prevShift.getSecond().intValue() == thisShift.getFirst().intValue()) {
							schedule.set(this.hoveredShift - 1, Pair.of(prevShift.getFirst(), thisShift.getSecond()));
							schedule.remove(this.hoveredShift);
						}
					} else if (this.dragging == ScheduleDragging.END && this.hoveredShift + 1 < schedule.size()) {
						Pair<Integer, Integer> nextShift = schedule.get(this.hoveredShift + 1);
						if (thisShift.getSecond().intValue() == nextShift.getFirst().intValue()) {
							schedule.set(this.hoveredShift, Pair.of(thisShift.getFirst(), nextShift.getSecond()));
							schedule.remove(this.hoveredShift + 1);
						}
					}
				}
				this.hoveredShift = -1;
			}
			this.dragging = ScheduleDragging.NONE;	
		}
		
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX1, double mouseY1, int button, double mouseX2, double mouseY2) {
		if (this.dragging != ScheduleDragging.NONE && this.menu.getSchedule().size() > 0) {
			List<Pair<Integer, Integer>> schedule = this.menu.getSchedule();
			Pair<Integer, Integer> thisShift = schedule.get(this.hoveredShift);
			
			int start = this.dragging == ScheduleDragging.START ? this.hoveredMinute : thisShift.getFirst();
			int end = this.dragging == ScheduleDragging.END ? this.hoveredMinute : thisShift.getSecond();
			
			if (this.dragging == ScheduleDragging.START && 0 <= this.hoveredShift - 1) {
				int prevEnd = schedule.get(this.hoveredShift - 1).getSecond();
				if (start <= prevEnd)
					start = prevEnd;
			} else if (this.dragging == ScheduleDragging.END && this.hoveredShift + 1 < schedule.size()) {
				int nextStart = schedule.get(this.hoveredShift + 1).getFirst();
				if (end >= nextStart)
					end = nextStart;
			}
				
			if (start < end) {
				// Validate max minutes
				Pair<Integer, Integer> oldPair = schedule.get(this.hoveredShift);
				schedule.set(this.hoveredShift, Pair.of(start, end));
				int minutes = schedule.stream()
						.map(shift -> shift.getSecond() - shift.getFirst())
						.reduce(0, Integer::sum);
				if (minutes > this.menu.getMaxMinutes()) {
					schedule.set(this.hoveredShift, oldPair);
				}
			}
		}
		
		return super.mouseDragged(mouseX1, mouseY1, button, mouseX2, mouseY2);
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		double d0 = mouseX - (double) this.leftPos;
		double d1 = mouseY - (double) this.topPos;
		
		int oldShift = this.hoveredShift;
		int oldMinute = this.hoveredMinute;
		boolean oldHovered = this.scheduleHovered;
		
		this.scheduleHovered = this.insideSchedule(d0, d1);
		if (this.scheduleHovered) {
			float posUnclamped = (float)(d0 - (double) SCHEDULE_START_X) / TimeUtils.WEEK_MINUTES;
			this.hoveredMinute = MathHelper.floor(MathHelper.clamp(posUnclamped, 0.0f, 1.0f) * TimeUtils.WEEK_MINUTES);
		}
		
		if (this.dragging == ScheduleDragging.NONE) this.hoveredShift = -1;
		
		List<Pair<Integer, Integer>> schedule = this.menu.getSchedule();
		if (this.scheduleHovered && this.dragging == ScheduleDragging.NONE) {
			int xOffs = this.leftPos + SCHEDULE_START_X;
			for (int i = 0; i < schedule.size(); i++) {
				Pair<Integer, Integer> shift = schedule.get(i);
				int start = shift.getFirst();
				int end = shift.getSecond();
				if (start <= this.hoveredMinute && this.hoveredMinute < end) {
					this.hoveredShift = i;
					xOffs += MathHelper.floor((float) start / TimeUtils.WEEK_MINUTES * (float) SCHEDULE_LENGTH);
				}
			}
			
			this.addShiftButton.x = this.hoveredShift == -1 ? MathHelper.floor(mouseX) : this.leftPos + SCHEDULE_START_X;
			this.addShiftButton.x -= 2;
			this.removeShiftButton.x = xOffs;
		}
		
		if (this.hoveredShift == -1 && this.hoveredMinute != oldMinute) {
			this.info.clear();
			this.info.add(formatTime(TIME_FIELD.copy().append(": "), this.hoveredMinute));
		} else if (this.hoveredShift > -1 && this.hoveredShift != oldShift) {
			this.info.clear();
			Pair<Integer, Integer> shift = schedule.get(this.hoveredShift);
			this.info.add(formatTime(START_TIME_field.copy().append(": "), shift.getFirst()));
			this.info.add(formatTime(END_TIME_FIELD.copy().append(": "), shift.getSecond()));
			this.info.add(SHIFT_LENGTH_FIELD.copy().append(": ").append(new TranslationTextComponent(TIME_FORMAT_2_KEY, shift.getSecond() - shift.getFirst())));
		}
		if (oldHovered && !this.scheduleHovered) {
			int minutes = schedule.stream()
					.map(shift -> shift.getSecond() - shift.getFirst())
					.reduce(Integer::sum)
					.orElse(0);
			
			this.info.clear();
			this.info.add(ALLOTTED_MINUTES_FIELD.copy().append(": " + minutes + " / " + this.menu.getMaxMinutes()));
			this.info.add(ALLOTTED_SHIFTS_FIELD.copy().append(": " + schedule.size() + " / " + this.menu.getMaxShifts()));
			this.info.add(new StringTextComponent(""));
			List<ITextProperties> splitPrompt = this.font.getSplitter().splitLines(PROMPT.copy(), INFO_WIDTH, Style.EMPTY);
			this.info.addAll(splitPrompt);
		}
		
		WidgetUtils.setActiveAndVisible(this.addShiftButton, this.canDisplayAdd());
		WidgetUtils.setActiveAndVisible(this.removeShiftButton, this.hoveredShift > -1 && this.dragging == ScheduleDragging.NONE);
	}
	
	@Override
	public void onClose() {
		IWNetwork.CHANNEL.sendToServer(new SEditScheduleSyncMessage(this.menu.getHand(), this.menu.getSchedule()));
		super.onClose();
	}
	
	private void addShift(Button button) {
		if (this.menu.canAddShift()) {
			int afterHover = this.hoveredMinute + 1;
			Pair<Integer, Integer> newShift = Pair.of(this.hoveredMinute, afterHover);
			List<Pair<Integer, Integer>> schedule = this.menu.getSchedule();
			
			if (!ScheduleUtils.inShift(schedule, this.hoveredMinute)) {
				ListIterator<Pair<Integer, Integer>> iter = schedule.listIterator();
				boolean inserted = false;
				while (iter.hasNext()) {
					if (iter.hasPrevious()) {
						Pair<Integer, Integer> prevShift = iter.previous();
						iter.next();
						Pair<Integer, Integer> thisShift = iter.next();
						
						int prevStart = prevShift.getFirst();
						int prevEnd = prevShift.getSecond();
						int thisStart = thisShift.getFirst();
						int thisEnd = thisShift.getSecond();
						
						if (prevEnd <= this.hoveredMinute && this.hoveredMinute < thisStart) {
							// Shift optimization
							boolean afterPrevShift = prevEnd == this.hoveredMinute;
							boolean beforeThisShift = afterHover == thisStart;
							boolean optimizable = afterPrevShift || beforeThisShift;
							
							if (optimizable) {
								int start = afterPrevShift ? prevStart : this.hoveredMinute;
								int end = beforeThisShift ? thisEnd : afterHover;
								if (afterPrevShift) {
									iter.previous();
									if (beforeThisShift) iter.remove();
									iter.previous();
								}
								iter.set(Pair.of(start, end));
							} else { // Add new shift
								iter.previous();
								iter.add(newShift);
							}
							inserted = true;
							break;
						}
					} else {
						Pair<Integer, Integer> thisShift = iter.next();
						if (this.hoveredMinute < thisShift.getFirst()) {
							if (afterHover == thisShift.getFirst()) {
								iter.set(Pair.of(this.hoveredMinute, thisShift.getSecond()));
							} else {
								iter.previous();
								iter.add(newShift);
							}
							inserted = true;
							break;
						}
					}
				}
				if (!inserted) { // End of schedule
					if (schedule.size() > 0) {
						Pair<Integer, Integer> lastShift = schedule.get(schedule.size() - 1);
						boolean afterLast = lastShift.getSecond() == this.hoveredMinute;
						int start = afterLast ? lastShift.getFirst() : this.hoveredMinute;
						if (afterLast) schedule.remove(schedule.size() - 1);
						schedule.add(Pair.of(start, afterHover));
					} else {
						schedule.add(newShift);
					}
				}
			}
			WidgetUtils.setActiveAndVisible(button, false);
		}
	}
	
	private void removeShift(Button button) {
		List<Pair<Integer, Integer>> schedule = this.menu.getSchedule();
		if (0 <= this.hoveredShift && this.hoveredShift < schedule.size()) schedule.remove(this.hoveredShift);
		WidgetUtils.setActiveAndVisible(button, false);
	}
	
	private boolean canDisplayAdd() {
		return this.scheduleHovered && this.hoveredShift == -1 && this.hoveredMinute < 140 && this.menu.canAddShift();
	}
	
	private boolean insideSchedule(double mouseX, double mouseY) {
		return SCHEDULE_START_X <= mouseX && mouseX < SCHEDULE_START_X + SCHEDULE_LENGTH + 1 && SCHEDULE_Y <= mouseY && mouseY <= SCHEDULE_Y + ADD_MARKER_HEIGHT;
	}
	
	private boolean hoveringTimeMarker(double mouseX, double mouseY) {
		int markerX = SCHEDULE_START_X - 2 + MathHelper.floor(this.menu.getMinuteOfTheWeekScaled() * (float) SCHEDULE_LENGTH) % SCHEDULE_LENGTH;
		return markerX <= mouseX && mouseX < markerX + TIME_MARKER_WIDTH && TOP_MARKERS_GUI_Y <= mouseY && mouseY <= TOP_MARKERS_GUI_Y + TIME_MARKER_HEAD_HEIGHT;
	}
	
	private static IFormattableTextComponent formatTime(IFormattableTextComponent tc, long minuteOfTheWeek) {
		return formatTime(tc, minuteOfTheWeek, Style.EMPTY);
	}
	
	private static IFormattableTextComponent formatTime(IFormattableTextComponent tc, long minuteOfTheWeek, Style style) {
		long day = minuteOfTheWeek / TimeUtils.DAY_MINUTES + 1L;
		long minute = minuteOfTheWeek % TimeUtils.DAY_MINUTES;
		return tc.copy().append(new TranslationTextComponent(TIME_FORMAT_1_KEY, day, minute).withStyle(style));
	}
	
}
