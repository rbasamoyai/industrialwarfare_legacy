package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class ItemCountArgHolder implements IArgHolder {

	private static final String TAG_COUNT = "count";
	
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
		tag.putByte(TAG_COUNT, this.arg);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.arg = nbt.getByte(TAG_COUNT);
	}

	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(new ItemCountArgSelector(this.arg));
	}

}
