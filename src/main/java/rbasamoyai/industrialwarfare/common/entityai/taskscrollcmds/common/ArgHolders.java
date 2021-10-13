package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.function.Supplier;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import rbasamoyai.industrialwarfare.IndustrialWarfare;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class ArgHolders {

	private static final ITextComponent FILTER_TITLE = new TranslationTextComponent("command." + IndustrialWarfare.MOD_ID + ".args.filter");
	
	public static final Supplier<IArgHolder> BASE_JUMP_CONDITION_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.BASE_JUMP_CONDITION_SELECTOR);
	public static final Supplier<IArgHolder> DAY_TIME_CONDITION_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.DAY_TIME_CONDITION_SELECTOR);
	public static final Supplier<IArgHolder> FILTER_ARG_HOLDER = () -> new ItemArgHolder(FILTER_TITLE);
	public static final Supplier<IArgHolder> ITEM_CONDITION_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.ITEM_CONDITION_SELECTOR);
	public static final Supplier<IArgHolder> ITEM_COUNT_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.ITEM_COUNT_SELECTOR);
	public static final Supplier<IArgHolder> JUMP_INDEX_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.JUMP_INDEX_SELECTOR);
	public static final Supplier<IArgHolder> LOOK_FOR_NAME_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.LOOK_FOR_NAME_SELECTOR);
	public static final Supplier<IArgHolder> LOOK_FOR_NUM_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.LOOK_FOR_NUM_SELECTOR);
	public static final Supplier<IArgHolder> POS_MODE_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.POS_MODE_SELECTOR);
	public static final Supplier<IArgHolder> TIME_COUNT_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.TIME_COUNT_SELECTOR);
	public static final Supplier<IArgHolder> WAIT_MODE_ARG_HOLDER = () -> new CountArgHolder(ArgSelectors.WAIT_MODE_SELECTOR);
	
}
