package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.wait_for;

import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class WaitModeArgHolder implements IArgHolder {

	private static final String TAG_MODE = "mode";
	
	private byte arg;
	
	@Override
	public void accept(ArgWrapper wrapper) {
		this.arg = (byte) wrapper.getArgNum();
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
	public void toNetwork(PacketBuffer buf) {
		buf.writeByte(this.arg);
	}

	@Override
	public void fromNetwork(PacketBuffer buf) {
		this.arg = buf.readByte();
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putByte(TAG_MODE, this.arg);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.arg = nbt.getByte(TAG_MODE);
	}

	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(new WaitModeArgSelector(this.arg));
	}

}
