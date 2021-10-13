package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class DayTimeArgHolder implements IArgHolder {

	private int dayTime;
	
	@Override
	public void accept(ArgWrapper wrapper) {
		this.dayTime = wrapper.getArgNum();
	}

	@Override
	public ArgWrapper getWrapper() {
		return new ArgWrapper(this.dayTime);
	}

	@Override
	public boolean isItemStackArg() {
		return false;
	}

	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(new DayTimeArgSelector(this.dayTime));
	}

}
