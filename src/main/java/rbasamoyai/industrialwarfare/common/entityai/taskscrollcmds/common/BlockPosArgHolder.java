package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.util.math.BlockPos;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgSelector;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class BlockPosArgHolder implements IArgHolder {
	
	private BlockPos arg;
	
	@Override
	public void accept(ArgWrapper wrapper) {
		this.arg = wrapper.getPos().orElse(BlockPos.ZERO);
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
		return Optional.of(new BlockPosArgSelector(container.getPlayer(), this.arg));
	}
	
}
