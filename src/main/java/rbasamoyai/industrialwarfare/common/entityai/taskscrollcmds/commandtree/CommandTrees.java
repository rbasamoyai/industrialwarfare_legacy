package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree;

import java.util.function.Predicate;

import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.JumpToCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.SwitchOrderCommand;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.BlockPosArgHolder;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.DayTimeArgHolder;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.StorageSideAccessArgHolder;
import rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common.WaitMode;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;

public class CommandTrees {

	private static final Predicate<ArgWrapper> ALWAYS_TRUE = wrapper -> true;
	
	public static final CommandTree POS_ONLY =
			CommandTree.builder(BlockPosArgHolder::new)
			.build();
	
	public static final CommandTree ITEM_HANDLING =
			CommandTree.builder(BlockPosArgHolder::new)
			.beginNode(ArgHolders.FILTER_ARG_HOLDER, ALWAYS_TRUE)
					.beginNode(StorageSideAccessArgHolder::new, ALWAYS_TRUE)
							.addTerminalNode(ArgHolders.ITEM_COUNT_ARG_HOLDER, ALWAYS_TRUE)
					.endNode()
			.endNode()
			.build();
	
	public static final CommandTree WAIT_FOR = 
			CommandTree.builder(ArgHolders.WAIT_MODE_ARG_HOLDER)
			.addTerminalNode(DayTimeArgHolder::new, wrapper -> WaitMode.fromId(wrapper.getArgNum()) == WaitMode.DAY_TIME)
			.addTerminalNode(ArgHolders.TIME_COUNT_ARG_HOLDER, wrapper -> WaitMode.fromId(wrapper.getArgNum()) == WaitMode.RELATIVE_TIME)
			.build();
	
	public static final CommandTree JUMP_TO =
			CommandTree.builder(ArgHolders.JUMP_INDEX_ARG_HOLDER)
			.beginNode(ArgHolders.BASE_JUMP_CONDITION_ARG_HOLDER, ALWAYS_TRUE)
					.beginNode(ArgHolders.DAY_TIME_CONDITION_ARG_HOLDER, wrapper -> wrapper.getArgNum() == JumpToCommand.BaseCondition.DAY_TIME)
							.addTerminalNode(DayTimeArgHolder::new, ALWAYS_TRUE)
					.endNode()
					.beginNode(ArgHolders.FILTER_ARG_HOLDER, wrapper -> wrapper.getArgNum() == JumpToCommand.BaseCondition.HAS_ITEMS)
							.beginNode(ArgHolders.ITEM_CONDITION_ARG_HOLDER, ALWAYS_TRUE)
									.addTerminalNode(ArgHolders.ITEM_COUNT_ARG_HOLDER, wrapper -> wrapper.getArgNum() != JumpToCommand.ItemCondition.UNCONDITIONAL)
							.endNode()
					.endNode()
			.endNode()
			.build();
	
	public static final CommandTree SWITCH_ORDER =
			CommandTree.builder(ArgHolders.LOOK_FOR_NUM_ARG_HOLDER)
			.beginNode(ArgHolders.LOOK_FOR_NAME_ARG_HOLDER, ALWAYS_TRUE)
					.beginNode(StorageSideAccessArgHolder::new, ALWAYS_TRUE)
							.beginNode(ArgHolders.POS_MODE_ARG_HOLDER, ALWAYS_TRUE)
									.addTerminalNode(BlockPosArgHolder::new, wrapper -> wrapper.getArgNum() == SwitchOrderCommand.PosModes.GET_FROM_POS)
							.endNode()
					.endNode()
			.endNode()
			.build();
	
	public static final CommandTree WORK_AT =
			CommandTree.builder(BlockPosArgHolder::new)
			.beginNode(ArgHolders.WORK_MODE_ARG_HOLDER, ALWAYS_TRUE)
					.addTerminalNode(DayTimeArgHolder::new, wrapper -> WaitMode.fromId(wrapper.getArgNum()) == WaitMode.DAY_TIME)
					.addTerminalNode(ArgHolders.TIME_COUNT_ARG_HOLDER, wrapper -> WaitMode.fromId(wrapper.getArgNum()) == WaitMode.RELATIVE_TIME)
			.endNode()
			.build();
	
	public static final CommandTree PATROL =
			CommandTree.builder(BlockPosArgHolder::new)
			.beginNode(ArgHolders.PURSUIT_DISTANCE_HOLDER, ALWAYS_TRUE)
					.beginNode(ArgHolders.PATROL_MODE_ARG_HOLDER, ALWAYS_TRUE)
							.addTerminalNode(DayTimeArgHolder::new, wrapper -> WaitMode.fromId(wrapper.getArgNum()) == WaitMode.DAY_TIME)
							.addTerminalNode(ArgHolders.TIME_COUNT_ARG_HOLDER, wrapper -> WaitMode.fromId(wrapper.getArgNum()) == WaitMode.RELATIVE_TIME)
					.endNode()
			.endNode()
			.build();
	
}
