package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.function.BiFunction;

import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;

/**
 * Convienience shortener for BiFunction<Integer, TaskScrollContainer, ArgSelector<?>>
 * 
 * @author rbasamoyai
 */

@FunctionalInterface
public interface IArgSelectorProvider extends BiFunction<Integer, TaskScrollContainer, ArgSelector<?>> {
}
