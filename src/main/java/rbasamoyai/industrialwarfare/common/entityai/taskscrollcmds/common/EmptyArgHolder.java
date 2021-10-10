package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class EmptyArgHolder implements IArgHolder {

	@Override
	public void accept(ArgWrapper wrapper) {
	}

	@Override
	public ArgWrapper getWrapper() {
		return new ArgWrapper(0);
	}
	
	@Override
	public boolean isItemStackArg() {
		return false;
	}
	
	@Override
	public void toNetwork(PacketBuffer buf) {
	}
	
	@Override
	public void fromNetwork(PacketBuffer buf) {
	}

	@Override
	public CompoundNBT serializeNBT() {
		return new CompoundNBT();
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
	}

	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(new EmptyArgSelector());
	}

}
