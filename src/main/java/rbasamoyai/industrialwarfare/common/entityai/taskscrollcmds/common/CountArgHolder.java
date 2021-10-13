package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;
import java.util.function.BiFunction;

import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class CountArgHolder implements IArgHolder {

	private final BiFunction<Integer, TaskScrollContainer, ArgSelector<?>> selectorProvider;
	protected int arg;
	
	public CountArgHolder(BiFunction<Integer, TaskScrollContainer, ArgSelector<?>> selectorProvider) {
		this.selectorProvider = selectorProvider;
	}

	@Override
	public void accept(ArgWrapper wrapper) {
		this.arg = wrapper.getArgNum();
	}

	@Override
	public ArgWrapper getWrapper() {
		return new ArgWrapper(this.arg);
	}

	@Override
	public boolean isItemStackArg() {
		return false;
	}

	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(this.selectorProvider.apply(this.arg, container));
	}
	
}
