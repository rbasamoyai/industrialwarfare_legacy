package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.function.BiFunction;

import rbasamoyai.industrialwarfare.common.containers.TaskScrollMenu;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;

/**
 * Convienience shortener for BiFunction<Integer, TaskScrollContainer, ArgSelector<?>>
 * 
 * @author rbasamoyai
 */

@FunctionalInterface
public interface IArgSelectorProvider extends BiFunction<Integer, TaskScrollMenu, ArgSelector<?>> {
}
