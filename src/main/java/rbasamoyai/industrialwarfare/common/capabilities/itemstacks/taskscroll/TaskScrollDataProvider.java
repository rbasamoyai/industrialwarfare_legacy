package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.taskscroll;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TaskScrollDataProvider implements ICapabilitySerializable<CompoundNBT> {

	private final TaskScrollDataHandler dataHandler = new TaskScrollDataHandler();
	private final LazyOptional<ITaskScrollDataHandler> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return this.dataOptional.cast();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY == null
				? new CompoundNBT()
				: (CompoundNBT) TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY.writeNBT(this.dataHandler, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY != null)
			TaskScrollDataCapability.TASK_SCROLL_DATA_CAPABILITY.readNBT(this.dataHandler, null, nbt);
	}

}
