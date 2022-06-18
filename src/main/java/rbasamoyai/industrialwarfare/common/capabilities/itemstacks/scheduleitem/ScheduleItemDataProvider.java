package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ScheduleItemDataProvider implements ICapabilitySerializable<CompoundNBT> {

	private final ScheduleItemDataHandler dataHandler = new ScheduleItemDataHandler();
	private final LazyOptional<IScheduleItemDataHandler> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER ? this.dataOptional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundNBT serializeNBT() {
		return ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER == null
				? new CompoundNBT()
				: (CompoundNBT) ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER.writeNBT(this.dataHandler, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		if (ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER != null)
			ScheduleItemDataCapability.SCHEDULE_ITEM_DATA_HANDLER.readNBT(this.dataHandler, null, nbt);
	}

}
