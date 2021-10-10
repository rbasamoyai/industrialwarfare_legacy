package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class StorageSideAccessArgHolder implements IArgHolder {

	private static final String TAG_SIDE = "side";

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
	public void toNetwork(PacketBuffer buf) {
		buf.writeByte(this.arg.get3DDataValue());
	}
	
	@Override
	public void fromNetwork(PacketBuffer buf) {
		this.arg = Direction.from3DDataValue(buf.readByte());
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putByte(TAG_SIDE, (byte) this.arg.get3DDataValue());
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.arg = Direction.from3DDataValue(nbt.getByte(TAG_SIDE));
	}

	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(new StorageSideAccessArgSelector(this.arg.get3DDataValue()));
	}
	
}
