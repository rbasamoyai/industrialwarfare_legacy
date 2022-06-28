package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.core.Direction;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollMenu;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class StorageSideAccessArgHolder implements IArgHolder {

	private Direction arg;
	
	@Override
	public void accept(ArgWrapper wrapper) {
		this.arg = Direction.from3DDataValue(wrapper.getArgNum());
	}
	
	@Override
	public ArgWrapper getWrapper() {
		return new ArgWrapper(this.arg.get3DDataValue());
	}
	
	@Override
	public boolean isItemStackArg() {
		return false;
	}

	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollMenu container) {
		return Optional.of(new StorageSideAccessArgSelector(this.arg.get3DDataValue()));
	}
	
}
