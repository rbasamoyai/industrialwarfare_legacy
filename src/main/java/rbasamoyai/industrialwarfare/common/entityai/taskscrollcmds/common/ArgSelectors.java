package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.JumpToCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.SwitchOrderCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WordedArgSelector.ArgGroup;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;

public class ArgSelectors {

	private static final String TASK_SCROLL_TOOLTIP_ROOT_KEY =  "selector.tooltip." + IndustrialWarfare.MOD_ID;
	private static final String TASK_SCROLL_TITLE_ROOT_KEY = "selector.title." + IndustrialWarfare.MOD_ID;
	
	private static final List<ITextComponent> ITEM_COUNT_TOOLTIP = Arrays.asList(new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".item_count").withStyle(ArgSelector.HEADER_STYLE));
	public static final IArgSelectorProvider ITEM_COUNT_SELECTOR = (i, menu) -> new CountArgSelector(i, 0, 1024, ITEM_COUNT_TOOLTIP);
	
	private static final List<ITextComponent> PURSUIT_DISTANCE_TOOLTIP = Arrays.asList(new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".pursuit"));
	public static final IArgSelectorProvider PURSUIT_DISTANCE_SELECTOR = (i, menu) -> new CountArgSelector(i, 1, 128, PURSUIT_DISTANCE_TOOLTIP);
	
	private static final List<ITextComponent> TIME_COUNT_TOOLTIP = Arrays.asList(new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".time_count").withStyle(ArgSelector.HEADER_STYLE));
	public static final IArgSelectorProvider TIME_COUNT_SELECTOR = (i, menu) -> new CountArgSelector(i, 0, 60, TIME_COUNT_TOOLTIP) {
		@Override
		public ITextComponent getTitle() {
			return new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".time_count.time_format", this.selectedArg);
		}
	};
	
	private static final List<ITextComponent> JUMP_INDEX_TOOLTIP = Arrays.asList(new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".jump_index").withStyle(ArgSelector.HEADER_STYLE));
	public static final IArgSelectorProvider JUMP_INDEX_SELECTOR = (i, menu) -> new CountArgSelector(i, 0, menu.getMaxOrders(), JUMP_INDEX_TOOLTIP) {
		@Override
		public ITextComponent getTitle() {
			return new StringTextComponent(Integer.toString(this.selectedArg + 1));
		}
	};
	
	private static final List<ITextComponent> LOOK_FOR_NUM_TOOLTIP = Arrays.asList(new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".look_for_num").withStyle(ArgSelector.HEADER_STYLE));
	private static final ITextComponent LOOK_FOR_NUM_TITLE_ANY = new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".look_for_num.any");
	private static final ITextComponent LOOK_FOR_NUM_TITLE_NONE = new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".look_for_num.none");
	public static final IArgSelectorProvider LOOK_FOR_NUM_SELECTOR = (i, menu) -> new CountArgSelector(i, -1, 127, LOOK_FOR_NUM_TOOLTIP) {
		@Override
		public ITextComponent getTitle() {
			switch (this.selectedArg) {
			case -1: return LOOK_FOR_NUM_TITLE_ANY;
			case 0: return LOOK_FOR_NUM_TITLE_NONE;
			default: return new StringTextComponent(Integer.toString(this.selectedArg));
			}
		}
	};
	
	private static final List<ArgGroup> BASE_JUMP_CONDITION_GROUPS =
			Arrays.asList(
					new ArgGroup(JumpToCommand.BaseCondition.UNCONDITIONAL, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".base_jump_condition.unconditional"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".base_jump_condition.unconditional")),
					new ArgGroup(JumpToCommand.BaseCondition.DAY_TIME, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".base_jump_condition.day_time"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".base_jump_condition.day_time")),
					new ArgGroup(JumpToCommand.BaseCondition.HAS_ITEMS, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".base_jump_condition.has_items"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".base_jump_condition.has_items")),
					new ArgGroup(JumpToCommand.BaseCondition.HEARD_BELL, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".base_jump_condition.heard_bell"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".base_jump_condition.heard_bell")));
	private static final ITextComponent BASE_JUMP_CONDITION_HEADER = new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".base_jump_condition").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider BASE_JUMP_CONDITION_SELECTOR = (i, menu) -> new WordedArgSelector(BASE_JUMP_CONDITION_GROUPS, i, BASE_JUMP_CONDITION_HEADER);
	
	private static final List<ArgGroup> DAY_TIME_CONDITION_GROUPS =
			Arrays.asList(
					new ArgGroup(JumpToCommand.DayTimeCondition.BEFORE, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".day_time_condition.before"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".day_time_condition.before")),
					new ArgGroup(JumpToCommand.DayTimeCondition.AFTER, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".day_time_condition.after"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".day_time_condition.after")));
	private static final ITextComponent DAY_TIME_CONDITION_HEADER = new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".day_time_condition").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider DAY_TIME_CONDITION_SELECTOR = (i, menu) -> new WordedArgSelector(DAY_TIME_CONDITION_GROUPS, i, DAY_TIME_CONDITION_HEADER);
	
	private static final List<ArgGroup> ITEM_CONDITION_GROUPS = 
			Arrays.asList(
					new ArgGroup(JumpToCommand.ItemCondition.UNCONDITIONAL, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".item_condition.unconditional"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".item_condition.unconditional")),
					new ArgGroup(JumpToCommand.ItemCondition.MORE_THAN, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".item_condition.more_than"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".item_condition.more_than")),
					new ArgGroup(JumpToCommand.ItemCondition.LESS_THAN, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".item_condition.less_than"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".item_condition.less_than")),
					new ArgGroup(JumpToCommand.ItemCondition.EQUAL_TO, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".item_condition.equal_to"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".item_condition.equal_to")));
	private static final ITextComponent ITEM_CONDITION_HEADER = new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".item_condition").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider ITEM_CONDITION_SELECTOR = (i, menu) -> new WordedArgSelector(ITEM_CONDITION_GROUPS, i, ITEM_CONDITION_HEADER);
	
	private static final List<ArgGroup> WAIT_MODE_GROUPS =
			Arrays.asList(
					new ArgGroup(WaitMode.DAY_TIME.getId(), new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".wait_mode.day_time"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".wait_mode.day_time")),
					new ArgGroup(WaitMode.RELATIVE_TIME.getId(), new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".wait_mode.relative_time"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".wait_mode.relative_time")),
					new ArgGroup(WaitMode.HEARD_BELL.getId(), new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".wait_mode.bell"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".wait_mode.bell")));	
	private static IArgSelectorProvider waitModeSelectorWithHeader(ITextComponent header) {
		return (i, menu) -> new WordedArgSelector(WAIT_MODE_GROUPS, i, header);
	}
	
	private static final ITextComponent WAIT_MODE_HEADER = new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".wait_mode").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider WAIT_MODE_SELECTOR = waitModeSelectorWithHeader(WAIT_MODE_HEADER);
	
	private static final ITextComponent WORK_MODE_HEADER = new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".work_mode").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider WORK_MODE_SELECTOR = waitModeSelectorWithHeader(WORK_MODE_HEADER);
	
	private static final ITextComponent PATROL_MODE_HEADER = new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".patrol_mode").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider PATROL_MODE_SELECTOR = waitModeSelectorWithHeader(PATROL_MODE_HEADER);
	
	private static final List<ArgGroup> LOOK_FOR_NAME_GROUPS =
			Arrays.asList(
					new ArgGroup(SwitchOrderCommand.LookNameModes.DONT_LOOK_FOR_NAME, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".look_for_name.dont_look"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".look_for_name.dont_look")),
					new ArgGroup(SwitchOrderCommand.LookNameModes.LOOK_FOR_NAME, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".look_for_name.look"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".look_for_name.look")));
	private static final ITextComponent LOOK_FOR_NAME_HEADER = new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".look_for_name").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider LOOK_FOR_NAME_SELECTOR = (i, menu) -> new WordedArgSelector(LOOK_FOR_NAME_GROUPS, i, LOOK_FOR_NAME_HEADER);
	
	private static final List<ArgGroup> POS_MODE_GROUPS =
			Arrays.asList(
					new ArgGroup(SwitchOrderCommand.PosModes.GET_FROM_JOB_SITE, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".pos_mode.job_site"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".pos_mode.job_site")),
					new ArgGroup(SwitchOrderCommand.PosModes.GET_FROM_POS, new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".pos_mode.pos"), new TranslationTextComponent(TASK_SCROLL_TITLE_ROOT_KEY + ".pos_mode.pos")));
	private static final ITextComponent POS_MODE_HEADER = new TranslationTextComponent(TASK_SCROLL_TOOLTIP_ROOT_KEY + ".pos_mode").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider POS_MODE_SELECTOR = (i, menu) -> new WordedArgSelector(POS_MODE_GROUPS, i, POS_MODE_HEADER);
	
}
