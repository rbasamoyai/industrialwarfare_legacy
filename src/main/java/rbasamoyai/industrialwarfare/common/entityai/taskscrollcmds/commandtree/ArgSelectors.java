package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.JumpToCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.SwitchOrderCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.CountArgSelector;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.IArgSelectorProvider;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WaitMode;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WordedArgSelector;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WordedArgSelector.ArgGroup;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;

public class ArgSelectors {

	private static final String SELECTOR_TOOLTIP_ROOT_KEY =  "selector.tooltip." + IndustrialWarfare.MOD_ID;
	private static final String SELECTOR_TITLE_ROOT_KEY = "selector.title." + IndustrialWarfare.MOD_ID;
	
	private static final List<ITextComponent> DAY_TIME_TOOLTIP = Arrays.asList(
			new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".day_time"),
			new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".day_time.shift_modifier").withStyle(TextFormatting.DARK_GRAY));
	public static final IArgSelectorProvider DAY_TIME_SELECTOR = (i, menu) -> new CountArgSelector(i, 0, 1200, 60, DAY_TIME_TOOLTIP) {
		@Override
		public ITextComponent getTitle() {
			String minute = String.format("%02d", this.selectedArg / 60);
			String second = String.format("%02d", this.selectedArg % 60);
			return new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".day_time.time_format", minute, second);
		}
	};
	
	private static final List<ITextComponent> ITEM_COUNT_TOOLTIP = Arrays.asList(new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".item_count").withStyle(ArgSelector.HEADER_STYLE));
	public static final IArgSelectorProvider ITEM_COUNT_SELECTOR = (i, menu) -> new CountArgSelector(i, 0, 1024, ITEM_COUNT_TOOLTIP);
	
	private static final List<ITextComponent> PURSUIT_DISTANCE_TOOLTIP = Arrays.asList(new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".pursuit"));
	public static final IArgSelectorProvider PURSUIT_DISTANCE_SELECTOR = (i, menu) -> new CountArgSelector(i, 1, 128, PURSUIT_DISTANCE_TOOLTIP);
	
	private static final List<ITextComponent> TIME_COUNT_TOOLTIP = Arrays.asList(new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".time_count").withStyle(ArgSelector.HEADER_STYLE));
	public static final IArgSelectorProvider TIME_COUNT_SELECTOR = (i, menu) -> new CountArgSelector(i, 0, 60, TIME_COUNT_TOOLTIP) {
		@Override
		public ITextComponent getTitle() {
			return new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".time_count.time_format", this.selectedArg);
		}
	};
	
	private static final List<ITextComponent> JUMP_INDEX_TOOLTIP = Arrays.asList(new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".jump_index").withStyle(ArgSelector.HEADER_STYLE));
	public static final IArgSelectorProvider JUMP_INDEX_SELECTOR = (i, menu) -> new CountArgSelector(i, 0, menu.getMaxOrders(), JUMP_INDEX_TOOLTIP) {
		@Override
		public ITextComponent getTitle() {
			return new StringTextComponent(Integer.toString(this.selectedArg + 1));
		}
	};
	
	private static final List<ITextComponent> LOOK_FOR_NUM_TOOLTIP = Arrays.asList(new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".look_for_num").withStyle(ArgSelector.HEADER_STYLE));
	private static final ITextComponent LOOK_FOR_NUM_TITLE_ANY = new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".look_for_num.any");
	private static final ITextComponent LOOK_FOR_NUM_TITLE_NONE = new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".look_for_num.none");
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
					new ArgGroup(JumpToCommand.BaseCondition.UNCONDITIONAL, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".base_jump_condition.unconditional"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".base_jump_condition.unconditional")),
					new ArgGroup(JumpToCommand.BaseCondition.DAY_TIME, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".base_jump_condition.day_time"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".base_jump_condition.day_time")),
					new ArgGroup(JumpToCommand.BaseCondition.HAS_ITEMS, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".base_jump_condition.has_items"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".base_jump_condition.has_items")),
					new ArgGroup(JumpToCommand.BaseCondition.HEARD_BELL, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".base_jump_condition.heard_bell"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".base_jump_condition.heard_bell")));
	private static final ITextComponent BASE_JUMP_CONDITION_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".base_jump_condition").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider BASE_JUMP_CONDITION_SELECTOR = (i, menu) -> new WordedArgSelector(BASE_JUMP_CONDITION_GROUPS, i, BASE_JUMP_CONDITION_HEADER);
	
	private static final List<ArgGroup> DAY_TIME_CONDITION_GROUPS =
			Arrays.asList(
					new ArgGroup(JumpToCommand.DayTimeCondition.BEFORE, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".day_time_condition.before"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".day_time_condition.before")),
					new ArgGroup(JumpToCommand.DayTimeCondition.AFTER, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".day_time_condition.after"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".day_time_condition.after")));
	private static final ITextComponent DAY_TIME_CONDITION_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".day_time_condition").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider DAY_TIME_CONDITION_SELECTOR = (i, menu) -> new WordedArgSelector(DAY_TIME_CONDITION_GROUPS, i, DAY_TIME_CONDITION_HEADER);
	
	private static final List<ArgGroup> EQUIP_SLOT_GROUPS =
			Arrays.stream(EquipmentSlotType.values())
			.map(e -> {
				String s = ".equip_slot." + e.getName();
				return new ArgGroup(e.getFilterFlag(), new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + s), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + s));
			})
			.collect(Collectors.toList());
	private static final ITextComponent EQUIP_SLOT_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".equip_slot").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider EQUIP_SLOT_SELECTOR = (i, menu) -> new WordedArgSelector(EQUIP_SLOT_GROUPS, i, EQUIP_SLOT_HEADER);
	
	private static final List<ArgGroup> UNEQUIP_SLOT_GROUPS =
			Arrays.stream(EquipmentSlotType.values())
			.map(e -> {
				String s = ".unequip_slot." + e.getName();
				return new ArgGroup(e.getFilterFlag(), new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + s), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + s));
			})
			.collect(Collectors.toList());
	private static final ITextComponent UNEQUIP_SLOT_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".unequip_slot").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider UNEQUIP_SLOT_SELECTOR = (i, menu) -> new WordedArgSelector(UNEQUIP_SLOT_GROUPS, i, UNEQUIP_SLOT_HEADER);

	private static final List<ArgGroup> ITEM_CONDITION_GROUPS = 
			Arrays.asList(
					new ArgGroup(JumpToCommand.ItemCondition.UNCONDITIONAL, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".item_condition.unconditional"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".item_condition.unconditional")),
					new ArgGroup(JumpToCommand.ItemCondition.MORE_THAN, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".item_condition.more_than"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".item_condition.more_than")),
					new ArgGroup(JumpToCommand.ItemCondition.LESS_THAN, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".item_condition.less_than"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".item_condition.less_than")),
					new ArgGroup(JumpToCommand.ItemCondition.EQUAL_TO, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".item_condition.equal_to"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".item_condition.equal_to")));
	private static final ITextComponent ITEM_CONDITION_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".item_condition").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider ITEM_CONDITION_SELECTOR = (i, menu) -> new WordedArgSelector(ITEM_CONDITION_GROUPS, i, ITEM_CONDITION_HEADER);
	
	private static final List<ArgGroup> WAIT_MODE_GROUPS =
			Arrays.asList(
					new ArgGroup(WaitMode.DAY_TIME.getId(), new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".wait_mode.day_time"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".wait_mode.day_time")),
					new ArgGroup(WaitMode.RELATIVE_TIME.getId(), new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".wait_mode.relative_time"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".wait_mode.relative_time")),
					new ArgGroup(WaitMode.HEARD_BELL.getId(), new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".wait_mode.bell"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".wait_mode.bell")));	
	private static IArgSelectorProvider waitModeSelectorWithHeader(ITextComponent header) {
		return (i, menu) -> new WordedArgSelector(WAIT_MODE_GROUPS, i, header);
	}
	
	private static final ITextComponent WAIT_MODE_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".wait_mode").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider WAIT_MODE_SELECTOR = waitModeSelectorWithHeader(WAIT_MODE_HEADER);
	
	private static final ITextComponent WORK_MODE_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".work_mode").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider WORK_MODE_SELECTOR = waitModeSelectorWithHeader(WORK_MODE_HEADER);
	
	private static final ITextComponent PATROL_MODE_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".patrol_mode").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider PATROL_MODE_SELECTOR = waitModeSelectorWithHeader(PATROL_MODE_HEADER);
	
	private static final List<ArgGroup> LOOK_FOR_NAME_GROUPS =
			Arrays.asList(
					new ArgGroup(SwitchOrderCommand.LookNameModes.DONT_LOOK_FOR_NAME, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".look_for_name.dont_look"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".look_for_name.dont_look")),
					new ArgGroup(SwitchOrderCommand.LookNameModes.LOOK_FOR_NAME, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".look_for_name.look"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".look_for_name.look")));
	private static final ITextComponent LOOK_FOR_NAME_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".look_for_name").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider LOOK_FOR_NAME_SELECTOR = (i, menu) -> new WordedArgSelector(LOOK_FOR_NAME_GROUPS, i, LOOK_FOR_NAME_HEADER);
	
	private static final List<ArgGroup> POS_MODE_GROUPS =
			Arrays.asList(
					new ArgGroup(SwitchOrderCommand.PosModes.GET_FROM_JOB_SITE, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".pos_mode.job_site"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".pos_mode.job_site")),
					new ArgGroup(SwitchOrderCommand.PosModes.GET_FROM_POS, new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".pos_mode.pos"), new TranslationTextComponent(SELECTOR_TITLE_ROOT_KEY + ".pos_mode.pos")));
	private static final ITextComponent POS_MODE_HEADER = new TranslationTextComponent(SELECTOR_TOOLTIP_ROOT_KEY + ".pos_mode").withStyle(ArgSelector.HEADER_STYLE);
	public static final IArgSelectorProvider POS_MODE_SELECTOR = (i, menu) -> new WordedArgSelector(POS_MODE_GROUPS, i, POS_MODE_HEADER);
	
}
