package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.common;

import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import rbasamoyai.industrialwarfare.client.screen.widgets.ArgSelector;
import rbasamoyai.industrialwarfare.common.containers.TaskScrollContainer;
import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class BlockPosArgHolder implements IArgHolder {
	
	private static final String TAG_X = "x";
	private static final String TAG_Y = "y";
	private static final String TAG_Z = "z";
	
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
	public void toNetwork(PacketBuffer buf) {
		buf.writeBlockPos(this.arg);
	}
	
	@Override
	public void fromNetwork(PacketBuffer buf) {
		this.arg = buf.readBlockPos();
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.putInt(TAG_X, this.arg.getX());
		tag.putInt(TAG_Y, this.arg.getY());		
		tag.putInt(TAG_Z, this.arg.getZ());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.arg = new BlockPos(nbt.getInt(TAG_X), nbt.getInt(TAG_Y), nbt.getInt(TAG_Z));
	}
	
	@Override
	public Optional<ArgSelector<?>> getSelector(TaskScrollContainer container) {
		return Optional.of(new BlockPosArgSelector(container.getPlayer(), this.arg));
	}
	
}
