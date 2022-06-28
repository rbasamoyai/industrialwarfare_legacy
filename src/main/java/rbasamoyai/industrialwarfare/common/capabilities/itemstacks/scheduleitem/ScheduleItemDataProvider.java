package rbasamoyai.industrialwarfare.common.capabilities.itemstacks.scheduleitem;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ScheduleItemDataProvider implements ICapabilitySerializable<CompoundTag> {

	private final IScheduleItemData dataHandler = new ScheduleItemDataHandler();
	private final LazyOptional<IScheduleItemData> dataOptional = LazyOptional.of(() -> this.dataHandler);
	
	public void invalidate() {
		this.dataOptional.invalidate();
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == ScheduleItemCapability.INSTANCE ? this.dataOptional.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return ScheduleItemCapability.INSTANCE.isRegistered() ? this.dataHandler.writeTag(new CompoundTag()) : new CompoundTag();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		if (ScheduleItemCapability.INSTANCE.isRegistered()) {
			this.dataHandler.readTag(nbt);
		}
	}

}
