package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TaskScrollDataProvider implements ICapabilitySerializable<CompoundTag> {

	private final ITaskScrollData dataHandler = new TaskScrollDataHandler();
	private final LazyOptional<ITaskScrollData> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == TaskScrollCapability.INSTANCE ? this.dataOptional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return TaskScrollCapability.INSTANCE.isRegistered() ? this.dataHandler.writeTag(new CompoundTag()) : new CompoundTag();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		if (TaskScrollCapability.INSTANCE.isRegistered()) {
			this.dataHandler.readTag(nbt);
		}
	}

}
